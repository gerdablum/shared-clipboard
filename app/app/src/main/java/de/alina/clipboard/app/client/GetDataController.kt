package de.alina.clipboard.app.client

import android.os.Bundle
import android.util.Log
import de.alina.clipboard.app.client.ClipboardServerAPICallback.Companion.CALLBACK_KEY_USER
import de.alina.clipboard.app.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*


class GetDataController(var apiCallback: ClipboardServerAPICallback): Callback<User?>, BaseApiController() {

    fun getData(id: UUID) {
        val call = apiJSON.getData(id.toString())
        call.enqueue(this)
        Log.d(this.toString(), "Requesting " + call.request().url())
    }
    override fun onFailure(call: Call<User?>, t: Throwable) {
        apiCallback.onFailure(Bundle(), ClipboardServerAPICallback.CallType.GET_DATA, t)
    }

    override fun onResponse(call: Call<User?>, response: Response<User?>) {
        if (response.isSuccessful) {
            val user = response.body()
            val data = Bundle()
            data.putSerializable(CALLBACK_KEY_USER, user)
            apiCallback.onSuccess(data, ClipboardServerAPICallback.CallType.GET_DATA)
        } else {
            Log.e("GetDataController", "Server responded with response code " + response.code())
            apiCallback.onFailure(Bundle(), ClipboardServerAPICallback.CallType.GET_DATA, null)
        }
    }
}