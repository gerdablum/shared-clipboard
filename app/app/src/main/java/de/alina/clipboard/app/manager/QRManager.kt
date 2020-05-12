package de.alina.clipboard.app.manager

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage

open class QRManager {

    fun scanQRCode(bitmap: Bitmap, callback: QRInterface) {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build()
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        detector.detectInImage(image).addOnSuccessListener {
            if (it.isEmpty()) {
                callback.onQRScanFinished(null)
                return@addOnSuccessListener
            }
            for (firebaseBarcode in it) {
                val a = it[0].rawValue ?: ""
                callback.onQRScanFinished(a)
                Log.d("QRScanner", "successful")

            }
        }.addOnFailureListener {
            it.printStackTrace()
            callback.onQRScanFinished(null)

            Log.d("QRScanner", "failed")
        }
    }
}