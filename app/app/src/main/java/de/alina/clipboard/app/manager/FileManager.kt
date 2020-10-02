package de.alina.clipboard.app.manager

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import java.io.InputStream

class FileManager {

    fun getBytes(inputStream: InputStream?): ByteArray {
        var byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024;
        var buffer = ByteArray(bufferSize);

        var len = inputStream?.read(buffer) ?: -1
        while (len != -1) {
            byteBuffer.write(buffer, 0, len);
            len = inputStream?.read(buffer) ?: -1
        }
        inputStream?.close()
        return byteBuffer.toByteArray();
    }

    fun getFilename(context: Context, fileUri: Uri): String? {
        val cursor: Cursor? = context.contentResolver.query( fileUri, null, null, null, null, null)

        val filename = cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (it.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))

            } else {
                "Unknown"
            }
        }
        return filename
    }
}