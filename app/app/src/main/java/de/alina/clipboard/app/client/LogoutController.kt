package de.alina.clipboard.app.client

import android.os.Bundle
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

class LogoutController(val apiCallback: ClipboardServerAPICallback): Callback<String?>, BaseApiController() {

    fun logout(id: UUID) {
        val call = apiString.logout(id.toString())
        call.enqueue(this)
        Log.d(this.toString(), "Requesting " + call.request().url())
    }
    override fun onFailure(call: Call<String?>?, t: Throwable?) {
        apiCallback.onFailure(Bundle(), ClipboardServerAPICallback.CallType.LOGOUT, t)
    }

    override fun onResponse(call: Call<String?>?, response: Response<String?>?) {
        if (response?.isSuccessful == true) {
            apiCallback.onSuccess(Bundle(), ClipboardServerAPICallback.CallType.LOGOUT)
        } else {
            Log.e("LogoutController", "Server responded with response code " + response?.code())
            apiCallback.onFailure(Bundle(), ClipboardServerAPICallback.CallType.LOGOUT, null)
        }
    }
}