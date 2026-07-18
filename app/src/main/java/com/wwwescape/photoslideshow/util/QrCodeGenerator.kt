package com.wwwescape.photoslideshow.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/** Encodes [text] as a QR code bitmap for display in Compose. Used only for the Google Photos
 * device-authorization verification URL — meant to be scanned by a phone, since a TV remote has
 * no practical way to type a URL. */
fun generateQrCodeBitmap(text: String, sizePx: Int = 512): ImageBitmap {
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx)
    val width = matrix.width
    val height = matrix.height
    // Fill one pixel array and upload it in a single call instead of one JNI setPixel per pixel.
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
        val row = y * width
        for (x in 0 until width) {
            pixels[row + x] = if (matrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
    }
    val bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.RGB_565)
    return bitmap.asImageBitmap()
}
