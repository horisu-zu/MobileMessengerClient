package com.example.testapp.domain.dto.message

import android.net.Uri
import com.example.testapp.presentation.templates.media.MediaType

data class LocalAttachment(
    val uri: Uri,
    val mediaType: MediaType
)
