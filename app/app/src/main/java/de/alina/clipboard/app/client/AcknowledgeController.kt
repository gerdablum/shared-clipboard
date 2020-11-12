package de.alina.clipboard.app.client

import android.util.Log
import de.alina.clipboard.app.client.APIManagerCallback.Companion.CALLBACK_ID_KEY
import de.alina.clipboard.app.client.APIManagerCallback.Companion.CALLBACK_KEY_ERROR_CODE
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AcknowledgeController(): BaseApiController() {

    var id: UUID? = null

    fun acknowledge(id: UUID, callback: Callback<String?>) {
        this.id = id
        val call = apiString.acknowledge("clipboard.id=" + id.toString())
        call.enqueue(callback)
        Log.d(this.toString(), call.request().headers().toString())
        Log.d(this.toString(), "Requesting " + call.request().url())
    }
}