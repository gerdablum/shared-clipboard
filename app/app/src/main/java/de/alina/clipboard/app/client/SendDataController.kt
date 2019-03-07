package de.alina.clipboard.app.client

import com.google.gson.GsonBuilder
import de.alina.clipboard.app.client.ClipboardServerAPI.Companion.BASE_URL
import de.alina.clipboard.app.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class SendDataController: Callback<String?> {

    fun sendStringData(id: UUID, stringData: String) {
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        val clipboardAPI = retrofit.create(ClipboardServerAPI::class.java)
        val call = clipboardAPI.sendData(id.toString(), stringData)
        call.enqueue(this)
    }
    override fun onFailure(call: Call<String?>?, t: Throwable?) {
        // TODO
    }

    override fun onResponse(call: Call<String?>?, response: Response<String?>?) {
        // TODO
    }
}