package de.alina.clipboard.app.model

import java.io.Serializable
import java.util.*

open class User( var id: UUID?): Serializable {
    var stringData: String? = null
    var fileUrl: String? = null
    var type: DataType? = null



    companion object {
        const val USER_KEY = "de.alina.clipboard.app.userKey"
    }



}