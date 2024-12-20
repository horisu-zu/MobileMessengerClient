package com.example.testapp.utils

import androidx.annotation.DrawableRes

sealed class ImageSource {
    data class Drawable(@DrawableRes val resId: Int) : ImageSource()
    data class Url(val url: String) : ImageSource()
}