package al.ahgitdevelopment.municion.repository.firebase

import android.graphics.Bitmap
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File

interface RemoteStorageDataSourceContract {
    suspend fun getTutorialImages(): List<File>
    fun saveItemImage(bitmap: Bitmap, itemId: String): UploadTask
    fun getReference(path: String?): StorageReference
}
