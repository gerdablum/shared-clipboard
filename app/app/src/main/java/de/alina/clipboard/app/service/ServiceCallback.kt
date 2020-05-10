package de.alina.clipboard.app.service

import java.util.*

interface ServiceCallback {
    fun performLogout(uuid: UUID)
    fun onCopyEvent(uuid: UUID, text: String)
}