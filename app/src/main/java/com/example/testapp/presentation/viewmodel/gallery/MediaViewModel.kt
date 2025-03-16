package com.example.testapp.presentation.viewmodel.gallery

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.presentation.templates.media.MediaType
import com.example.testapp.utils.MediaLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor() : ViewModel() {
    private val _mediaState = MutableStateFlow<Map<MediaType, List<Uri>>>(emptyMap())
    val mediaState: StateFlow<Map<MediaType, List<Uri>>> = _mediaState.asStateFlow()

    fun loadMedia(context: Context, mediaType: MediaType) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMedia = _mediaState.value.toMutableMap()
            val newMedia = when (mediaType) {
                MediaType.IMAGES -> MediaLoader.loadImagesFromGallery(context)
                MediaType.AUDIO -> MediaLoader.loadAudioFiles(context)
                MediaType.VIDEO -> MediaLoader.loadVideos(context)
            }
            updatedMedia[mediaType] = newMedia
            _mediaState.emit(updatedMedia)
        }
    }
}