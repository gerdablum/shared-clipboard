package de.alina.clipboard.app.client

import de.alina.clipboard.app.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.io.File

interface ClipboardServerAPI {

    @GET("/acknowledge")
    @Headers("Accept: text/plain")
    fun acknowledge(@Query("id") id: String): Call<String?>

    @GET("/logout")
    @Headers("Accept: text/plain, application/json")
    fun logout(@Query("id") id: String): Call<String?>

    @GET("/connected")
    @Headers("Accept: text/plain")
    fun testConnection(@Query("id") id: String): Call<String?>

    @GET("/get-data")
    @Headers("Accept: application/json")
    fun getData(@Query("id")id: String): Call<User?>

    @POST("/send-data")
    @Headers("Accept: text/plain")
    fun sendData(@Header("id")id: String, @Header("data") stringData: String): Call<String?>

    @Multipart
    @POST("upload-data")
    @Headers("Accept: text/plain, application/json")
    fun uploadData(@Query("id")id: String, @Part file: MultipartBody.Part): Call<String?>



    companion object {
        const val BASE_URL = "http://100.64.2.32:8090"
        //const val BASE_URL = "http://192.168.11.106:8090/"
        //const val BASE_URL = "http://172.27.176.59:8090/"
    }
}