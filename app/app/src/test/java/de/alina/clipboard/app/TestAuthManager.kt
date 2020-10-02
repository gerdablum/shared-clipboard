package de.alina.clipboard.app

import android.content.Context
import android.content.SharedPreferences
import de.alina.clipboard.app.manager.AuthManager
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*

class TestAuthManager {

    @Test
    fun testGetUserKey_ReturnNull() {
        val context = mock(Context::class.java)
        val sp = mock(SharedPreferences::class.java)
        `when`(context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE))
                .thenReturn(sp)
        `when`(sp.getString(context.getString(R.string.user_auth_id_key), "")).thenReturn("")

        val authManager = AuthManager()
        val user = authManager.getUserKey(context)

        assertNull(user)
    }

    @Test
    fun testGetUserKey_ReturnValidUser() {
        val id = UUID.randomUUID()
        val context = mock(Context::class.java)
        val sp = mock(SharedPreferences::class.java)
        `when`(context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE))
                .thenReturn(sp)
        `when`(sp.getString(context.getString(R.string.user_auth_id_key), "")).thenReturn(id.toString())

        val authManager = AuthManager()
        val user = authManager.getUserKey(context)

        assertEquals(user?.id, id)
    }

    @Test
    fun testIsUUIDValid_ReturnNull() {
        val authManager = AuthManager()
        val uuid = authManager.isUUIDValid("Kuchen")

        assertNull(uuid)
    }

    @Test
    fun testIsUUIDValid_ReturnValue() {
        val uuid = UUID.randomUUID().toString()
        val authManager = AuthManager()
        val testUUID = authManager.isUUIDValid(uuid)

        assertEquals(uuid, testUUID.toString())
    }
}