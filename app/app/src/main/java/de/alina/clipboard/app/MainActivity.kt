package de.alina.clipboard.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import de.alina.clipboard.app.client.AcknowledgeController
import de.alina.clipboard.app.client.AcknowledgeController.Companion.CALLBACK_KEY_ERROR_CODE
import de.alina.clipboard.app.client.ClipboardServerAPICallback
import de.alina.clipboard.app.client.LogoutController
import de.alina.clipboard.app.model.User

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity(), ClipboardServerAPICallback {

    private val ackController by lazy { AcknowledgeController(this) }
    private val logoutController by lazy { LogoutController(this) }
    private val CAPTURE_PICTURE_REQUEST = 1
    private var user: User? = null
    private var userLoggedIn = true;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        alert_image.visibility = View.INVISIBLE
        alert_text.visibility = View.INVISIBLE
        button_logout.visibility = View.INVISIBLE
        fab.setOnClickListener { captureImage() }
        button_logout.setOnClickListener {
            val id = user?.id
            if (id != null) {
                logoutController.logout(id)
            }
        }
        checkAuthentication()
        user?.id?.let { ackController.acknowledge(it) }
    }

    override fun onSuccess(data: Bundle, type: ClipboardServerAPICallback.CallType) {
        when(type) {
            ClipboardServerAPICallback.CallType.ACKNOWLEDGE -> processAuthenticationSuccessful(data)
            ClipboardServerAPICallback.CallType.SEND_DATA -> processSendDataSuccessful(data)
            ClipboardServerAPICallback.CallType.SEND_FILE_DATA -> processSendDataSuccessful(data)
            ClipboardServerAPICallback.CallType.GET_DATA -> processGetDataSuccessful(data)
            ClipboardServerAPICallback.CallType.LOGOUT -> processLogoutSuccessful(data)
        }
    }

    override fun onFailure(data: Bundle, type: ClipboardServerAPICallback.CallType, t: Throwable?) {
        t?.printStackTrace()
        when(type) {
            ClipboardServerAPICallback.CallType.ACKNOWLEDGE -> showFailure(data)
            ClipboardServerAPICallback.CallType.SEND_DATA -> showSendDataFailure()
            ClipboardServerAPICallback.CallType.SEND_FILE_DATA -> showSendDataFailure()
            ClipboardServerAPICallback.CallType.GET_DATA -> showGetDataFailure()
            ClipboardServerAPICallback.CallType.LOGOUT -> showLogoutFailure()
        }
    }

    private fun processAuthenticationSuccessful(data: Bundle) {
        data.getString(AcknowledgeController.CALLBACK_ID_KEY)?.let {
            user = User(UUID.fromString(it))
            val editor = this.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
            editor.putString(getString(R.string.user_auth_id_key), it)
            editor.apply()
            showLoggedInSuccessfully()
            userLoggedIn = true
        }
    }

    private fun processSendDataSuccessful(data: Bundle) {
        Toast.makeText(this, getString(R.string.send_Data_successful), Toast.LENGTH_LONG).show()
    }

    private fun processGetDataSuccessful(data: Bundle) {
        // TODO: wait for server implementation and copy to local clipboard
    }

    private fun processLogoutSuccessful(data: Bundle) {
        user?.let {
            val editor = this.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
            editor.putString(getString(R.string.user_auth_id_key), "")
            editor.apply()
        }
        user = null
        userLoggedIn = false
        checkAuthentication()
    }

    private fun showLogoutFailure() {
        // TODO
    }

    private fun checkAuthentication(){
        val sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userID = sharedPref.getString(getString(R.string.user_auth_id_key), "") ?: ""
        // TODO check connection with server
       if (!hasInternetConnection()) {
            fab.hide()
            signed_in_text.text = getString(R.string.no_internet_info)
        } else if (userID != "") {
            showLoggedInSuccessfully()
           userLoggedIn = true
           user = User(UUID.fromString(userID))
        } else {
            showLoggedOutSuccessfully()
        }
    }

    private fun hasInternetConnection(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected ?: false
    }

    private fun captureImage() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, CAPTURE_PICTURE_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAPTURE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            data?.let {
                val imageBitmap = data.extras?.get("data") as Bitmap
                scanQRCode(imageBitmap)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun scanQRCode(bitmap: Bitmap) {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build()
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        detector.detectInImage(image).addOnSuccessListener {
            if (it.isEmpty()) {
                showFailure()
                return@addOnSuccessListener
            }
            for (firebaseBarcode in it) {
                val a = it[0].rawValue ?: ""
                Log.d(this@MainActivity.localClassName, "###" + a)
                checkAndProceedUUID(a)

            }
        }.addOnFailureListener {
            it.printStackTrace()
            showFailure()
        }
    }

    private fun checkAndProceedUUID(id: String) {
        try {
            val uuid = UUID.fromString(id)
            ackController.acknowledge(uuid)
        } catch (e: Exception) {
            e.printStackTrace()
            showFailure()
        }
    }

    private fun showLoggedInSuccessfully() {
        alert_image.visibility = View.INVISIBLE
        alert_text.visibility = View.INVISIBLE
        fab.hide()
        button_logout.visibility = View.VISIBLE
        signed_in_text.text = getString(R.string.signed_in_info)
    }

    private fun showLoggedOutSuccessfully() {
        fab.show()
        button_logout.visibility = View.INVISIBLE
        signed_in_text.text = getString(R.string.not_signed_in_info)
    }
    private fun showFailure(data: Bundle? = null) {
        // error code 401 means we are no longer connected to the server. Log out then.
        val errorCode = data?.get(CALLBACK_KEY_ERROR_CODE)
        if (errorCode == 401) {
            Log.d("MainActivity", "Logged user out because of 401 response of server")
            showLoggedOutSuccessfully()
            userLoggedIn = false
        }
        alert_image.visibility = View.VISIBLE
        alert_text.visibility = View.VISIBLE
    }

    private fun showSendDataFailure() {
        Toast.makeText(this, (R.string.send_data_failed), Toast.LENGTH_LONG).show()
    }

    private fun showGetDataFailure() {
        alert_image.visibility = View.VISIBLE
        alert_text.text = getString(R.string.get_data_failed)
        alert_text.visibility = View.VISIBLE
    }

}
