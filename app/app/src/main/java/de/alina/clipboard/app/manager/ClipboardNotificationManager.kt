package de.alina.clipboard.app.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import de.alina.clipboard.app.R
import de.alina.clipboard.app.view.MainActivity
import java.util.*

open class ClipboardNotificationManager {

    fun createNotificationChannel(context: Context) {
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

    fun buildNotification(id: UUID, context: Context, cancelIntent: Intent): Notification {
        cancelIntent.putExtra( DISCONNECT_REQUESTED_KEY, true)
        val pendingIntent  = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(cancelIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
            return NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_content_copy_black_24dp)
                .setContentTitle("SharedClipboard")
                .setContentText("Connected to computer")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_content_copy_black_24dp, context.getString(R.string.cancel_service),
                        pendingIntent)

                .build()
    }

    companion object {
        const val DISCONNECT_REQUESTED_KEY = "de.alina.clipboard.app.disconnectRequested"
    }
}