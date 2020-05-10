package de.alina.clipboard.app.controller

import android.os.Bundle
import de.alina.clipboard.app.client.ClipboardServerAPICallback

class ClipboardCallback: ClipboardServerAPICallback {
    override fun onSuccess(data: Bundle, type: ClipboardServerAPICallback.CallType) {

    }

    override fun onFailure(data: Bundle, type: ClipboardServerAPICallback.CallType, t: Throwable?) {

    }
}