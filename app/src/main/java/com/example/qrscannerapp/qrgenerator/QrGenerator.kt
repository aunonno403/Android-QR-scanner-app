package com.example.qrscannerapp.qrgenerator

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrGenerator {
    data class Options(
        val size: Int = 1024,
        val margin: Int = 1,
        val foreground: Int = 0xFF000000.toInt(),
        val background: Int = 0xFFFFFFFF.toInt(),
        val characterSet: String = "UTF-8",
        val errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.M
    )

    fun generate(text: String, options: Options = Options()): Bitmap {
        require(text.isNotBlank()) { "Text to encode must not be blank" }
        val hints: MutableMap<EncodeHintType, Any> = mutableMapOf(
            EncodeHintType.MARGIN to options.margin,
            EncodeHintType.CHARACTER_SET to options.characterSet,
            EncodeHintType.ERROR_CORRECTION to options.errorCorrection
        )
        val writer = QRCodeWriter()
        val bitMatrix: BitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, options.size, options.size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) options.foreground else options.background
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
