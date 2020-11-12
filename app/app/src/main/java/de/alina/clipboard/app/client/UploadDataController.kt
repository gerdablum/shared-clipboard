package de.alina.clipboard.app.client

import android.os.Bundle
import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class UploadDataController(): BaseApiController() {

    fun sendFileData(id: UUID, bytes: ByteArray, mimeType: MediaType, filename: String,
                     callback: Callback<String?>) {
        val file = RequestBody.create(mimeType, bytes)
        val part = MultipartBody.Part.createFormData("file", filename, file)
        val call = apiJSON.uploadData("clipboard.id=" + id.toString(), part)
        call.enqueue(callback)
        Log.d(this.toString(), "Requesting " + call.request().url())
    }
}