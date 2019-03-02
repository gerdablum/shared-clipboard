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
import de.alina.clipboard.app.callbacks.UUIDCheckerCallback
import de.alina.clipboard.app.utils.UUIDChecker

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity(), UUIDCheckerCallback{

    override fun uuidValid(uuid: UUID) {
        alert_image.visibility = View.INVISIBLE
        alert_text.visibility = View.INVISIBLE
        //TODO setup sharing stuff
    }

    override fun uuidInvalid() {
        alert_image.visibility = View.VISIBLE
        alert_text.visibility = View.VISIBLE
    }

    private val CAPTURE_PICTURE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        alert_image.visibility = View.INVISIBLE
        alert_text.visibility = View.INVISIBLE
        checkAuthentication()
    }

    private fun checkAuthentication(){
        val sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userID = sharedPref.getString(getString(R.string.user_auth_id_key), "") ?: ""

        if (userID != "" && isSessionValid(userID)) {
            fab.hide()
            signed_in_text.text = getString(R.string.signed_in_info)
        } else if (!hasInternetConnection()) {
            fab.hide()
            signed_in_text.text = getString(R.string.no_internet_info)
        } else {
            fab.show()
            fab.setOnClickListener { captureImage() }
            signed_in_text.text = getString(R.string.not_signed_in_info)
        }
    }

    private fun isSessionValid(id: String): Boolean {
        // TODO implement
        return true
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
                this.uuidInvalid()
                return@addOnSuccessListener
            }
            for (firebaseBarcode in it) {
                val a = it[0].rawValue ?: ""
                Log.d(this@MainActivity.localClassName, "###" + a)
                UUIDChecker.checkString(a, this)

            }
        }.addOnFailureListener {
            it.printStackTrace()
            Toast.makeText(baseContext, "Sorry, something went wrong!", Toast.LENGTH_SHORT).show()
        }
    }

}
