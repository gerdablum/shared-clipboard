package de.alina.clipboard.app.client

import de.alina.clipboard.app.controller.AppController
import okhttp3.MediaType
import java.util.*

interface APIManager {

    fun acknowledge(id: UUID)

    fun isConnected(id: UUID)

    fun getData(id: UUID)

    fun logout(id: UUID)

    fun sendStringData(id: UUID, stringData: String)

    fun sendFileData(id: UUID, bytes: ByteArray, mimeType: MediaType, filename: String)

    companion object {
        const val CALLBACK_ID_KEY = "de.alina.clipboard.app.callbackId"
        const val CALLBACK_KEY_ERROR_CODE = "de.alina.clipboard.app.callbackErrorCode"
        const val CALLBACK_KEY_USER= "de.alina.clipboard.app.callbackUser"
    }

    fun subscribe(observer: APIManagerCallback)
}