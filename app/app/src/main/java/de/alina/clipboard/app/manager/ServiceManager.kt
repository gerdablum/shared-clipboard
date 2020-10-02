package de.alina.clipboard.app.manager

import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import de.alina.clipboard.app.model.User
import de.alina.clipboard.app.service.CopyEventService

open class ServiceManager {
    fun startCopyListenService(context: Context, user: User?) {
        //TODO only start service if not already running
        if (CopyEventService.isRunning) {
            return
        }
        val intent = Intent(context, CopyEventService::class.java)
        intent.putExtra(User.USER_KEY, user?.id.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopCopyListenService(context: Context, user: User?) {
        val intent = Intent(context, CopyEventService::class.java)
        intent.putExtra(User.USER_KEY, user?.id.toString())
        context.stopService(intent)
    }

    fun stopCopyListenService(context: Context) {
        val intent = Intent(context, CopyEventService::class.java)
        context.stopService(intent)
    }
}