package de.alina.clipboard.app.client

import android.os.Bundle
import android.util.Log
import de.alina.clipboard.app.client.APIManager.Companion.CALLBACK_ID_KEY
import de.alina.clipboard.app.client.APIManager.Companion.CALLBACK_KEY_ERROR_CODE
import de.alina.clipboard.app.client.APIManager.Companion.CALLBACK_KEY_USER
import de.alina.clipboard.app.model.User
import okhttp3.MediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class APIManagerImpl: APIManager {

    private val ackController = AcknowledgeController()
    private val connectionController = CheckConnectionController()
    private val getDataController = GetDataController()
    private val logoutController = LogoutController()
    private val sendDataController = SendDataController()
    private val uploadDataController = UploadDataController();

    val observers: MutableList<APIManagerCallback> = mutableListOf()

    override fun subscribe(observer: APIManagerCallback) {
        observers.add(observer)
    }

    override fun acknowledge(id: UUID) {
        ackController.acknowledge(id, object: Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                for (observer in observers) {
                    observer.onFailure(Bundle(), APIManagerCallback.CallType.ACKNOWLEDGE, t)
                }
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    val data = Bundle()
                    data.putString(CALLBACK_ID_KEY, id.toString())
                    for (observer in observers) {
                        observer.onSuccess(data, APIManagerCallback.CallType.ACKNOWLEDGE)
                    }

                } else {
                    respondToError(response.code(), "AcknowledgeController",
                            APIManagerCallback.CallType.ACKNOWLEDGE)

                }
            }
        })
    }

    override fun isConnected(id: UUID) {
        connectionController.isConnected(id, object :Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                observers.forEach() {
                    it.onFailure(Bundle(), APIManagerCallback.CallType.CONNECTION, t)
                }
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    if (response.body() == "true") {
                        observers.forEach() {
                            it.onSuccess(Bundle(), APIManagerCallback.CallType.CONNECTION)
                        }
                    } else {
                        Log.d("TestConnectionControlle", "Connection not longer alive.")
                        observers.forEach() {
                            it.onFailure(Bundle(), APIManagerCallback.CallType.CONNECTION, null)
                        }
                    }
                } else {
                    respondToError(response.code(), "CheckConnectionControlle",
                            APIManagerCallback.CallType.CONNECTION)
                }
            }

        })
    }

    override fun getData(id: UUID) {
        getDataController.getData(id, object: Callback<User?> {
            override fun onFailure(call: Call<User?>, t: Throwable) {
                observers.forEach() {
                    it.onFailure(Bundle(), APIManagerCallback.CallType.GET_DATA, t)
                }
            }

            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    val data = Bundle()
                    data.putSerializable(CALLBACK_KEY_USER, user)
                    observers.forEach {
                        it.onSuccess(data, APIManagerCallback.CallType.GET_DATA)
                    }
                } else {
                    respondToError(response.code(), "GetDataController", APIManagerCallback.CallType.GET_DATA)
                }
            }
        })
    }

    override fun logout(id: UUID) {
        logoutController.logout(id, object : Callback<String?> {

            override fun onFailure(call: Call<String?>, t: Throwable) {
                handleOnFailure(APIManagerCallback.CallType.GET_DATA, t)
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    observers.forEach {
                        it.onSuccess(Bundle(), APIManagerCallback.CallType.GET_DATA)
                    }
                } else {
                    respondToError(response.code(), "LogoutController", APIManagerCallback.CallType.LOGOUT)
                }
            }
        })
    }

    override fun sendStringData(id: UUID, stringData: String) {
        sendDataController.sendStringData(id, stringData, object : Callback<String?> {

            override fun onFailure(call: Call<String?>, t: Throwable) {
                handleOnFailure(APIManagerCallback.CallType.SEND_DATA, t)
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    observers.forEach {
                        it.onSuccess(Bundle(), APIManagerCallback.CallType.SEND_DATA)
                    }
                } else {
                    respondToError(response.code(), "SendDataController", APIManagerCallback.CallType.SEND_DATA)
                }
            }
        })
    }

    override fun sendFileData(id: UUID, bytes: ByteArray, mimeType: MediaType, filename: String) {
        uploadDataController.sendFileData(id, bytes, mimeType, filename, object : Callback<String?> {

            override fun onFailure(call: Call<String?>, t: Throwable) {
                handleOnFailure(APIManagerCallback.CallType.SEND_FILE_DATA, t)
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    observers.forEach {
                        it.onSuccess(Bundle(), APIManagerCallback.CallType.SEND_DATA)
                    }
                } else {
                    respondToError(response.code(), "SendFileDataController", APIManagerCallback.CallType.SEND_FILE_DATA)
                }
            }
        })
    }

    private fun respondToError(responseCode: Int, className: String, type: APIManagerCallback.CallType) {
        Log.e(className, "Server responded with response code " + responseCode)
        val data = Bundle()
        data.putInt(CALLBACK_KEY_ERROR_CODE, responseCode)
        handleOnFailure(type, null)

    }

    private fun handleOnFailure(type: APIManagerCallback.CallType, t: Throwable?) {
        observers.forEach {
            it.onFailure(Bundle(), type, t)
        }
    }

}