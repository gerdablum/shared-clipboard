package de.alina.clipboard.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import de.alina.clipboard.app.view.MainActivity.Companion.CHANNEL_ID
import de.alina.clipboard.app.R
import de.alina.clipboard.app.client.ClipboardServerAPICallback
import de.alina.clipboard.app.client.SendDataController
import de.alina.clipboard.app.model.User.Companion.USER_KEY
import java.util.*

class CopyEventService: Service(), ClipboardServerAPICallback{


    private fun buildNotification(id: UUID): Notification {
        val cancelIntent = Intent(this, CancelServiceReceiver::class.java)
        cancelIntent.putExtra("blabla", id.toString())
        val pendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0)

        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_content_copy_black_24dp)
                .setContentTitle("SharedClipboard")
                .setContentText("Connected to computer")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_content_copy_black_24dp, getString(R.string.cancel_service),
                        pendingIntent)

                .build()
    }

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
        val sendDataController = SendDataController(this)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            clipboard.primaryClip?.let {
                val content = it.getItemAt(0)
                if (content.text != null) {
                    sendDataController.sendStringData(id, content.text.toString())
                }
            }
        }
        isRunning = true
        startForeground(FOREGROUND_SERVICE_ID, buildNotification(id))
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

    override fun onSuccess(data: Bundle, type: ClipboardServerAPICallback.CallType) {
        Log.d("CopyEventService", "copied data from background")
    }

    override fun onFailure(data: Bundle, type: ClipboardServerAPICallback.CallType, t: Throwable?) {
        t?.printStackTrace()
        Log.d("CopyEventService", "failed to copy data from background")
    }

    companion object {
        const val FOREGROUND_SERVICE_ID = 42
        var isRunning = false
    }

}