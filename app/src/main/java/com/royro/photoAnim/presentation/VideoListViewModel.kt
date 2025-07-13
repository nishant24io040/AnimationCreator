package com.royro.photoAnim.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.royro.photoAnim.domain.GetSavedVideosUseCase
import java.io.File

class VideoListViewModel(application: Application) : AndroidViewModel(application) {
    private val getVideosUseCase = GetSavedVideosUseCase()

    private val _videos = MutableLiveData<List<File>>()
    val videos: LiveData<List<File>> get() = _videos

    fun loadVideos() {
        _videos.value = getVideosUseCase.execute(getApplication())
    }
}
