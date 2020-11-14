package al.ahgitdevelopment.municion.repository.firebase

import java.io.File

interface RemoteStorageDataSourceContract {
    suspend fun getTutorialImages(): List<File>
}
