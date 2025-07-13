package com.royro.photoAnim

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val useCase = CreateVideoUseCase()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _videoFile = MutableLiveData<File?>()
    val videoFile: LiveData<File?> = _videoFile

    fun createVideo(imageUris: List<Uri>, duration: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val file = useCase.execute(getApplication(), imageUris, duration)
            _videoFile.postValue(file)
            _isLoading.postValue(false)
        }
    }
}
