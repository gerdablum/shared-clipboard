package de.alina.clipboard.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import de.alina.clipboard.app.manager.BackgroundServiceManager

class CancelServiceReceiver(): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceManager = BackgroundServiceManager()
        if (context == null) {
            Log.e("CancelServiceReceiver", "Failed to stop foreground service")
        } else {
            serviceManager.stopCopyListenService(context)
        }

    }

}