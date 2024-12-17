package com.example.testapp.presentation.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.testapp.R
import kotlinx.coroutines.launch

@Composable
fun ThemeIcon(
    onThemeToggle: suspend () -> Unit,
    isDarkTheme: Boolean,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(backgroundColor, shape = CircleShape)
            .clip(CircleShape)
            .clickable {
                coroutineScope.launch {
                    onThemeToggle()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(
                id = if (isDarkTheme) R.drawable.ic_dark_theme
                else R.drawable.ic_light_theme
            ),
            contentDescription = "Toggle theme",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}