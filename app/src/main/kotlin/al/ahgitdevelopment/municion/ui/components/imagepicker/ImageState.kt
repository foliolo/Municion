package al.ahgitdevelopment.municion.ui.components.imagepicker

import android.net.Uri

/**
 * Estado unificado para la gestión de imágenes en formularios.
 *
 * Simplifica la lógica de 3 variables separadas (selectedImageUri, existingImageUrl, storagePath)
 * en un solo sealed class con estados claros.
 *
 * Estados posibles:
 * - NoImage: No hay imagen asociada
 * - Existing: Imagen ya subida a Firebase Storage
 * - New: Nueva imagen seleccionada localmente (pendiente de subir)
 *
 * @since v3.2.4 (Image State Simplification)
 */
sealed class ImageState {
    /**
     * No hay imagen asociada a este registro
     */
    data object NoImage : ImageState()

    /**
     * Imagen existente ya subida a Firebase Storage
     *
     * @param url URL pública de descarga de Firebase Storage
     * @param storagePath Ruta en Storage para poder eliminar la imagen
     */
    data class Existing(
        val url: String,
        val storagePath: String
    ) : ImageState()

    /**
     * Nueva imagen seleccionada localmente, pendiente de subir
     *
     * @param uri URI local de la imagen (content:// o file://)
     * @param previousState Estado anterior (para poder restaurar si se cancela)
     */
    data class New(
        val uri: Uri,
        val previousState: ImageState = NoImage
    ) : ImageState()

    /**
     * URL a mostrar en la UI.
     * - Para Existing: la URL de Firebase
     * - Para New: la URI local convertida a String
     * - Para NoImage: null
     */
    val displayUrl: String?
        get() = when (this) {
            is NoImage -> null
            is Existing -> url
            is New -> uri.toString()
        }

    /**
     * Indica si hay imagen (existente o nueva) para mostrar
     */
    val hasImage: Boolean
        get() = this !is NoImage

    /**
     * Indica si hay una nueva imagen pendiente de subir
     */
    val hasNewImage: Boolean
        get() = this is New

    /**
     * Indica si hay una imagen existente en Storage
     */
    val hasExistingImage: Boolean
        get() = this is Existing

    /**
     * Obtiene el storagePath si existe (para eliminar imagen anterior)
     */
    val existingStoragePath: String?
        get() = when (this) {
            is Existing -> storagePath
            is New -> (previousState as? Existing)?.storagePath
            else -> null
        }

    /**
     * Obtiene la URI de la nueva imagen si existe
     */
    val newImageUri: Uri?
        get() = (this as? New)?.uri

    /**
     * Obtiene la URL existente si hay
     */
    val existingUrl: String?
        get() = (this as? Existing)?.url

    companion object {
        /**
         * Crea un ImageState desde los campos de una entidad
         *
         * @param fotoUrl URL de Firebase Storage (puede ser null o blank)
         * @param storagePath Ruta en Storage (puede ser null o blank)
         * @return ImageState apropiado
         */
        fun fromEntity(fotoUrl: String?, storagePath: String?): ImageState {
            return if (!fotoUrl.isNullOrBlank() && !storagePath.isNullOrBlank()) {
                Existing(url = fotoUrl, storagePath = storagePath)
            } else {
                NoImage
            }
        }
    }
}

/**
 * Datos de imagen para guardar en la entidad.
 *
 * Usado como resultado del procesamiento de ImageState antes de guardar.
 */
data class ImageData(
    val fotoUrl: String?,
    val storagePath: String?
) {
    companion object {
        val EMPTY = ImageData(null, null)
    }
}

/**
 * Convierte un ImageState a ImageData para guardar.
 *
 * IMPORTANTE: Solo debe usarse después de que las nuevas imágenes
 * hayan sido subidas a Firebase (New → Existing).
 */
fun ImageState.toImageData(): ImageData = when (this) {
    is ImageState.NoImage -> ImageData.EMPTY
    is ImageState.Existing -> ImageData(fotoUrl = url, storagePath = storagePath)
    is ImageState.New -> {
        // Si llegamos aquí con New, significa que no se subió la imagen
        // Preservamos el estado anterior si existe
        (previousState as? ImageState.Existing)?.let {
            ImageData(fotoUrl = it.url, storagePath = it.storagePath)
        } ?: ImageData.EMPTY
    }
}
