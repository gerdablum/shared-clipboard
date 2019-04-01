package de.alina.clipboard.app.controller

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import de.alina.clipboard.app.R
import de.alina.clipboard.app.client.AcknowledgeController
import de.alina.clipboard.app.client.ClipboardServerAPICallback
import de.alina.clipboard.app.client.LogoutController
import de.alina.clipboard.app.client.TestConnectionController
import de.alina.clipboard.app.model.User
import de.alina.clipboard.app.service.CopyEventService
import de.alina.clipboard.app.view.BaseView
import de.alina.clipboard.app.view.MainActivity
import java.lang.Exception
import java.util.*

class AppController(private val context: Activity, private val view: BaseView):  ClipboardServerAPICallback, LifecycleObserver {

    private val ackController by lazy { AcknowledgeController(this)}
    private val logoutController by lazy { LogoutController(this)}
    private val connectController by lazy { TestConnectionController(this) }
    var user: User? = null
    var userLoggedIn = false

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        checkUserKey()
        user?.id?.let { connectController.isConnected(it) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        createNotificationChannel()
    }

    fun logoutUser() {
        val id = user?.id
            if (id != null) {
                logoutController.logout(id)
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(MainActivity.CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    private fun checkUserKey(){
        val sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userID = sharedPref.getString(context.getString(R.string.user_auth_id_key), "") ?: ""
        if (!hasInternetConnection()) {
            view.showNoInternetConnection()
        } else if (userID != "") {
            user = User(UUID.fromString(userID))
        } else {
            user = null
            userLoggedIn = false
            view.showLogoutSuccessful()
        }
    }

    private fun hasInternetConnection(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected ?: false
    }

    private fun authenticationSuccessful(data: Bundle) {
        data.getString(ClipboardServerAPICallback.CALLBACK_ID_KEY)?.let {
            user = User(UUID.fromString(it))
            val editor = context.getSharedPreferences(
                    context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
            editor.putString(context.getString(R.string.user_auth_id_key), it)
            editor.apply()
            loginSuccessfull()
            view.showLoggedInSuccessful()
            userLoggedIn = true
        }
    }

    private fun loginSuccessfull() {
        view.showLoggedInSuccessful()
        //TODO only start service if not already running
        if (CopyEventService.isRunning) {
            return
        }
        val intent = Intent(context, CopyEventService::class.java)
        intent.putExtra(User.USER_KEY, user?.id.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun checkAndProceedUUID(id: String) {
        try {
            val uuid = UUID.fromString(id)
            ackController.acknowledge(uuid)
        } catch (e: Exception) {
            e.printStackTrace()
            view.showFailure()
        }
    }

    private fun processLogoutSuccessful(data: Bundle? = null) {
        user?.let {
            val editor = context.getSharedPreferences(
                    context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
            editor.putString(context.getString(R.string.user_auth_id_key), "")
            editor.apply()
        }
        user = null
        userLoggedIn = false
        val intent = Intent(context, CopyEventService::class.java)
        intent.putExtra(User.USER_KEY, user?.id.toString())
        context.stopService(intent)
        view.showLogoutSuccessful()
    }

    private fun processSendDataSuccessful(data: Bundle) {
        view.showSendDataSuccessful()
    }

    private fun processGetDataSuccessful(data: Bundle) {
        view.showGetDataSuccessfull()
    }

     fun captureImage() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(context.packageManager)?.also {
                context.startActivityForResult(takePictureIntent, CAPTURE_PICTURE_REQUEST)
            }
        }
    }

    fun getImage(data: Intent?) {
        data?.let {
            val imageBitmap = data.extras?.get("data") as Bitmap
            scanQRCode(imageBitmap)
        }
    }

    private fun scanQRCode(bitmap: Bitmap) {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build()
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        detector.detectInImage(image).addOnSuccessListener {
            if (it.isEmpty()) {
                view.showFailure()
                return@addOnSuccessListener
            }
            for (firebaseBarcode in it) {
                val a = it[0].rawValue ?: ""
                checkAndProceedUUID(a)

            }
        }.addOnFailureListener {
            it.printStackTrace()
            view.showFailure()
        }
    }


    override fun onSuccess(data: Bundle, type: ClipboardServerAPICallback.CallType) {
        when(type) {
            ClipboardServerAPICallback.CallType.ACKNOWLEDGE -> authenticationSuccessful(data)
            ClipboardServerAPICallback.CallType.SEND_DATA -> processSendDataSuccessful(data)
            ClipboardServerAPICallback.CallType.SEND_FILE_DATA -> processSendDataSuccessful(data)
            ClipboardServerAPICallback.CallType.GET_DATA -> processGetDataSuccessful(data)
            ClipboardServerAPICallback.CallType.LOGOUT -> processLogoutSuccessful(data)
            ClipboardServerAPICallback.CallType.CONNECTION -> loginSuccessfull()
        }
    }


    override fun onFailure(data: Bundle, type: ClipboardServerAPICallback.CallType, t: Throwable?) {
        t?.printStackTrace()
        when(type) {
            ClipboardServerAPICallback.CallType.ACKNOWLEDGE -> view.showLoginFailure()
            ClipboardServerAPICallback.CallType.SEND_DATA -> view.showSendDataFailure()
            ClipboardServerAPICallback.CallType.SEND_FILE_DATA -> view.showSendDataFailure()
            ClipboardServerAPICallback.CallType.GET_DATA -> view.showGetDataFailure()
            ClipboardServerAPICallback.CallType.LOGOUT -> view.showLogoutFailure()
            ClipboardServerAPICallback.CallType.CONNECTION -> view.showLogoutSuccessful()
        }
    }

    companion object {
        val CAPTURE_PICTURE_REQUEST = 1
    }
}