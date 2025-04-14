package com.example.testapp.presentation.templates.section

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

sealed class SectionItem {
    data class Text(
        val title: String,
        val subtitle: String,
        val onClick: () -> Unit
    ): SectionItem()

    data class Icon(
        val title: String,
        val icon: Painter,
        val onClick: () -> Unit,
        val trailingText: String? = null,
        val tintColor: Color? = null
    ): SectionItem()

    data class Radio(
        val title: String,
        val selected: Boolean,
        val onClick: () -> Unit
    ): SectionItem()

    data class Input(
        val label: String,
        val value: String,
        val onValueChange: (String) -> Unit,
        val limit: Int? = null,
        val placeholder: String? = null,
        val inputFilter: (String) -> String = { it }
    ): SectionItem()
}