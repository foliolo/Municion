package al.ahgitdevelopment.municion.repository.firebase

import android.content.Context
import android.net.ConnectivityManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File

class RemoteStorageDataSource constructor(
    context: Context,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val crashlytics: FirebaseCrashlytics,
    private val connectivityManager: ConnectivityManager,
) : RemoteStorageDataSourceContract {

    private val imagesRootDir = context.externalCacheDir

    override suspend fun getTutorialImages(): List<File> {
        val imageReference = storage.reference.child(STORAGE_ROOT_PATH)

        return auth.currentUser.let { currentUser ->
            @Suppress("DEPRECATION")
            if (currentUser != null && connectivityManager.activeNetworkInfo?.isConnected == true) {
                Timber.d("Retrieve images")
                verifyMetadataOrDownload(imageReference)
            } else {
                Timber.w("Not authenticated")
                crashlytics.recordException(IllegalStateException("Not authenticated"))
                arrayListOf()
            }
        }
    }

    private suspend fun verifyMetadataOrDownload(imageReference: StorageReference): List<File> {

        val imageUrls = mutableListOf<File>()
        imageReference.listAll().await().let { images ->

            for (image in images.items) {

                val imagePath = image.name
                val imageFile = File(imagesRootDir, "images/$imagePath")
                val md5File = File(imagesRootDir, "images/$imagePath.md5")

                if (imageFile.exists() && md5File.verifyMd5Hash(image.metadata.await().md5Hash)) {
                    Timber.d("Using existing image: $imagePath (md5 OK)")
                    imageUrls.add(imageFile)
                } else {
                    md5File.saveMd5Hash(image.metadata.await().md5Hash)
                    imageUrls.add(downloadImage(image, imageFile, md5File))
                }
            }
        }
        return imageUrls
    }

    private suspend fun downloadImage(image: StorageReference, imageFile: File, md5File: File): File {

        imageFile.delete()
        md5File.delete()

        return image.getFile(imageFile).await().let {
            Timber.d("Image downloaded: ${image.name}")
            md5File.saveMd5Hash(image.metadata.await().md5Hash)
            imageFile
        }
    }

    private fun File.verifyMd5Hash(md5Hash: String?): Boolean = exists() && readText() == md5Hash

    private fun File.saveMd5Hash(md5Hash: String?) {
        parentFile?.mkdirs()
        writeText(text = md5Hash.orEmpty())
    }

    companion object {
        private const val STORAGE_ROOT_PATH = "TutorialImages"

        const val PARAM_USER_UID = "user_uid"
        const val EVENT_LOGOUT = "logout"
        const val EVENT_CLOSE_APP = "close_app"
    }
}
