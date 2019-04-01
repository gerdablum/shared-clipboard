package de.alina.clipboard.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import de.alina.clipboard.app.R
import de.alina.clipboard.app.client.ClipboardServerAPICallback
import de.alina.clipboard.app.client.LogoutController
import java.util.*

class CancelServiceReceiver: BroadcastReceiver(), ClipboardServerAPICallback {

    private var context: Context? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getStringExtra("blabla")
        id?.let {
            val uuid = UUID.fromString(it)
            LogoutController(this).logout(uuid)
        }
        this.context = context

    }

    override fun onSuccess(data: Bundle, type: ClipboardServerAPICallback.CallType) {
        val editor = context?.getSharedPreferences(
                context?.getString(R.string.preference_file_key), Context.MODE_PRIVATE)?.edit()
        editor?.putString(context?.getString(R.string.user_auth_id_key), "")
        editor?.apply()
        val intent = Intent(context, CopyEventService::class.java)
        context?.stopService(intent)
    }

    override fun onFailure(data: Bundle, type: ClipboardServerAPICallback.CallType, t: Throwable?) {
        t?.printStackTrace()
        val intent = Intent(context, CopyEventService::class.java)
        context?.stopService(intent)
        Log.e("CancelServiceReceiver", "Could not log out.")
    }

}