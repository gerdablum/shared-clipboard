package de.alina.clipboard.app.model

import java.util.*

open class User( var id: UUID?) {
    var stringData: String? = null
    var fileUrl: String? = null
    var type: DataType? = null


}