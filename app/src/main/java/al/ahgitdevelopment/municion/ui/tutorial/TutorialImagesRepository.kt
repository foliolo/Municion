package al.ahgitdevelopment.municion.ui.tutorial

import java.io.File

interface TutorialImagesRepository {
    suspend fun getImages(): List<File>
}
