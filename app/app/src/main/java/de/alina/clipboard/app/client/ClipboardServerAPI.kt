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
    fun acknowledge(@Header("Cookie") id: String): Call<String?>

    @GET("/logout")
    @Headers("Accept: text/plain, application/json")
    fun logout(@Header("Cookie") id: String): Call<String?>

    @GET("/connected")
    @Headers("Accept: text/plain")
    fun testConnection(@Header("Cookie") id: String): Call<String?>

    @GET("/get-data")
    @Headers("Accept: application/json")
    fun getData(@Header("Cookie")id: String): Call<User?>

    @POST("/send-data")
    @Headers("Accept: text/plain")
    fun sendData(@Header("Cookie")id: String, @Header("data") stringData: String): Call<String?>

    @Multipart
    @POST("upload-data")
    @Headers("Accept: text/plain, application/json")
    fun uploadData(@Header("Cookie")id: String, @Part file: MultipartBody.Part): Call<String?>



    companion object {
        const val BASE_URL = "http://100.64.3.19:8090"
        //const val BASE_URL = "http://192.168.1.45:8090/"
        //const val BASE_URL = "http://172.27.176.59:8090/"
    }
}