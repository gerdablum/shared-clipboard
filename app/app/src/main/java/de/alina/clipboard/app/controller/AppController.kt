package de.alina.clipboard.app.controller

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import de.alina.clipboard.app.client.*
import de.alina.clipboard.app.manager.*
import de.alina.clipboard.app.model.User
import de.alina.clipboard.app.view.BaseView
import okhttp3.MediaType
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
                    private val notifManager: ClipboardNotificationManager,
                    private val fileManager: FileManager) : ClipboardServerAPICallback, LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        notifManager.createNotificationChannel(context)
        ackController.subscribe(this)
        logoutController.subscribe(this)
        connectController.subscribe(this)
        uploadController.subscribe(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        // user = authManager.getUserKey(context)
        if (!hasInternetConnection()) {
            view.showNoInternetConnection()
        }

        val user = authManager.getActiveUser(context)

        // user is already logged out
        if (user == null) {
            view.showLogoutSuccessful()
        }
        // user requested logout but it failed
        else if (user != null && authManager.logoutRequested(context)) {
            logoutUser()
        }
        // user is logged in successfully
        else if (user != null) {
            user.id?.let { connectController.isConnected(it) }
            view.showLoggedInSuccessful()
        }

    }

    fun logoutUser() {
        val user = authManager.getActiveUser(context)
        authManager.logoutUser(context)
        if (user != null) {
            user.id?.let {
                logoutController.logout(it)
            }
            serviceManager.stopCopyListenService(context)
            view.showLogoutSuccessful()
        }

    }

    fun uploadBytes(fileUri: Uri, mimeType: MediaType) {
        val user = authManager.getActiveUser(context)
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val byteArray = fileManager.getBytes(inputStream)
        val filename = fileManager.getFilename(context, fileUri)
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
            qrManager.scanQRCode(imageBitmap, (object : QRInterface {
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

    override fun onSuccess(data: Bundle, type: ClipboardServerAPICallback.CallType) {
        when (type) {
            ClipboardServerAPICallback.CallType.ACKNOWLEDGE -> onAckSuccessful(data)
            ClipboardServerAPICallback.CallType.SEND_DATA -> onSendDataSuccessful()
            ClipboardServerAPICallback.CallType.SEND_FILE_DATA -> view.showSendDataSuccessful()
            ClipboardServerAPICallback.CallType.GET_DATA -> view.showGetDataSuccessful()
            ClipboardServerAPICallback.CallType.LOGOUT -> onLogoutSuccessful()
            ClipboardServerAPICallback.CallType.CONNECTION -> {
                authManager.getActiveUser(context)?.let {
                    serviceManager.startCopyListenService(context, it)
                }

            }
        }
    }

    private fun onLogoutSuccessful() {
        authManager.deleteUserData(context)
        Log.d(AppController::class.java.name, "User logged out successful")
    }

    private fun onSendDataSuccessful() {
        Log.d(AppController::class.java.name, "copied data from background")
        view.showSendDataSuccessful()
    }

    private fun onAckSuccessful(data: Bundle) {
        data.getString(ClipboardServerAPICallback.CALLBACK_ID_KEY)?.let {
            authManager.storeUser(it, context)
            serviceManager.startCopyListenService(context, authManager.getActiveUser(context) )
            view.showLoggedInSuccessful()
        }
    }


    override fun onFailure(data: Bundle, type: ClipboardServerAPICallback.CallType, t: Throwable?) {
        t?.printStackTrace()
        when (type) {
            ClipboardServerAPICallback.CallType.ACKNOWLEDGE -> view.showLoginFailure()
            ClipboardServerAPICallback.CallType.SEND_DATA -> {
                Log.d(AppController::class.java.name, "failed to copy data from background")
                view.showSendDataFailure()
            }
            ClipboardServerAPICallback.CallType.SEND_FILE_DATA -> view.showSendDataFailure()
            ClipboardServerAPICallback.CallType.GET_DATA -> view.showGetDataFailure()
            ClipboardServerAPICallback.CallType.LOGOUT -> view.showLogoutFailure()
            ClipboardServerAPICallback.CallType.CONNECTION -> {
                authManager.getActiveUser(context)?.let {
                    logoutUser()
                }
                view.showLogoutSuccessful()
            }
        }
    }

    fun handleShareImageEvent(intent: Intent) {
        if (intent.type == "text/plain") {
            Log.d("ShareActivity", "text shared")
        }
        else if (intent.type.contains("image/") || intent.type == "application/pdf") {
            (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                uploadBytes(it, MediaType.parse(intent.type)!!)
                Log.d("MainActivity", "image shared")
            }
        }
    }

    companion object {
        val CAPTURE_PICTURE_REQUEST = 1
    }

}