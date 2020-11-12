package de.alina.clipboard.app.service

import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder

import android.util.Log
import de.alina.clipboard.app.client.APIManagerCallback
import de.alina.clipboard.app.client.SendDataController
import de.alina.clipboard.app.manager.ClipboardNotificationManager
import de.alina.clipboard.app.model.User.Companion.USER_KEY
import de.alina.clipboard.app.view.MainActivity
import java.util.*

class CopyEventService: Service(), APIManagerCallback{
    lateinit var notifManager: ClipboardNotificationManager
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        notifManager = ClipboardNotificationManager()
        isRunning = false
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val idString = intent.getStringExtra(USER_KEY)
        val id = UUID.fromString(idString)
        listenToCopyEvents(id)
        isRunning = true
        // val cancelIntent = Intent(this, CancelServiceReceiver::class.java)
        val cancelIntent = Intent(this, MainActivity::class.java)

        startForeground(FOREGROUND_SERVICE_ID, notifManager.buildNotification(id, this, cancelIntent))
        Log.d("CopyEventService", "service created.")
        return Service.START_NOT_STICKY
    }

    private fun listenToCopyEvents(id: UUID) {
        val sendDataController = SendDataController()
        sendDataController.subscribe(this)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            clipboard.primaryClip?.let {
                val content = it.getItemAt(0)
                if (content.text != null) {
                    sendDataController.sendStringData(id, content.text.toString())
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d("CopyEventService", "service destroyed.")
        isRunning = false
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("CopyEventService", "service removed.")
        super.onTaskRemoved(rootIntent)
    }

    override fun onSuccess(data: Bundle, type: APIManagerCallback.CallType) {
        Log.d("CopyEventService", "copied data from background")
    }

    override fun onFailure(data: Bundle, type: APIManagerCallback.CallType, t: Throwable?) {
        t?.printStackTrace()
        Log.d("CopyEventService", "failed to copy data from background")
    }

    companion object {
        const val FOREGROUND_SERVICE_ID = 42
        var isRunning = false
    }

}