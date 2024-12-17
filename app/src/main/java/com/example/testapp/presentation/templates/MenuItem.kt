package com.example.testapp.presentation.templates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MenuItem(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurface
) {
    DropdownMenuItem(
        text = { Text(text = text, color = iconTint) },
        onClick = onClick,
        leadingIcon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        },
        contentPadding = PaddingValues(horizontal = 16.dp)
    )
}