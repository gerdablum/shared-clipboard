package de.alina.clipboard.app.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import de.alina.clipboard.app.R
import de.alina.clipboard.app.client.*
import de.alina.clipboard.app.controller.AppController
import de.alina.clipboard.app.controller.AppController.Companion.CAPTURE_PICTURE_REQUEST
import de.alina.clipboard.app.manager.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.server_adress_main.*

class MainActivity : AppCompatActivity(), BaseView {

    private val controller = AppController(this, this,
            APIManagerImpl(),
            AuthManager(),
            BackgroundServiceManager(),
            QRManager(),
            ClipboardNotificationManager(),
            FileManager(),
            ServerAddressManager())


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
        server_adress_input_text.setText( controller.getCurrentServerAddress() ?: "")
        server_adress_input_text.setOnEditorActionListener { v, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                controller.setCurrentServerAddress(v.text.toString())
                hideKeyboard(v)
                v.clearFocus()
                true
            } else {
                false
            }
        }
        fab.setOnClickListener { controller.captureImage() }
        button_logout.setOnClickListener {
            controller.logoutUser()
        }
        val disconnectClicked = intent.getBooleanExtra(
                ClipboardNotificationManager.DISCONNECT_REQUESTED_KEY,false)
        if (intent?.action == Intent.ACTION_SEND) {
                controller.handleShareImageEvent(intent)
        }
        if (disconnectClicked) {
            controller.logoutUser()
        }
        else Log.d("MainActivity", "do nothing")
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

    override fun showGetDataSuccessful() {
        // TODO: wait for server implementation and copy to local clipboard
    }

    override fun showLogoutFailure() {
        // TODO
        showLogoutSuccessful()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAPTURE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            controller.processImage(data)
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

    override fun serverUrlIncorrect() {
        // TODO: show feedback
    }

    override fun showSendDataFailure() {
        Toast.makeText(this, (R.string.send_data_failed), Toast.LENGTH_LONG).show()
    }

    override fun showGetDataFailure() {
        alert_image.visibility = View.VISIBLE
        alert_text.text = getString(R.string.get_data_failed)
        alert_text.visibility = View.VISIBLE
    }

    fun hideKeyboard(editText: View) {
        val inputManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE)
        if (inputManager is InputMethodManager) {
            inputManager.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    companion object {
        const val CHANNEL_ID = "de.alina.clipboard.app.notificationChannel"
    }

}
