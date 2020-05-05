package de.alina.clipboard.app.controller

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
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
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import de.alina.clipboard.app.R
import de.alina.clipboard.app.client.*
import de.alina.clipboard.app.model.User
import de.alina.clipboard.app.service.CopyEventService
import de.alina.clipboard.app.view.BaseView
import de.alina.clipboard.app.view.MainActivity
import okhttp3.MediaType
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Exception
import java.util.*

class AppController(private val context: Activity, private val view: BaseView):  ClipboardServerAPICallback, LifecycleObserver {

    private val ackController by lazy { AcknowledgeController(this)}
    private val logoutController by lazy { LogoutController(this)}
    private val connectController by lazy { TestConnectionController(this) }
    private val uploadController by lazy { UploadDataController(this) }
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

    fun uploadBytes(fileUri: Uri, mimeType: MediaType) {
        checkUserKey()
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

    public fun checkUserKey(){
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
        //TODO: Change back, only for testing
        checkAndProceedUUID("9a5855e2-3dbc-4f57-9c9c-9b2f642e48a6")
        /*detector.detectInImage(image).addOnSuccessListener {
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
        }*/
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
            ClipboardServerAPICallback.CallType.CONNECTION -> processLogoutSuccessful()
        }
    }

    companion object {
        val CAPTURE_PICTURE_REQUEST = 1
    }
}