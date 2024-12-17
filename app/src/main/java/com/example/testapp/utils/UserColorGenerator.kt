package com.example.testapp.utils

import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object UserColorGenerator {
    private val userColors = listOf(
        Color(0xFFE57373),
        Color(0xFF81C784),
        Color(0xFF64B5F6),
        Color(0xFFBA68C8),
        Color(0xFF4DB6AC),
        Color(0xFFFF8A65),
        Color(0xFFFFC107),
        Color(0xFF5C6BC0),
        Color(0xFFFF5722),
        Color(0xFFAB47BC),
    )

    fun getUserColor(): String {
        return userColors.random().toHexString()
    }

    fun getColors(): List<Color> {
        return userColors
    }

    fun Color.toHexString(): String {
        return String.format("#%06X", 0xFFFFFF and this.toArgb())
    }

    fun String.toColor(): Color {
        return try {
            when {
                startsWith("0x", ignoreCase = true) -> {
                    val colorLong = this.substring(2).toLong(16)
                    Color(colorLong.toInt())
                }
                else -> {
                    Color(parseColor(this))
                }
            }
        } catch (e: IllegalArgumentException) {
            Color.Black
        }
    }
}