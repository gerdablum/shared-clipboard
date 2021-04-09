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
import de.alina.clipboard.app.view.BaseView
import okhttp3.MediaType

class AppController(private val context: Activity, private val view: BaseView,
                    private val apiController: APIManager,
                    private val authManager: AuthManager,
                    private val serviceManager: BackgroundServiceManager,
                    private val qrManager: QRManager,
                    private val notifManager: ClipboardNotificationManager,
                    private val fileManager: FileManager,
                    private val serverManager: ServerAddressManager) : APIManagerCallback, LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        notifManager.createNotificationChannel(context)
        apiController.subscribe(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (!hasInternetConnection()) {
            view.showNoInternetConnection()
        }

        val user = authManager.getActiveUser(context)

        // user is already logged out
        if (user == null) {
            view.showLogoutSuccessful()
        }
        // user requested logout but it failed
        else if (authManager.logoutRequested(context)) {
            logoutUser()
        }
        // user is logged in successfully
        else {
            user.id?.let { apiController.isConnected(it) }
            view.showLoggedInSuccessful()
        }

    }

    fun logoutUser() {
        val user = authManager.getActiveUser(context)
        authManager.logoutUser(context)
        if (user != null) {
            user.id?.let {
                apiController.logout(it)
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
            apiController.sendFileData(it, byteArray, mimeType, filename ?: "unknown-file")
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
                        apiController.acknowledge(uuid)
                    } else {
                        view.showLoginFailure()
                    }
                }
            }))
        }
    }

    override fun onSuccess(data: Bundle, type: APIManagerCallback.CallType) {
        when (type) {
            APIManagerCallback.CallType.ACKNOWLEDGE -> onAckSuccessful(data)
            APIManagerCallback.CallType.SEND_DATA -> onSendDataSuccessful()
            APIManagerCallback.CallType.SEND_FILE_DATA -> view.showSendDataSuccessful()
            APIManagerCallback.CallType.GET_DATA -> view.showGetDataSuccessful()
            APIManagerCallback.CallType.LOGOUT -> onLogoutSuccessful()
            APIManagerCallback.CallType.CONNECTION -> {
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
        data.getString(APIManagerCallback.CALLBACK_ID_KEY)?.let {
            authManager.storeUser(it, context)
            serviceManager.startCopyListenService(context, authManager.getActiveUser(context))
            view.showLoggedInSuccessful()
        }
    }


    override fun onFailure(data: Bundle, type: APIManagerCallback.CallType, t: Throwable?) {
        t?.printStackTrace()
        when (type) {
            APIManagerCallback.CallType.ACKNOWLEDGE -> view.showLoginFailure()
            APIManagerCallback.CallType.SEND_DATA -> {
                Log.d(AppController::class.java.name, "failed to copy data from background")
                view.showSendDataFailure()
            }
            APIManagerCallback.CallType.SEND_FILE_DATA -> view.showSendDataFailure()
            APIManagerCallback.CallType.GET_DATA -> view.showGetDataFailure()
            APIManagerCallback.CallType.LOGOUT -> view.showLogoutFailure()
            APIManagerCallback.CallType.CONNECTION -> {
                authManager.getActiveUser(context)?.let {
                    logoutUser()
                }
                view.showLogoutSuccessful()
            }
        }
    }


    fun handleShareImageEvent(intent: Intent) {
        if (intent.type == null) {
            return
        }
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            uploadBytes(it, MediaType.parse(intent.type)!!)
            Log.d("MainActivity", "image shared")
        }

    }

    fun getCurrentServerAddress(): String {
        val url = serverManager.getAddress(context)
        if (url != ClipboardServerAPI.BASE_URL && Regex("http://.*").matches(url)) {
            ClipboardServerAPI.BASE_URL = url
            apiController.reload()
        }
        return ClipboardServerAPI.BASE_URL ?: ""
    }

    fun setCurrentServerAddress(url: String) {
        if (!Regex("http://.*").matches(url)) {
            view.serverUrlIncorrect()
            return
        }
        ClipboardServerAPI.BASE_URL = url
        apiController.reload()
        serverManager.saveAddress(url, context)
    }

    companion object {
        const val CAPTURE_PICTURE_REQUEST = 1
    }

}