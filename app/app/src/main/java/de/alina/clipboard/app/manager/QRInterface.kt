package de.alina.clipboard.app.manager

interface QRInterface {
    fun onQRScanFinished(id: String?) = Unit
}