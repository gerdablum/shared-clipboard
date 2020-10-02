package de.alina.clipboard.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import de.alina.clipboard.app.client.*
import de.alina.clipboard.app.controller.AppController
import de.alina.clipboard.app.manager.*
import de.alina.clipboard.app.model.User
import de.alina.clipboard.app.view.BaseView
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.util.*

class TestAppController {
    private val ackController = mock(AcknowledgeController::class.java)
    private val logoutController = mock(LogoutController::class.java)
    private val connectController = mock(CheckConnectionController::class.java)
    private val uploadController = mock(UploadDataController::class.java)
    private val sendDataController = mock(SendDataController::class.java)
    private val authManager = mock(AuthManager::class.java)
    private val serviceManager = mock(ServiceManager::class.java)
    private val qrManager = mock(QRManager::class.java)
    private val notifManager = mock(ClipboardNotificationManager::class.java)
    private val fileManager = mock(FileManager::class.java)
    private val context = mock(Activity::class.java)
    private val baseView = mock(BaseView::class.java)
    private val controller = spy(AppController(context, baseView, ackController, logoutController,
            connectController, uploadController, sendDataController, authManager,
            serviceManager, qrManager, notifManager, fileManager))

    @Test
    fun testOnResume_VerifyIsConnected() {
        val user = User(UUID.randomUUID())
        doReturn(user).`when`(authManager).getUserKey(context)
        doReturn(true).`when`(controller).hasInternetConnection()

        controller.onResume()

        verify(connectController).isConnected(user.id!!)
    }

    @Test
    fun testOnResume_VerifyShowNoInternetConnection() {
        val user = User(UUID.randomUUID())
        doReturn(user).`when`(authManager).getUserKey(context)
        doReturn(false).`when`(controller).hasInternetConnection()

        controller.onResume()

        verify(baseView).showNoInternetConnection()
    }

    @Test
    fun testOnResume_VerifyShowLogoutSuccessful() {
        val user = null
        doReturn(user).`when`(authManager).getUserKey(context)
        doReturn(true).`when`(controller).hasInternetConnection()

        controller.onResume()

        verify(baseView).showLogoutSuccessful()
    }

    @Test
    fun testLogoutUser_VerifyLogout() {
        val user = User(UUID.randomUUID())
        controller.user = user

        controller.logoutUser()

        verify(logoutController).logout(user.id!!)
    }

    @Test
    fun testProcessImage_VerifyAcknowledge() {
        val data = mock(Intent::class.java)
        val bitmap = mock(Bitmap::class.java)
        val id = UUID.randomUUID().toString()
        doReturn(bitmap).`when`(data).extras?.get("data")
        doAnswer {
            val callback = it.arguments[1] as QRInterface
            callback.onQRScanFinished(id)

        }.`when`(qrManager.scanQRCode(bitmap, any(QRInterface::class.java)))

        controller.processImage(data)

        verify(baseView).showLoginFailure()
    }


}