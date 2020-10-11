package al.ahgitdevelopment.municion.tutorial

import java.io.File

interface TutorialImagesRepository {
    suspend fun getImages(): List<File>
}