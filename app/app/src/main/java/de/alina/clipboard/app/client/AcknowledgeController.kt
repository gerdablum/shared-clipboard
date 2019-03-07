package de.alina.clipboard.app.client

import android.os.Bundle
import android.util.Log
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

class AcknowledgeController(val apiCallback: ClipboardServerAPICallback): Callback<String?> {

    var id: UUID? = null

    fun acknowledge(id: UUID) {
        this.id = id
        val retrofit = Retrofit.Builder().baseUrl(ClipboardServerAPI.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        val clipboardAPI = retrofit.create(ClipboardServerAPI::class.java)
        val call = clipboardAPI.acknowledge(id.toString())
        call.enqueue(this)

    }
    override fun onFailure(call: Call<String?>?, t: Throwable?) {
        Log.d("AcknowledgeController", call?.request()?.toString())
        apiCallback.onFailure(Bundle(), ClipboardServerAPICallback.CallType.ACKNOWLEDGE, t)
    }

    override fun onResponse(call: Call<String?>?, response: Response<String?>?) {
        Log.d("AcknowledgeController", response?.toString())
        if (response?.isSuccessful == true) {
            val data = Bundle()
            data.putString(CALLBACK_ID_KEY, id?.toString())
            apiCallback.onSuccess(data, ClipboardServerAPICallback.CallType.ACKNOWLEDGE)

        } else {
            Log.d("AcknowledgeController", "Server responded with response code " + response?.code())
            val data = Bundle()
            data.putInt(CALLBACK_KEY_ERROR_CODE, response?.code() ?: 0)
            apiCallback.onFailure(data, ClipboardServerAPICallback.CallType.ACKNOWLEDGE, null)
        }
    }

    companion object {
        const val CALLBACK_ID_KEY = "de.alina.clipboard.app.callbackAcknowledge"
        const val CALLBACK_KEY_ERROR_CODE = "de.alina.clipboard.app.callbackAcknowledgeErrorCode"
    }
}