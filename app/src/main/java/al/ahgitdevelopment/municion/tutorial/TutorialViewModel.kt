package al.ahgitdevelopment.municion.tutorial

import al.ahgitdevelopment.municion.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class TutorialViewModel @Inject constructor(
    tutorialImagesRepository: TutorialImagesRepository
) : ViewModel() {

    private val _images = MutableLiveData<List<File>>()
    val images: LiveData<List<File>> = _images

    val progressBar = SingleLiveEvent<Boolean>()

    init {
        viewModelScope.launch {
            progressBar.postValue(true)
            _images.postValue(tutorialImagesRepository.getImages())
            progressBar.postValue(false)
        }
    }
}
