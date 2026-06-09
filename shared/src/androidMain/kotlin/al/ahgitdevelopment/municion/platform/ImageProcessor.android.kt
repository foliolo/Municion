package al.ahgitdevelopment.municion.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

actual fun processImageForUpload(
    bytes: ByteArray,
    maxDimension: Int,
    quality: Int,
): ByteArray =
    try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

        val decodeOpts =
            BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
            }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
        if (decoded == null) {
            bytes
        } else {
            val oriented = decoded.applyExifOrientation(bytes)
            val scaled = oriented.scaleLongestSideTo(maxDimension)
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            if (scaled !== decoded) scaled.recycle()
            if (oriented !== decoded && oriented !== scaled) oriented.recycle()
            decoded.recycle()
            out.toByteArray()
        }
    } catch (e: Exception) {
        // Never block the upload on a processing failure — fall back to the original bytes.
        bytes
    }

/** Largest power-of-two sample size that keeps both dimensions >= [maxDimension]. */
private fun calculateInSampleSize(
    width: Int,
    height: Int,
    maxDimension: Int,
): Int {
    var sample = 1
    var w = width
    var h = height
    while (w / 2 >= maxDimension && h / 2 >= maxDimension) {
        w /= 2
        h /= 2
        sample *= 2
    }
    return sample
}

private fun Bitmap.applyExifOrientation(source: ByteArray): Bitmap {
    val orientation =
        ByteArrayInputStream(source).use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        else -> return this
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private fun Bitmap.scaleLongestSideTo(maxDimension: Int): Bitmap {
    val longest = maxOf(width, height)
    if (longest <= maxDimension) return this
    val ratio = maxDimension.toFloat() / longest
    return Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
}
