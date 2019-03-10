package de.alina.clipboard.app.client

import android.os.Bundle
interface ClipboardServerAPICallback {

    enum class CallType {
        ACKNOWLEDGE,
        SEND_DATA,
        SEND_FILE_DATA,
        GET_DATA,
        LOGOUT,
        CONNECTION
    }

    fun onSuccess(data: Bundle, type: CallType)

    fun onFailure(data: Bundle, type: CallType, t: Throwable?)

    companion object {
        const val CALLBACK_ID_KEY = "de.alina.clipboard.app.callbackId"
        const val CALLBACK_KEY_ERROR_CODE = "de.alina.clipboard.app.callbackErrorCode"
    }
}