package de.alina.clipboard.app.client

import de.alina.clipboard.app.model.User
import retrofit2.Call
import retrofit2.http.*
import java.io.File

interface ClipboardServerAPI {

    @GET("/acknowledge")
    @Headers("Accept: text/plain")
    fun acknowledge(@Query("id") id: String): Call<String?>

    @GET("/logout")
    @Headers("Accept: text/plain")
    fun logout(@Query("id") id: String): Call<String?>

    @GET("/get-data")
    @Headers("Accept: application/json")
    fun getData(@Query("id")id: String): Call<User?>

    @POST("/send-data")
    @Headers("Accept: text/plain")
    fun sendData(@Header("id")id: String, @Header("data") stringData: String): Call<String?>

    @POST("upload-data")
    @Headers("Accept: text/plain")
    fun uploadData(@Header("id")id: String, @Header("file")file: File): Call<String?>

    companion object {
        const val BASE_URL = "http://100.64.2.32:8090"
    }
}