package al.ahgitdevelopment.municion.ui.components.imagepicker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidades para el manejo de imágenes con corrección de orientación EXIF.
 * 
 * Soluciona el problema común de imágenes rotadas 90° en dispositivos Samsung y otros.
 * 
 * El flujo recomendado:
 * 1. Crear archivo temporal con createImageFile()
 * 2. Obtener Uri con getUriForFile()
 * 3. Pasar Uri a TakePicture contract
 * 4. Procesar imagen con processAndCorrectOrientation()
 * 
 * @since v3.2.2
 */
object ImageUtils {
    
    private const val TAG = "ImageUtils"
    private const val FILE_PROVIDER_AUTHORITY = "al.ahgitdevelopment.municion.fileprovider"
    private const val MAX_IMAGE_DIMENSION = 1920
    private const val JPEG_QUALITY = 85
    
    /**
     * Crea un archivo temporal para guardar la foto de la cámara.
     * 
     * @param context Context de la aplicación
     * @return File temporal en el directorio de imágenes de la app
     * @throws IOException si no se puede crear el archivo
     */
    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir("Pictures")
            ?: throw IOException("No se pudo acceder al directorio de almacenamiento")
        
        return File.createTempFile(imageFileName, ".jpg", storageDir).also {
            Log.d(TAG, "Created temp file: ${it.absolutePath}")
        }
    }
    
    /**
     * Obtiene un Uri seguro para compartir con la cámara usando FileProvider.
     * 
     * @param context Context de la aplicación
     * @param file Archivo para el que obtener el Uri
     * @return Uri que puede ser pasado a TakePicture contract
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
    }
    
    /**
     * Procesa una imagen corrigiendo la orientación EXIF y opcionalmente redimensionando.
     * 
     * Este método soluciona el problema de imágenes rotadas en dispositivos Samsung y otros
     * que guardan la imagen en orientación del sensor y usan tags EXIF para indicar la rotación.
     * 
     * @param context Context de la aplicación
     * @param sourceUri Uri de la imagen original (puede ser de cámara o galería)
     * @param maxDimension Dimensión máxima (ancho o alto) para redimensionar
     * @param quality Calidad JPEG (0-100)
     * @return Uri del archivo procesado con orientación correcta, o null si falla
     */
    suspend fun processAndCorrectOrientation(
        context: Context,
        sourceUri: Uri,
        maxDimension: Int = MAX_IMAGE_DIMENSION,
        quality: Int = JPEG_QUALITY
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Processing image: $sourceUri")
            
            // 1. Leer orientación EXIF antes de decodificar
            val rotation = getExifRotation(context, sourceUri)
            Log.d(TAG, "EXIF rotation: $rotation degrees")
            
            // 2. Decodificar bitmap con tamaño optimizado
            val bitmap = decodeSampledBitmap(context, sourceUri, maxDimension)
                ?: return@withContext null
            
            // 3. Aplicar rotación si es necesario
            val correctedBitmap = if (rotation != 0) {
                rotateBitmap(bitmap, rotation)
            } else {
                bitmap
            }
            
            // 4. Guardar a archivo temporal
            val outputFile = createImageFile(context)
            FileOutputStream(outputFile).use { out ->
                correctedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            
            // 5. Liberar memoria
            if (correctedBitmap != bitmap) {
                bitmap.recycle()
            }
            correctedBitmap.recycle()
            
            Log.d(TAG, "Image processed successfully: ${outputFile.absolutePath}")
            Uri.fromFile(outputFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image: ${e.message}", e)
            null
        }
    }
    
    /**
     * Lee la rotación EXIF de una imagen.
     * 
     * @param context Context de la aplicación
     * @param uri Uri de la imagen
     * @return Grados de rotación (0, 90, 180, 270)
     */
    private fun getExifRotation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    ExifInterface.ORIENTATION_TRANSVERSE -> 90  // Flipped + rotated
                    ExifInterface.ORIENTATION_TRANSPOSE -> 270  // Flipped + rotated
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            Log.w(TAG, "Could not read EXIF: ${e.message}")
            0
        }
    }
    
    /**
     * Decodifica un bitmap con tamaño optimizado para evitar OOM.
     * 
     * @param context Context de la aplicación
     * @param uri Uri de la imagen
     * @param maxDimension Dimensión máxima deseada
     * @return Bitmap decodificado o null si falla
     */
    private fun decodeSampledBitmap(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
        return try {
            // Primero obtener dimensiones sin cargar el bitmap
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
            
            // Calcular sample size
            options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension)
            options.inJustDecodeBounds = false
            
            Log.d(TAG, "Original: ${options.outWidth}x${options.outHeight}, sampleSize: ${options.inSampleSize}")
            
            // Decodificar con sample size
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bitmap: ${e.message}", e)
            null
        }
    }
    
    /**
     * Calcula el factor de escala óptimo para decodificar un bitmap.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Rota un bitmap según los grados especificados.
     * 
     * @param bitmap Bitmap original
     * @param degrees Grados de rotación (90, 180, 270)
     * @return Nuevo bitmap rotado
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees.toFloat())
        }
        
        return Bitmap.createBitmap(
            bitmap,
            0, 0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
    
    /**
     * Limpia archivos temporales de imágenes antiguos.
     * 
     * @param context Context de la aplicación
     * @param maxAgeMs Edad máxima en milisegundos (default: 24 horas)
     */
    fun cleanupOldTempFiles(context: Context, maxAgeMs: Long = 24 * 60 * 60 * 1000) {
        try {
            val storageDir = context.getExternalFilesDir("Pictures") ?: return
            val cutoffTime = System.currentTimeMillis() - maxAgeMs
            
            storageDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("JPEG_") && file.lastModified() < cutoffTime) {
                    file.delete()
                    Log.d(TAG, "Deleted old temp file: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning temp files: ${e.message}")
        }
    }
}
