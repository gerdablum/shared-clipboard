package de.alina.clipboard.app.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import de.alina.clipboard.app.R
import de.alina.clipboard.app.client.*
import de.alina.clipboard.app.controller.AppController
import de.alina.clipboard.app.controller.AppController.Companion.CAPTURE_PICTURE_REQUEST
import de.alina.clipboard.app.databinding.ActivityMainBinding
import de.alina.clipboard.app.databinding.ContentMainBinding
import de.alina.clipboard.app.databinding.ServerAdressMainBinding
import de.alina.clipboard.app.manager.*


class MainActivity : AppCompatActivity(), BaseView {

    private lateinit var activityBinding: ActivityMainBinding
    private lateinit var mainContentBinding: ContentMainBinding
    private lateinit var serverBinding: ServerAdressMainBinding

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

        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        mainContentBinding = ContentMainBinding.inflate(layoutInflater)
        serverBinding = ServerAdressMainBinding.inflate(layoutInflater)


        super.onCreate(savedInstanceState)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = activityBinding.root
        setContentView(view)

        setSupportActionBar(activityBinding.toolbar)
        mainContentBinding.alertImage.visibility = View.INVISIBLE
        mainContentBinding.alertText.visibility = View.INVISIBLE
        mainContentBinding.buttonLogout.visibility = View.INVISIBLE
        serverBinding.serverAdressInputText.setText( controller.getCurrentServerAddress() ?: "")
        serverBinding.serverAdressInputText.setOnEditorActionListener { v, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                controller.setCurrentServerAddress(v.text.toString())
                hideKeyboard(v)
                v.clearFocus()
                true
            } else {
                false
            }
        }
        activityBinding.fab.setOnClickListener { controller.captureImage() }
        mainContentBinding.buttonLogout.setOnClickListener {
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
        activityBinding.fab.hide()
        mainContentBinding.signedInText.text = getString(R.string.no_internet_info)
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
        mainContentBinding.alertImage.visibility = View.INVISIBLE
        mainContentBinding.alertText.visibility = View.INVISIBLE
        activityBinding.fab.hide()
        mainContentBinding.buttonLogout.visibility = View.VISIBLE
        mainContentBinding.signedInText.text = getString(R.string.signed_in_info)

    }

    override fun showLogoutSuccessful() {
        activityBinding.fab.show()
        mainContentBinding.buttonLogout.visibility = View.INVISIBLE
        mainContentBinding.signedInText.text = getString(R.string.not_signed_in_info)
    }
    override fun showFailure() {
        mainContentBinding.alertImage.visibility = View.VISIBLE
        mainContentBinding.alertText.visibility = View.VISIBLE
    }

    override fun serverUrlIncorrect() {
        // TODO: show feedback
    }

    override fun showSendDataFailure() {
        Toast.makeText(this, (R.string.send_data_failed), Toast.LENGTH_LONG).show()
    }

    override fun showGetDataFailure() {
        mainContentBinding.alertImage.visibility = View.VISIBLE
        mainContentBinding.alertText.text = getString(R.string.get_data_failed)
        mainContentBinding.alertText.visibility = View.VISIBLE
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
