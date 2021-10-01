package de.alina.clipboard.app.manager

import android.content.Context
import de.alina.clipboard.app.R

class ServerAddressManager {

    fun saveAddress(address: String, context: Context) {
        val editor = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
        editor.putString(context.getString(R.string.server_url_key), address)
        editor.apply()

    }

    fun getAddress(context: Context): String? {
        val sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        return sharedPref.getString(context.getString(R.string.server_url_key), "")
    }

    companion object {

    }
}