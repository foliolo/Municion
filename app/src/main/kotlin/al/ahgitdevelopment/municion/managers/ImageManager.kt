package al.ahgitdevelopment.municion.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para gestión de imágenes
 *
 * FASE 5: Managers - Optimización de imágenes
 * - Decode con inSampleSize (previene OutOfMemoryError)
 * - Compresión optimizada
 * - Upload a Firebase Storage
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Singleton
class ImageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseStorage: FirebaseStorage,
    private val crashlytics: FirebaseCrashlytics
) {

    /**
     * Optimiza y guarda imagen localmente
     * - Decodifica con inSampleSize para reducir memoria
     * - Comprime a JPEG con calidad 85%
     * - Libera bitmap después de guardar
     */
    suspend fun optimizeAndSaveImage(
        originalPath: String,
        targetWidth: Int = 800,
        targetHeight: Int = 800
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Decode bounds (sin cargar imagen en memoria)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(originalPath, options)

            // Step 2: Calcular inSampleSize
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)

            // Step 3: Decodificar imagen optimizada
            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeFile(originalPath, options)
                ?: return@withContext Result.failure(IllegalStateException("Failed to decode image"))

            // Step 4: Guardar versión optimizada
            val optimizedFile = File(context.cacheDir, "optimized_${System.currentTimeMillis()}.jpg")
            FileOutputStream(optimizedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            // Step 5: Liberar bitmap
            bitmap.recycle()

            Log.i("ImageManager", "Image optimized: ${optimizedFile.absolutePath}, size=${optimizedFile.length() / 1024}KB")

            Result.success(optimizedFile.absolutePath)
        } catch (e: Exception) {
            Log.e("ImageManager", "Error optimizing image", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Sube imagen a Firebase Storage
     */
    suspend fun uploadToFirebase(
        localPath: String,
        userId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(localPath)
            val fileName = file.name

            val ref = firebaseStorage.reference
                .child("images")
                .child(userId)
                .child(fileName)

            // Upload file
            ref.putFile(Uri.fromFile(file)).await()

            // Get download URL
            val downloadUrl = ref.downloadUrl.await().toString()

            Log.i("ImageManager", "Image uploaded to Firebase: $fileName")

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("ImageManager", "Error uploading image to Firebase", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Descarga imagen desde Firebase Storage
     */
    suspend fun downloadFromFirebase(
        downloadUrl: String,
        localFileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val localFile = File(context.cacheDir, localFileName)
            val ref = firebaseStorage.getReferenceFromUrl(downloadUrl)

            ref.getFile(localFile).await()

            Log.i("ImageManager", "Image downloaded from Firebase: $localFileName")

            Result.success(localFile.absolutePath)
        } catch (e: Exception) {
            Log.e("ImageManager", "Error downloading image from Firebase", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Calcula inSampleSize óptimo para reducir memoria
     * @return Power of 2 que reduce dimensiones manteniendo calidad aceptable
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Limpia imágenes optimizadas del cache
     */
    suspend fun clearOptimizedImagesCache(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir
            var deletedCount = 0

            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("optimized_") && file.extension == "jpg") {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }

            Log.i("ImageManager", "Cleared $deletedCount optimized images from cache")
            Result.success(deletedCount)
        } catch (e: Exception) {
            android.util.Log.e("ImageManager", "Error clearing cache", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}
