package com.example.testapp.presentation.main.group.groupcustomization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onGallerySelected: () -> Unit,
    onLinkEntered: (String) -> Unit
) {
    var showLinkInput by remember { mutableStateOf(false) }
    var link by remember { mutableStateOf("") }

    if (!showLinkInput) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(
                text = "Choose the source of the image",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            ) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onGallerySelected) {
                        Text("Gallery")
                    }
                    TextButton(onClick = { showLinkInput = true }) {
                        Text("Link")
                    }
                }
            },
            confirmButton = { },
            dismissButton = { },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
        )
    } else {
        AlertDialog(
            onDismissRequest = { showLinkInput = false },
            title = { Text("Enter the link") },
            text = {
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Image URL") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onLinkEntered(link)
                    showLinkInput = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkInput = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}