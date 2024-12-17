package com.example.testapp.presentation.chat.message

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MessageTextFragment(
    message: String,
    replyMessage: Boolean,
    modifier: Modifier = Modifier
) {
    SelectionContainer(
        modifier = modifier.padding(
            start = 12.dp, end = 12.dp,
            top = if (replyMessage) 4.dp else 8.dp,
            bottom = 8.dp
        )
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}