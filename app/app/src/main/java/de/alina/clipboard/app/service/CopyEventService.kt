package de.alina.clipboard.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.util.Log
import de.alina.clipboard.app.view.MainActivity.Companion.CHANNEL_ID
import de.alina.clipboard.app.R
import de.alina.clipboard.app.client.ClipboardServerAPICallback
import de.alina.clipboard.app.client.SendDataController
import de.alina.clipboard.app.manager.ClipboardNotificationManager
import de.alina.clipboard.app.model.User.Companion.USER_KEY
import java.util.*

class CopyEventService(private var callback: ServiceCallback): Service() {
    lateinit var notifManager: ClipboardNotificationManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        isRunning = false
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val idString = intent.getStringExtra(USER_KEY)
        val id = UUID.fromString(idString)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            clipboard.primaryClip?.let {
                val content = it.getItemAt(0)
                if (content.text != null) {
                    callback.onCopyEvent(id, content.text.toString())
                }
            }
        }
        isRunning = true
        val cancelIntent = Intent(this, CancelServiceReceiver::class.java)
        startForeground(FOREGROUND_SERVICE_ID, notifManager.buildNotification(id, this, cancelIntent))
        Log.d("CopyEventService", "service created.")
        return Service.START_NOT_STICKY
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


    companion object {
        const val FOREGROUND_SERVICE_ID = 42
        var isRunning = false
    }

}