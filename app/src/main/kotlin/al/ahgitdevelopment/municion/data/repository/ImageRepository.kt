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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gestionar imágenes en Firebase Storage
 *
 * Estructura de almacenamiento: 
 * - Armas: v3_userdata/{userId}/armas/{armaId}_{timestamp}.jpg
 * - Licencias: v3_userdata/{userId}/licencias/{licenciaId}_{timestamp}.jpg
 * - Compras: v3_userdata/{userId}/compras/{compraId}_{timestamp}.jpg
 *
 * El timestamp tiene formato: yyyyMMdd_HHmmss (ej: 20251217_220315)
 * Ejemplo completo: v3_userdata/abc123/armas/5_20251217_220315.jpg
 *
 * @since v3.2.2 (Image Upload Feature)
 * @since v3.2.2 (Extended for Licencias)
 * @since v3.2.3 (Extended for Compras)
 * @since v3.2.3 (Improved naming with timestamp)
 */
interface ImageRepository {
    /**
     * Sube una imagen de arma a Firebase Storage
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
     * Elimina una imagen de arma de Firebase Storage
     *
     * @param storagePath Ruta completa en Storage (ej: v3_userdata/{userId}/armas/{armaId}.jpg)
     * @return Result indicando éxito o fallo
     */
    suspend fun deleteGuiaImage(storagePath: String): Result<Unit>

    /**
     * Sube una imagen de licencia a Firebase Storage
     *
     * @param uri Uri local del archivo de imagen
     * @param userId ID del usuario autenticado
     * @param licenciaId ID único de la licencia
     * @param compressQuality Calidad de compresión JPEG (0-100). Por defecto 80.
     * @return Result con la URL de descarga pública y el storagePath
     */
    suspend fun uploadLicenciaImage(
        uri: Uri,
        userId: String,
        licenciaId: String,
        compressQuality: Int = 80
    ): Result<ImageUploadResult>

    /**
     * Elimina una imagen de licencia de Firebase Storage
     *
     * @param storagePath Ruta completa en Storage
     * @return Result indicando éxito o fallo
     */
    suspend fun deleteLicenciaImage(storagePath: String): Result<Unit>

    /**
     * Sube una imagen de compra a Firebase Storage
     *
     * @param uri Uri local del archivo de imagen
     * @param userId ID del usuario autenticado
     * @param compraId ID único de la compra
     * @param compressQuality Calidad de compresión JPEG (0-100). Por defecto 80.
     * @return Result con la URL de descarga pública y el storagePath
     */
    suspend fun uploadCompraImage(
        uri: Uri,
        userId: String,
        compraId: String,
        compressQuality: Int = 80
    ): Result<ImageUploadResult>

    /**
     * Elimina una imagen de compra de Firebase Storage
     *
     * @param storagePath Ruta completa en Storage
     * @return Result indicando éxito o fallo
     */
    suspend fun deleteCompraImage(storagePath: String): Result<Unit>

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
        private const val LICENCIAS_FOLDER = "licencias"
        private const val COMPRAS_FOLDER = "compras"
        private const val MAX_IMAGE_DIMENSION = 1920 // Máximo ancho/alto para redimensionar
        private const val IMAGE_EXTENSION = ".jpg"
    }

    // ==================== GUÍAS (ARMAS) ====================

    override suspend fun uploadGuiaImage(
        uri: Uri,
        userId: String,
        armaId: String,
        compressQuality: Int
    ): Result<ImageUploadResult> = uploadImage(
        uri = uri,
        userId = userId,
        entityId = armaId,
        folder = ARMAS_FOLDER,
        entityType = "arma",
        compressQuality = compressQuality
    )

    override suspend fun deleteGuiaImage(storagePath: String): Result<Unit> = 
        deleteImage(storagePath)

    // ==================== LICENCIAS ====================

    override suspend fun uploadLicenciaImage(
        uri: Uri,
        userId: String,
        licenciaId: String,
        compressQuality: Int
    ): Result<ImageUploadResult> = uploadImage(
        uri = uri,
        userId = userId,
        entityId = licenciaId,
        folder = LICENCIAS_FOLDER,
        entityType = "licencia",
        compressQuality = compressQuality
    )

    override suspend fun deleteLicenciaImage(storagePath: String): Result<Unit> = 
        deleteImage(storagePath)

    // ==================== COMPRAS ====================

    override suspend fun uploadCompraImage(
        uri: Uri,
        userId: String,
        compraId: String,
        compressQuality: Int
    ): Result<ImageUploadResult> = uploadImage(
        uri = uri,
        userId = userId,
        entityId = compraId,
        folder = COMPRAS_FOLDER,
        entityType = "compra",
        compressQuality = compressQuality
    )

    override suspend fun deleteCompraImage(storagePath: String): Result<Unit> = 
        deleteImage(storagePath)

    // ==================== MÉTODOS COMUNES ====================

    /**
     * Sube una imagen genérica a Firebase Storage
     */
    private suspend fun uploadImage(
        uri: Uri,
        userId: String,
        entityId: String,
        folder: String,
        entityType: String,
        compressQuality: Int
    ): Result<ImageUploadResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting upload for $entityType=$entityId, userId=$userId")

            // 1. Comprimir imagen
            val compressedBytes = compressImage(uri, compressQuality)
                ?: return@withContext Result.failure(
                    IllegalStateException("No se pudo procesar la imagen")
                )

            Log.d(TAG, "Image compressed: ${compressedBytes.size} bytes")

            // 2. Construir ruta de almacenamiento
            val storagePath = buildStoragePath(userId, entityId, folder)
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
            crashlytics.log("Image upload failed for $entityType=$entityId: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Elimina una imagen de Firebase Storage
     */
    private suspend fun deleteImage(storagePath: String): Result<Unit> = withContext(Dispatchers.IO) {
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
     * Formato: v3_userdata/{userId}/{folder}/{entityId}_{timestamp}.jpg
     * Ejemplo: v3_userdata/abc123/armas/5_20251217_220315.jpg
     *
     * @param userId ID del usuario
     * @param entityId ID de la entidad (licencia, guia, compra)
     * @param folder Carpeta destino (armas, licencias, compras)
     * @return Ruta completa del archivo en Storage
     */
    private fun buildStoragePath(userId: String, entityId: String, folder: String = ARMAS_FOLDER): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${entityId}_$timestamp"
        return "$STORAGE_VERSION/$userId/$folder/$fileName$IMAGE_EXTENSION"
    }
}
