package de.alina.clipboard.app.client

import android.os.Bundle
import android.text.Html
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SendDataController: BaseApiController(){

    fun sendStringData(id: UUID, stringData: String, callback: Callback<String?>) {
        val call = apiJSON.sendData("clipboard.id=" + id.toString(), Html.escapeHtml(stringData))
        call.enqueue(callback)
        Log.d(this.toString(), "Requesting " + call.request().url())
    }
}