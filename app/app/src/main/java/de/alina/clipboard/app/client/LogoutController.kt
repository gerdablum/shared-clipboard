package de.alina.clipboard.app.client

import android.os.Bundle
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LogoutController(): BaseApiController() {

    fun logout(id: UUID, callback: Callback<String?>) {
        val call = apiString.logout("clipboard.id=" + id.toString())
        call.enqueue(callback)
        Log.d(this.toString(), "Requesting " + call.request().url())
    }
}