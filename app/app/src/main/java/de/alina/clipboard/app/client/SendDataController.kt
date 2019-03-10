package de.alina.clipboard.app.client

import android.os.Bundle
import android.text.Html
import android.util.Log
import com.google.gson.GsonBuilder
import de.alina.clipboard.app.client.ClipboardServerAPI.Companion.BASE_URL
import de.alina.clipboard.app.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class SendDataController(val apiCallback: ClipboardServerAPICallback): Callback<String?>, BaseApiController(){

    fun sendStringData(id: UUID, stringData: String) {
        val call = apiJSON.sendData(id.toString(), Html.escapeHtml(stringData))
        call.enqueue(this)
        Log.d(this.toString(), "Requesting " + call.request().url())
    }
    override fun onFailure(call: Call<String?>?, t: Throwable?) {
        apiCallback.onFailure(Bundle(), ClipboardServerAPICallback.CallType.SEND_DATA, t)
    }

    override fun onResponse(call: Call<String?>?, response: Response<String?>?) {
        if (response?.isSuccessful == true) {
            apiCallback.onSuccess(Bundle(), ClipboardServerAPICallback.CallType.SEND_DATA)
        } else {
            val data = Bundle()
            data.putInt(ClipboardServerAPICallback.CALLBACK_KEY_ERROR_CODE, response?.code() ?: 0)
            Log.e("SendDataController", "Server responded with response code" + response?.code())
            apiCallback.onFailure(data, ClipboardServerAPICallback.CallType.SEND_DATA, null)
        }
    }
}