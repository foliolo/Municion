package al.ahgitdevelopment.municion.data.repository

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
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gestionar imágenes en Firebase Storage
 *
 * Estructura de almacenamiento: v3_userdata/{userId}/armas/{armaId}.jpg
 *
 * @since v3.2.2 (Image Upload Feature)
 */
interface ImageRepository {
    /**
     * Sube una imagen a Firebase Storage
     *
     * @param uri Uri local del archivo de imagen
     * @param userId ID del usuario autenticado
     * @param armaId ID único del arma (Room ID o UUID)
     * @param compressQuality Calidad de compresión JPEG (0-100). Por defecto 80.
     * @return Result con la URL de descarga pública y el storagePath
     */
    suspend fun uploadGuiaImage(
        uri: Uri,
        userId: String,
        armaId: String,
        compressQuality: Int = 80
    ): Result<ImageUploadResult>

    /**
     * Elimina una imagen de Firebase Storage
     *
     * @param storagePath Ruta completa en Storage (ej: v3_userdata/{userId}/armas/{armaId}.jpg)
     * @return Result indicando éxito o fallo
     */
    suspend fun deleteGuiaImage(storagePath: String): Result<Unit>

    /**
     * Obtiene la URL de descarga de una imagen existente
     *
     * @param storagePath Ruta completa en Storage
     * @return Result con la URL de descarga
     */
    suspend fun getDownloadUrl(storagePath: String): Result<String>
}

/**
 * Resultado de subir una imagen
 *
 * @param downloadUrl URL pública de descarga (https://firebasestorage...)
 * @param storagePath Ruta en Storage para facilitar el borrado futuro
 */
data class ImageUploadResult(
    val downloadUrl: String,
    val storagePath: String
)

/**
 * Implementación de ImageRepository usando Firebase Storage
 */
@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseStorage: FirebaseStorage,
    private val crashlytics: FirebaseCrashlytics
) : ImageRepository {

    companion object {
        private const val TAG = "ImageRepository"
        private const val STORAGE_VERSION = "v3_userdata"
        private const val ARMAS_FOLDER = "armas"
        private const val MAX_IMAGE_DIMENSION = 1920 // Máximo ancho/alto para redimensionar
        private const val IMAGE_EXTENSION = ".jpg"
    }

    override suspend fun uploadGuiaImage(
        uri: Uri,
        userId: String,
        armaId: String,
        compressQuality: Int
    ): Result<ImageUploadResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting upload for armaId=$armaId, userId=$userId")

            // 1. Comprimir imagen
            val compressedBytes = compressImage(uri, compressQuality)
                ?: return@withContext Result.failure(
                    IllegalStateException("No se pudo procesar la imagen")
                )

            Log.d(TAG, "Image compressed: ${compressedBytes.size} bytes")

            // 2. Construir ruta de almacenamiento
            val storagePath = buildStoragePath(userId, armaId)
            val storageRef = firebaseStorage.reference.child(storagePath)

            Log.d(TAG, "Uploading to: $storagePath")

            // 3. Subir archivo
            val uploadTask = storageRef.putBytes(compressedBytes)
            uploadTask.await()

            Log.d(TAG, "Upload completed")

            // 4. Obtener URL de descarga
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Log.i(TAG, "Image uploaded successfully: $downloadUrl")

            Result.success(
                ImageUploadResult(
                    downloadUrl = downloadUrl,
                    storagePath = storagePath
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}", e)
            crashlytics.log("Image upload failed for armaId=$armaId: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun deleteGuiaImage(storagePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (storagePath.isBlank()) {
                Log.w(TAG, "Attempted to delete with blank storagePath")
                return@withContext Result.success(Unit)
            }

            Log.d(TAG, "Deleting image at: $storagePath")

            val storageRef = firebaseStorage.reference.child(storagePath)
            storageRef.delete().await()

            Log.i(TAG, "Image deleted successfully: $storagePath")

            Result.success(Unit)
        } catch (e: Exception) {
            // Si el archivo no existe, considerarlo como éxito
            if (e.message?.contains("does not exist") == true) {
                Log.w(TAG, "Image already deleted or doesn't exist: $storagePath")
                return@withContext Result.success(Unit)
            }

            Log.e(TAG, "Delete failed: ${e.message}", e)
            crashlytics.log("Image delete failed for path=$storagePath: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun getDownloadUrl(storagePath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (storagePath.isBlank()) {
                return@withContext Result.failure(
                    IllegalArgumentException("storagePath cannot be blank")
                )
            }

            val storageRef = firebaseStorage.reference.child(storagePath)
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get download URL: ${e.message}", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Comprime y redimensiona la imagen para optimizar el almacenamiento
     *
     * @param uri Uri de la imagen original
     * @param quality Calidad de compresión JPEG (0-100)
     * @return ByteArray de la imagen comprimida o null si falla
     */
    private fun compressImage(uri: Uri, quality: Int): ByteArray? {
        return try {
            // Leer dimensiones sin cargar la imagen completa
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            val originalWidth = options.outWidth
            val originalHeight = options.outHeight

            if (originalWidth <= 0 || originalHeight <= 0) {
                Log.e(TAG, "Invalid image dimensions: ${originalWidth}x$originalHeight")
                return null
            }

            // Calcular factor de escala
            val scaleFactor = calculateScaleFactor(originalWidth, originalHeight)

            // Decodificar con escala
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scaleFactor
            }

            val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return null

            // Comprimir a JPEG
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            bitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Image compression failed: ${e.message}", e)
            null
        }
    }

    /**
     * Calcula el factor de escala para redimensionar la imagen
     */
    private fun calculateScaleFactor(width: Int, height: Int): Int {
        var scaleFactor = 1
        val maxDimension = maxOf(width, height)

        while (maxDimension / scaleFactor > MAX_IMAGE_DIMENSION) {
            scaleFactor *= 2
        }

        return scaleFactor
    }

    /**
     * Construye la ruta de almacenamiento según la especificación
     *
     * Formato: v3_userdata/{userId}/armas/{armaId}.jpg
     */
    private fun buildStoragePath(userId: String, armaId: String): String {
        return "$STORAGE_VERSION/$userId/$ARMAS_FOLDER/$armaId$IMAGE_EXTENSION"
    }
}
