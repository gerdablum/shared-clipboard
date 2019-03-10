package de.alina.clipboard.app.client

import android.os.Bundle
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class TestConnectionController(val apiCallback: ClipboardServerAPICallback): Callback<String?>, BaseApiController() {

    fun isConnected(id: UUID) {
        val call = apiString.testConnection(id.toString())
        call.enqueue(this)
        Log.d(this.toString(), "Requesting " + call.request().url())

    }
    override fun onFailure(call: Call<String?>, t: Throwable) {
        apiCallback.onFailure(Bundle(), ClipboardServerAPICallback.CallType.CONNECTION, t)
    }

    override fun onResponse(call: Call<String?>, response: Response<String?>) {
        if (response.isSuccessful == true) {
            if (response.body() == "true") {
                apiCallback.onSuccess(Bundle(), ClipboardServerAPICallback.CallType.CONNECTION)
            } else {
                Log.d("TestConnectionControlle", "Connection not longer alive.")
                apiCallback.onFailure(Bundle(), ClipboardServerAPICallback.CallType.CONNECTION, null)
            }
        } else {
            val data = Bundle()
            data.putInt(ClipboardServerAPICallback.CALLBACK_KEY_ERROR_CODE, response?.code() ?: 0)
            Log.d("TestConnectionControlle", "Server responded with response code " + response?.code())
            apiCallback.onFailure(data, ClipboardServerAPICallback.CallType.CONNECTION, null)
        }
    }
}