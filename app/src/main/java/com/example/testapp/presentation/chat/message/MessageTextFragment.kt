package com.example.testapp.presentation.chat.message

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MessageTextFragment(
    message: String,
    modifier: Modifier = Modifier
) {
    SelectionContainer(modifier = modifier) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}