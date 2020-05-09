package de.alina.clipboard.app.client

import android.net.Uri
import android.os.Bundle
import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.util.*


class UploadDataController(var apiCallback: ClipboardServerAPICallback): Callback<String?>, BaseApiController() {

    fun sendFileData(id: UUID, bytes: ByteArray, mimeType: MediaType, filename: String) {
        val file = RequestBody.create(mimeType, bytes)
        val part = MultipartBody.Part.createFormData("file", filename, file)
        val call = apiJSON.uploadData("clipboard.id=" + id.toString(), part)
        call.enqueue(this)
        Log.d(this.toString(), "Requesting " + call.request().url())
    }
    override fun onFailure(call: Call<String?>, t: Throwable) {
        apiCallback.onFailure(Bundle(), ClipboardServerAPICallback.CallType.SEND_FILE_DATA, t)
    }

    override fun onResponse(call: Call<String?>, response: Response<String?>) {
        if (response.isSuccessful) {
            apiCallback.onSuccess(Bundle(), ClipboardServerAPICallback.CallType.SEND_FILE_DATA)
        } else {
            Log.e("UploadDataController", "Server responded with response code " + response.code())
            apiCallback.onFailure(Bundle(), ClipboardServerAPICallback.CallType.SEND_FILE_DATA, null)
        }
    }
}