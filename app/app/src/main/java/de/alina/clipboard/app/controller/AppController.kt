package de.alina.clipboard.app.controller

import android.os.Bundle
import de.alina.clipboard.app.client.AcknowledgeController
import de.alina.clipboard.app.client.ClipboardServerAPICallback
import de.alina.clipboard.app.client.LogoutController

class AppController:  ClipboardServerAPICallback{

    private val ackController by lazy { AcknowledgeController(this)}
    private val logoutController by lazy { LogoutController(this)}


    fun activityCreated() {

    }

    fun fabButtonClicked() {

    }

    fun logoutButtonClicked() {

    }

    override fun onFailure(data: Bundle, type: ClipboardServerAPICallback.CallType, t: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onSuccess(data: Bundle, type: ClipboardServerAPICallback.CallType) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}