package de.alina.clipboard.app

import android.app.Activity
import android.content.Context
import de.alina.clipboard.app.client.CheckConnectionController
import de.alina.clipboard.app.controller.AppController
import de.alina.clipboard.app.manager.AuthManager
import de.alina.clipboard.app.model.User
import de.alina.clipboard.app.view.BaseView
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.*

class TestAppController {

    @Test
    fun testOnResumeTriggerAPICall() {
        val controller = AppController(mock(Activity::class.java), mock(BaseView::class.java))
        val authManager = mock(AuthManager::class.java)
        val connectController = mock(CheckConnectionController::class.java)
        val context = mock(Activity::class.java)
        val user = User(UUID.randomUUID())
        Mockito.`when`(authManager.getUserKey(context)).thenReturn(user)
        Mockito.`when`(controller.hasInternetConnection()).thenReturn(true)
        verify(connectController.isConnected(user.id ?: UUID.randomUUID()))
    }
}