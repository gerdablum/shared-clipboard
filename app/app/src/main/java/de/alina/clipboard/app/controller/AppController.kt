package de.alina.clipboard.app.controller

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import de.alina.clipboard.app.client.*
import de.alina.clipboard.app.manager.*
import de.alina.clipboard.app.model.User
import de.alina.clipboard.app.service.CopyEventService
import de.alina.clipboard.app.service.ServiceCallback
import de.alina.clipboard.app.view.BaseView
import okhttp3.MediaType
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class AppController(private val context: Activity, private val view: BaseView,
                    private val ackController: AcknowledgeController,
                    private val logoutController: LogoutController,
                    private val connectController: CheckConnectionController,
                    private val uploadController: UploadDataController,
                    private val sendDataController: SendDataController,
                    private val authManager: AuthManager,
                    private val serviceManager: ServiceManager,
                    private val qrManager: QRManager,
                    private val notifManager: ClipboardNotificationManager):  ClipboardServerAPICallback, LifecycleObserver, ServiceCallback {

    var user: User? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        user = authManager.getUserKey(context)
        if (!hasInternetConnection()) {
            view.showNoInternetConnection()
        }
        if (user == null) {
            view.showLogoutSuccessful()
        }
        user?.id?.let { connectController.isConnected(it) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        notifManager.createNotificationChannel(context)
        ackController.subscribe(this)
        logoutController.subscribe(this)
        connectController.subscribe(this)
        uploadController.subscribe(this)
    }

    fun logoutUser() {
        val id = user?.id
            if (id != null) {
                logoutController.logout(id)
            }
    }

    fun uploadBytes(fileUri: Uri, mimeType: MediaType) {
        user = authManager.getUserKey(context)
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val byteArray = getBytes(inputStream)

        val cursor: Cursor? = context.contentResolver.query( fileUri, null, null, null, null, null)

        val filename = cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (it.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))

            } else {
                "Unknown"
            }
        }

            user?.id?.let {
            uploadController.sendFileData(it, byteArray, mimeType, filename ?: "unknown-file")
        }
    }

    fun hasInternetConnection(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected ?: false
    }

     fun captureImage() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(context.packageManager)?.also {
                context.startActivityForResult(takePictureIntent, CAPTURE_PICTURE_REQUEST)
            }
        }
    }

    fun processImage(data: Intent?) {
        data?.let {
            val imageBitmap = data.extras?.get("data") as Bitmap
            qrManager.scanQRCode(imageBitmap, (object: QRInterface {
                override fun onQRScanFinished(id: String?) {
                    val uuid = authManager.isUUIDValid(id);
                    if (uuid != null) {
                        ackController.acknowledge(uuid)
                    } else {
                        view.showLoginFailure()
                    }
                }
            }))
        }
    }

    fun getBytes(inputStream: InputStream?): ByteArray {
        var byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024;
        var buffer = ByteArray(bufferSize);

        var len = inputStream?.read(buffer) ?: -1
        while (len != -1) {
            byteBuffer.write(buffer, 0, len);
            len = inputStream?.read(buffer) ?: -1
        }
        inputStream?.close()
        return byteBuffer.toByteArray();
    }

    override fun onSuccess(data: Bundle, type: ClipboardServerAPICallback.CallType) {
        when(type) {
            ClipboardServerAPICallback.CallType.ACKNOWLEDGE -> {
                user = authManager.getUserIDfromServerAndStore(data, context)
                serviceManager.startCopyListenService(context, user)
                view.showLoggedInSuccessful()
            }
            ClipboardServerAPICallback.CallType.SEND_DATA -> {
                Log.d(AppController::class.java.name, "copied data from background")
                view.showSendDataSuccessful()
            }
            ClipboardServerAPICallback.CallType.SEND_FILE_DATA -> view.showSendDataSuccessful()
            ClipboardServerAPICallback.CallType.GET_DATA -> view.showGetDataSuccessfull()
            ClipboardServerAPICallback.CallType.LOGOUT -> {
                authManager.logoutUser(context)
                serviceManager.stopCopyListenService(context, user)
                user = null
                view.showLogoutSuccessful()
            }
            ClipboardServerAPICallback.CallType.CONNECTION -> serviceManager.startCopyListenService(context, user)
        }
    }


    override fun onFailure(data: Bundle, type: ClipboardServerAPICallback.CallType, t: Throwable?) {
        t?.printStackTrace()
        when(type) {
            ClipboardServerAPICallback.CallType.ACKNOWLEDGE -> view.showLoginFailure()
            ClipboardServerAPICallback.CallType.SEND_DATA -> {
                Log.d(AppController::class.java.name, "failed to copy data from background")
                view.showSendDataFailure()
            }
            ClipboardServerAPICallback.CallType.SEND_FILE_DATA -> view.showSendDataFailure()
            ClipboardServerAPICallback.CallType.GET_DATA -> view.showGetDataFailure()
            ClipboardServerAPICallback.CallType.LOGOUT -> view.showLogoutFailure()
            ClipboardServerAPICallback.CallType.CONNECTION -> {
                authManager.logoutUser(context)
                serviceManager.stopCopyListenService(context, user)
                user = null
                view.showLogoutSuccessful()
            }
        }
    }

    fun handleShareImageEvent(intent: Intent) {
        if (intent.type == "text/plain") {
            Log.d("ShareActivity", "text shared")
        } else if (intent.type.contains("image/")) {
            (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM)as? Uri)?.let {
                uploadBytes(it, MediaType.parse(intent.type)!!)
                Log.d("MainActivity", "image shared")
            }
        } else if (intent.type == "application/pdf") {

        }
    }

    override fun performLogout(uuid: UUID) {
        //serviceManager.stopCopyListenService(context, user)
        logoutController.logout(uuid)
    }

    override fun onCopyEvent(uuid: UUID, text: String) {
        sendDataController.sendStringData(uuid, text)
    }

    companion object {
        val CAPTURE_PICTURE_REQUEST = 1
    }

}