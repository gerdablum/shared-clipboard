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

    private var user: User? = null

    fun logoutUser(context: Context) {
        user = null
        val editor = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
        editor.putBoolean(context.getString(R.string.user_auth_request_logout_key), true)
        editor.apply()
    }

    fun isUUIDValid(id: String?): UUID? {
        try {
            val uuid = UUID.fromString(id)
            return uuid
        } catch (e: Exception) {
            return null
        }
    }

    // user zurückgeben wenn logout requested???
    fun getActiveUser(context: Context): User? {
        if (user != null) return user;
        val sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val userID = sharedPref.getString(context.getString(R.string.user_auth_id_key), "") ?: ""
        if (userID != "") {
            user = User(UUID.fromString(userID))
            return user
        } else {
            return null
        }
    }

    fun logoutRequested(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        return sharedPref.getBoolean(context.getString(R.string.user_auth_request_logout_key), false)
    }

    fun deleteUserData(context: Context) {
        val editor = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
        editor.putString(context.getString(R.string.user_auth_id_key), "")
        editor.apply()
    }

    fun storeUser(id: String, context: Context) {
        val editor = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
        editor.putString(context.getString(R.string.user_auth_id_key), id)
        editor.putBoolean(context.getString(R.string.user_auth_request_logout_key), false)
        editor.apply()
        user = User(UUID.fromString(id))
    }
}