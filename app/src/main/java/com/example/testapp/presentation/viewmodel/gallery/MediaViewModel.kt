package com.example.testapp.presentation.viewmodel.gallery

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.presentation.templates.media.MediaType
import com.example.testapp.utils.MediaLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaViewModel: ViewModel() {
    private val _images = MutableStateFlow<List<Uri>>(emptyList())
    private val _documents = MutableStateFlow<List<Uri>>(emptyList())
    private val _audio = MutableStateFlow<List<Uri>>(emptyList())
    private val _videos = MutableStateFlow<List<Uri>>(emptyList())

    val images: StateFlow<List<Uri>> = _images.asStateFlow()
    val documents: StateFlow<List<Uri>> = _documents.asStateFlow()
    val audio: StateFlow<List<Uri>> = _audio.asStateFlow()
    val videos: StateFlow<List<Uri>> = _videos.asStateFlow()

    fun loadMedia(context: Context, mediaType: MediaType) {
        viewModelScope.launch(Dispatchers.IO) {
            when (mediaType) {
                MediaType.IMAGES -> _images.emit(MediaLoader.loadImagesFromGallery(context))
                MediaType.DOCUMENTS -> _documents.emit(MediaLoader.loadDocuments(context))
                MediaType.AUDIO -> _audio.emit(MediaLoader.loadAudioFiles(context))
                MediaType.VIDEO -> _videos.emit(MediaLoader.loadVideos(context))
            }
        }
    }
}