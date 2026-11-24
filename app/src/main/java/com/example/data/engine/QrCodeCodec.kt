package com.example.data.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeCodec {

    private val decodeHints: Map<DecodeHintType, Any> = mapOf(
        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
        DecodeHintType.TRY_HARDER to true,
        DecodeHintType.CHARACTER_SET to "UTF-8"
    )

    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
        if (content.isBlank() || sizePx <= 0) return null
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 2
            )
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val w = matrix.width
            val h = matrix.height
            val pixels = IntArray(w * h)
            val dark = AndroidColor.rgb(7, 14, 24)
            val light = AndroidColor.WHITE
            for (y in 0 until h) {
                val rowOffset = y * w
                for (x in 0 until w) {
                    pixels[rowOffset + x] = if (matrix.get(x, y)) dark else light
                }
            }
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, w, 0, 0, w, h)
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun generateBarcode128Bitmap(content: String, widthPx: Int = 420, heightPx: Int = 96): Bitmap? {
        val clean = content.trim()
        if (clean.isBlank() || widthPx <= 0 || heightPx <= 0) return null
        return try {
            val matrix = Code128Writer().encode(clean, BarcodeFormat.CODE_128, widthPx, heightPx)
            val w = matrix.width
            val h = matrix.height
            val pixels = IntArray(w * h)
            val dark = AndroidColor.rgb(7, 14, 24)
            val light = AndroidColor.WHITE
            for (y in 0 until h) {
                val rowOffset = y * w
                for (x in 0 until w) {
                    pixels[rowOffset + x] = if (matrix.get(x, y)) dark else light
                }
            }
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, w, 0, 0, w, h)
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun decodeQrFromImageProxy(imageProxy: ImageProxy): String? {
        return try {
            val planes = imageProxy.planes
            if (planes.isEmpty()) return null
            val width = imageProxy.width
            val height = imageProxy.height
            if (width <= 0 || height <= 0) return null

            val yPlane = planes[0]
            val buffer = yPlane.buffer ?: return null
            val rowStride = yPlane.rowStride
            val pixelStride = yPlane.pixelStride
            val data = ByteArray(width * height)

            buffer.rewind()
            if (pixelStride == 1 && rowStride == width) {
                buffer.get(data, 0, data.size)
            } else {
                val rowBuffer = ByteArray(rowStride)
                for (row in 0 until height) {
                    val remaining = buffer.remaining()
                    if (remaining <= 0) break
                    val toRead = minOf(rowStride, remaining)
                    buffer.get(rowBuffer, 0, toRead)
                    val dstOffset = row * width
                    if (pixelStride == 1) {
                        val copyLen = minOf(width, toRead)
                        System.arraycopy(rowBuffer, 0, data, dstOffset, copyLen)
                    } else {
                        for (col in 0 until width) {
                            val srcIdx = col * pixelStride
                            if (srcIdx < toRead) {
                                data[dstOffset + col] = rowBuffer[srcIdx]
                            }
                        }
                    }
                }
            }

            val source = PlanarYUVLuminanceSource(
                data,
                width,
                height,
                0,
                0,
                width,
                height,
                false
            )
            decodeLuminanceSource(source)
        } catch (_: Throwable) {
            null
        }
    }

    fun decodeQrFromBitmap(bitmap: Bitmap): String? {
        return try {
            val srcWidth = bitmap.width
            val srcHeight = bitmap.height
            if (srcWidth <= 0 || srcHeight <= 0) return null

            val maxDim = 1280
            val workingBitmap = if (srcWidth > maxDim || srcHeight > maxDim) {
                val scale = minOf(maxDim.toFloat() / srcWidth, maxDim.toFloat() / srcHeight)
                val targetW = (srcWidth * scale).toInt().coerceAtLeast(1)
                val targetH = (srcHeight * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }

            try {
                val w = workingBitmap.width
                val h = workingBitmap.height
                val pixels = IntArray(w * h)
                workingBitmap.getPixels(pixels, 0, w, 0, 0, w, h)
                val source = RGBLuminanceSource(w, h, pixels)
                decodeLuminanceSource(source)
            } finally {
                if (workingBitmap !== bitmap && !workingBitmap.isRecycled) {
                    workingBitmap.recycle()
                }
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun decodeQrFromUri(context: Context, uri: Uri): String? {
        var decoded: Bitmap? = null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sampleSize = 1
            val maxDim = 1400
            while ((bounds.outWidth / sampleSize) > maxDim || (bounds.outHeight / sampleSize) > maxDim) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            }
            decoded?.let { decodeQrFromBitmap(it) }
        } catch (_: Throwable) {
            null
        } finally {
            decoded?.let {
                if (!it.isRecycled) it.recycle()
            }
        }
    }

    private fun decodeLuminanceSource(source: LuminanceSource): String? {
        val reader = MultiFormatReader().apply { setHints(decodeHints) }
        val candidates = listOf(
            BinaryBitmap(HybridBinarizer(source)),
            BinaryBitmap(GlobalHistogramBinarizer(source)),
            BinaryBitmap(HybridBinarizer(source.invert()))
        )
        for (candidate in candidates) {
            try {
                val result = reader.decodeWithState(candidate)
                val text = result.text?.trim()
                if (!text.isNullOrEmpty()) return text
            } catch (_: Throwable) {
                // Continue trying next binarizer
            } finally {
                reader.reset()
            }
        }
        return null
    }
}
