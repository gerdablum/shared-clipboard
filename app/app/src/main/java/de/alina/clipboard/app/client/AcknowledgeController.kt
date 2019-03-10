package de.alina.clipboard.app.client

import android.os.Bundle
import android.util.Log
import com.google.gson.GsonBuilder
import de.alina.clipboard.app.client.ClipboardServerAPICallback.Companion.CALLBACK_ID_KEY
import de.alina.clipboard.app.client.ClipboardServerAPICallback.Companion.CALLBACK_KEY_ERROR_CODE
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

class AcknowledgeController(val apiCallback: ClipboardServerAPICallback): Callback<String?>, BaseApiController() {

    var id: UUID? = null

    fun acknowledge(id: UUID) {
        this.id = id
        val call = apiString.acknowledge(id.toString())
        call.enqueue(this)
        Log.d(this.toString(), "Requesting " + call.request().url())
    }
    override fun onFailure(call: Call<String?>?, t: Throwable?) {
        apiCallback.onFailure(Bundle(), ClipboardServerAPICallback.CallType.ACKNOWLEDGE, t)
    }

    override fun onResponse(call: Call<String?>?, response: Response<String?>?) {
        if (response?.isSuccessful == true) {
            val data = Bundle()
            data.putString(CALLBACK_ID_KEY, id?.toString())
            apiCallback.onSuccess(data, ClipboardServerAPICallback.CallType.ACKNOWLEDGE)

        } else {
            Log.e("AcknowledgeController", "Server responded with response code " + response?.code())
            val data = Bundle()
            data.putInt(CALLBACK_KEY_ERROR_CODE, response?.code() ?: 0)
            apiCallback.onFailure(data, ClipboardServerAPICallback.CallType.ACKNOWLEDGE, null)
        }
    }
}