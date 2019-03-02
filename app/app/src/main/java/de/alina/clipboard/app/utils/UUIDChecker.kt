package de.alina.clipboard.app.utils

import de.alina.clipboard.app.callbacks.UUIDCheckerCallback
import java.lang.Exception
import java.util.*

class UUIDChecker {

    companion object {
        fun checkString(id: String, callback: UUIDCheckerCallback) {
            try {
                var id = UUID.fromString(id)
                callback.uuidValid(id)
            } catch (e: Exception) {
                callback.uuidInvalid()
            }

        }
    }
}