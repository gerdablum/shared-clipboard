package de.alina.clipboard.app.manager

import android.graphics.Bitmap
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage

open class QRManager {

    fun scanQRCode(bitmap: Bitmap): String? {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build()
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        var id: String? = ""
        detector.detectInImage(image).addOnSuccessListener {
            if (it.isEmpty()) {
                id = null
                return@addOnSuccessListener
            }
            for (firebaseBarcode in it) {
                val a = it[0].rawValue ?: ""
                id = a

            }
        }.addOnFailureListener {
            id = null
        }
        return id
    }
}