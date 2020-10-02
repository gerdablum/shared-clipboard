package de.alina.clipboard.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import de.alina.clipboard.app.R
import de.alina.clipboard.app.client.ClipboardServerAPICallback
import de.alina.clipboard.app.client.LogoutController
import de.alina.clipboard.app.manager.AuthManager
import de.alina.clipboard.app.manager.ServiceManager
import java.util.*

class CancelServiceReceiver(): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceManager = ServiceManager()
        if (context == null) {
            Log.e("CancelServiceReceiver", "Failed to stop foreground service")
        } else {
            serviceManager.stopCopyListenService(context)
        }

    }

}