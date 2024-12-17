package com.example.testapp.presentation.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.example.testapp.presentation.viewmodel.gallery.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAppBar(
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    onChangeAvatar: () -> Unit,
    showDropdown: Boolean,
    onDismissDropdown: () -> Unit
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = { onBackClick() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Main")
            }
        },
        actions = {
            IconButton(onClick = { onMoreClick() }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }

            ProfileDropdown(
                isExpanded = showDropdown,
                onDismissRequest = { onDismissDropdown() },
                onEditClick = { /*TODO*/ },
                onChangeAvatarClick = { onChangeAvatar() },
                onChangeColorClick = { /*TODO*/ },
            )
        }
    )
}