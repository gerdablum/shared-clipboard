package de.alina.clipboard.app.model

import java.util.*

class UserFileData(id: UUID): User(id) {
    var base64: String? = null
    var mimeType: String? = null
    var originalFilename: String? = null
}