package de.alina.clipboard.app.client

import android.os.Bundle
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

class LogoutController(val apiCallback: ClipboardServerAPICallback): Callback<String?> {

    fun logout(id: UUID) {
        val retrofit = Retrofit.Builder().baseUrl(ClipboardServerAPI.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        val clipboardAPI = retrofit.create(ClipboardServerAPI::class.java)
        val call = clipboardAPI.logout(id.toString())
        call.enqueue(this)

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