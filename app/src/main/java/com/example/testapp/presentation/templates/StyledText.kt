package com.example.testapp.presentation.templates

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun StyledText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    MarkdownText(
        markdown = text,
        modifier = modifier,
        style = style,
        isTextSelectable = true
    )
}