package de.alina.clipboard.app.manager

import android.content.Context
import android.os.Bundle
import de.alina.clipboard.app.R
import de.alina.clipboard.app.client.ClipboardServerAPICallback
import de.alina.clipboard.app.model.User
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.Exception
import java.util.*

open class AuthManager {

    fun getUserKey(context: Context): User? {
        val sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userID = sharedPref.getString(context.getString(R.string.user_auth_id_key), "") ?: ""
        if (userID != "") {
            return User(UUID.fromString(userID))
        } else {
            return null
        }
    }

    fun logoutUser(context: Context) {
        val editor = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
        editor.putString(context.getString(R.string.user_auth_id_key), "")
        editor.apply()
    }

    // TODO check if id is valid
    fun storeUserId(id: String?, context: Context): User? {
        id ?: return null
        val editor = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
        editor.putString(context.getString(R.string.user_auth_id_key), id.toString())
        editor.apply()
        return User(UUID.fromString(id))
    }
    fun isUUIDValid(id: String?): UUID? {
        try {
            val uuid = UUID.fromString(id)
            return uuid
        } catch (e: Exception) {
            return null
        }
    }
}