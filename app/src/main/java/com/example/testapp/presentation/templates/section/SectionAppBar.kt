package com.example.testapp.presentation.templates.section

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionAppBar(
   title: String,
   onBackClick: () -> Unit,
   isDataChanged: Boolean = false,
   onSaveClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { onBackClick() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Main")
            }
        },
        actions = {
            if(isDataChanged) {
                IconButton(onClick = { onSaveClick() }) {
                    Icon(Icons.Default.Check, contentDescription = "Save changes")
                }
            }
        }
    )
}