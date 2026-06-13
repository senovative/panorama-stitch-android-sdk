package io.senovative.panorama.sdk.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun Bitmap.rotateBy(degrees: Int): Bitmap {
    if (degrees == 0) return this
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

internal fun Bitmap.scaledCopyTo(maxEdge: Int): Bitmap {
    val edge = maxOf(width, height)
    if (edge <= maxEdge) return copy(Bitmap.Config.ARGB_8888, false)
    val scale = maxEdge.toFloat() / edge.toFloat()
    val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
    val scaledHeight = (height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(this, scaledWidth, scaledHeight, true)
}

internal fun saveBitmap(context: Context, bitmap: Bitmap): Uri {
    val name = "PANORAMA_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
    val file = File(context.cacheDir, name)
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
    return Uri.fromFile(file)
}
