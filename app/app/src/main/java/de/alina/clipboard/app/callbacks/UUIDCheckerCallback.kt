package de.alina.clipboard.app.callbacks

import java.util.*

interface UUIDCheckerCallback {
    fun uuidValid(uuid: UUID)
    fun uuidInvalid();
}