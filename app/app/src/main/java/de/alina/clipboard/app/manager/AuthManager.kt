package de.alina.clipboard.app.manager

import android.content.Context
import android.os.Bundle
import de.alina.clipboard.app.R
import de.alina.clipboard.app.client.ClipboardServerAPICallback
import de.alina.clipboard.app.model.User
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
            USER_LOGGED_IN = false
            return null
        }
    }

    fun logoutUser(context: Context) {
        val editor = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
        editor.putString(context.getString(R.string.user_auth_id_key), "")
        editor.apply()
        USER_LOGGED_IN = false
    }

    fun getUserIDfromServerAndStore(data: Bundle, context: Context): User? {
        var user: User? = null
        data.getString(ClipboardServerAPICallback.CALLBACK_ID_KEY)?.let {
            user = User(UUID.fromString(it))
            val editor = context.getSharedPreferences(
                    context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
            editor.putString(context.getString(R.string.user_auth_id_key), it)
            editor.apply()
            USER_LOGGED_IN = true
        }
        return user
    }
    fun isUUIDValid(id: String?): UUID? {
        try {
            val uuid = UUID.fromString(id)
            return uuid
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        var USER_LOGGED_IN = false
    }
}