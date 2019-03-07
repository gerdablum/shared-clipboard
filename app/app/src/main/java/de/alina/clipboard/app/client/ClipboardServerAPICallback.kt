package de.alina.clipboard.app.client

import android.os.Bundle
interface ClipboardServerAPICallback {

    enum class CallType {
        ACKNOWLEDGE,
        SEND_DATA,
        SEND_FILE_DATA,
        GET_DATA,
        LOGOUT
    }

    fun onSuccess(data: Bundle, type: CallType)

    fun onFailure(data: Bundle, type: CallType, t: Throwable?)
}