package al.ahgitdevelopment.municion.ui.tutorial

import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSourceContract
import al.ahgitdevelopment.municion.utils.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TutorialViewModel @Inject constructor(
    remoteStorageDataSourceContract: RemoteStorageDataSourceContract,
) : ViewModel() {

    private val _images = MutableLiveData<List<File>>()
    val images: LiveData<List<File>> = _images

    val progressBar = SingleLiveEvent<Boolean>()

    init {
        viewModelScope.launch {
            progressBar.postValue(true)
            _images.postValue(remoteStorageDataSourceContract.getTutorialImages())
            progressBar.postValue(false)
        }
    }
}
