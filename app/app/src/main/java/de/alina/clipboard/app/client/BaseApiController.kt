package de.alina.clipboard.app.client

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

open class BaseApiController {
    private lateinit var retrofitString: Retrofit
    private lateinit var retrofitJson: Retrofit

    open val apiJSON: ClipboardServerAPI by lazy {
        retrofitJson.create(ClipboardServerAPI::class.java)
    }
    open val apiString: ClipboardServerAPI by lazy {
        retrofitString.create(ClipboardServerAPI::class.java)
    }

    init {
        retrofitString = Retrofit.Builder().baseUrl(ClipboardServerAPI.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        val gson = GsonBuilder().setLenient().create()
        retrofitJson = Retrofit.Builder().baseUrl(ClipboardServerAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
    }
}