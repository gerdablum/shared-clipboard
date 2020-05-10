package de.alina.clipboard.app.client

import android.os.Bundle
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

class LogoutController(): Callback<String?>, BaseApiController() {

    fun logout(id: UUID) {
        val call = apiString.logout("clipboard.id=" + id.toString())
        call.enqueue(this)
        Log.d(this.toString(), "Requesting " + call.request().url())
    }
    override fun onFailure(call: Call<String?>?, t: Throwable?) {
        observers.forEach {
            it.onFailure(Bundle(), ClipboardServerAPICallback.CallType.LOGOUT, t)
        }
    }

    override fun onResponse(call: Call<String?>?, response: Response<String?>?) {
        if (response?.isSuccessful == true) {
            observers.forEach {
                it.onSuccess(Bundle(), ClipboardServerAPICallback.CallType.LOGOUT)
            }
        } else {
            Log.e("LogoutController", "Server responded with response code " + response?.code())
            observers.forEach {
                it.onFailure(Bundle(), ClipboardServerAPICallback.CallType.LOGOUT, null)
            }
        }
    }
}