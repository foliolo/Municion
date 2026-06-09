package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.firebase.toStorageData
import al.ahgitdevelopment.municion.platform.processImageForUpload
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage

/** Storage sub-tree per entity, mirroring the `develop` layout `v3_userdata/{uid}/{folder}/…`. */
enum class ImageFolder(
    val folder: String,
) {
    GUIA("armas"),
    LICENCIA("licencias"),
    COMPRA("compras"),
}

/** Download URL (stored in `fotoUrl`) + full Storage path (stored in `storagePath`, used for deletion). */
data class ImageUploadResult(
    val downloadUrl: String,
    val storagePath: String,
)

interface ImageStorageRepository {
    /**
     * Processes [bytes] and uploads them to `v3_userdata/{userId}/{folder}/{key}.jpg`.
     * [key] is the entity's stable syncId so the path is deterministic across new/edit and devices.
     */
    suspend fun uploadImage(
        userId: String,
        folder: ImageFolder,
        key: String,
        bytes: ByteArray,
    ): Result<ImageUploadResult>

    /** Deletes a previously-uploaded image by its full Storage path (best-effort). */
    suspend fun deleteImage(storagePath: String): Result<Unit>
}

class FirebaseImageStorageRepository(
    private val crashReporter: CrashReporter,
) : ImageStorageRepository {
    override suspend fun uploadImage(
        userId: String,
        folder: ImageFolder,
        key: String,
        bytes: ByteArray,
    ): Result<ImageUploadResult> =
        runCatching {
            val path = "$ROOT/$userId/${folder.folder}/$key.jpg"
            val ref = Firebase.storage.reference.child(path)
            ref.putData(processImageForUpload(bytes).toStorageData())
            ImageUploadResult(downloadUrl = ref.getDownloadUrl(), storagePath = path)
        }.onFailure { crashReporter.recordException(it) }

    override suspend fun deleteImage(storagePath: String): Result<Unit> =
        runCatching {
            Firebase.storage.reference
                .child(storagePath)
                .delete()
        }.onFailure { crashReporter.recordException(it) }

    private companion object {
        const val ROOT = "v3_userdata"
    }
}
