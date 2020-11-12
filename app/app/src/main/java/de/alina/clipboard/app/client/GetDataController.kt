package de.alina.clipboard.app.client

import android.os.Bundle
import android.util.Log
import de.alina.clipboard.app.client.APIManagerCallback.Companion.CALLBACK_KEY_USER
import de.alina.clipboard.app.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class GetDataController():  BaseApiController() {

    fun getData(id: UUID, callback: Callback<User?>) {
        val call = apiJSON.getData("clipboard.id=" + id.toString())
        call.enqueue(callback)
        Log.d(this.toString(), "Requesting " + call.request().url())
    }
}