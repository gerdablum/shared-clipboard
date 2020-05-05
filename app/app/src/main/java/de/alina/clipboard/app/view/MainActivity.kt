package de.alina.clipboard.app.view

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import de.alina.clipboard.app.R
import de.alina.clipboard.app.controller.AppController
import de.alina.clipboard.app.controller.AppController.Companion.CAPTURE_PICTURE_REQUEST

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.MediaType

class MainActivity : AppCompatActivity(), BaseView {

    private val controller = AppController(this, this)

    init {
        lifecycle.addObserver(controller)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        alert_image.visibility = View.INVISIBLE
        alert_text.visibility = View.INVISIBLE
        button_logout.visibility = View.INVISIBLE
        fab.setOnClickListener { controller.captureImage() }
        button_logout.setOnClickListener {
            controller.logoutUser()
        }
        when {
            intent?.action == Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    Log.d("ShareActivity", "text shared")
                } else if (intent.type.contains("image/")) {
                    (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM)as? Uri)?.let {
                        controller.uploadBytes(it, MediaType.parse(intent.type)!!)
                        Log.d("MainActivity", "image shared")
                    }
                } else if (intent.type == "application/pdf") {

                }
            }
            else -> Log.d("MainActivity", "do nothing")
        }
    }

    override fun showLoginFailure() {
        showFailure()
    }

    override fun showNoInternetConnection() {
        fab.hide()
        signed_in_text.text = getString(R.string.no_internet_info)
    }

    override fun showSendDataSuccessful() {
        Toast.makeText(this, getString(R.string.send_Data_successful), Toast.LENGTH_LONG).show()
    }

    override fun showGetDataSuccessfull() {
        // TODO: wait for server implementation and copy to local clipboard
    }

    override fun showLogoutFailure() {
        // TODO
        showLogoutSuccessful()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAPTURE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            controller.getImage(data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showLoggedInSuccessful() {
        alert_image.visibility = View.INVISIBLE
        alert_text.visibility = View.INVISIBLE
        fab.hide()
        button_logout.visibility = View.VISIBLE
        signed_in_text.text = getString(R.string.signed_in_info)

    }

    override fun showLogoutSuccessful() {
        fab.show()
        button_logout.visibility = View.INVISIBLE
        signed_in_text.text = getString(R.string.not_signed_in_info)
    }
    override fun showFailure() {
        alert_image.visibility = View.VISIBLE
        alert_text.visibility = View.VISIBLE
    }

    override fun showSendDataFailure() {
        Toast.makeText(this, (R.string.send_data_failed), Toast.LENGTH_LONG).show()
    }

    override fun showGetDataFailure() {
        alert_image.visibility = View.VISIBLE
        alert_text.text = getString(R.string.get_data_failed)
        alert_text.visibility = View.VISIBLE
    }

    companion object {
        const val CHANNEL_ID = "de.alina.clipboard.app.notificationChannel"
    }

}
