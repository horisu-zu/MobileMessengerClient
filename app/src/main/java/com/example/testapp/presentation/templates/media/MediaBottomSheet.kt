package com.example.testapp.presentation.templates.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaBottomSheet(
    mediaState: Map<MediaType, List<Uri>>,
    onMediaSelected: (Uri, MediaType) -> Unit,
    onDismiss: () -> Unit,
    onRequestPermission: (MediaType) -> Unit,
    context: Context
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Images", "Documents", "Audio", "Video")

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when (selectedTab) {
            0 -> if (permissions[Manifest.permission.READ_MEDIA_IMAGES] == true ||
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
                onRequestPermission(MediaType.IMAGES)
            }
            1 -> if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
                onRequestPermission(MediaType.DOCUMENTS)
            }
            2 -> if (permissions[Manifest.permission.READ_MEDIA_AUDIO] == true ||
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
                onRequestPermission(MediaType.AUDIO)
            }
            3 -> if (permissions[Manifest.permission.READ_MEDIA_VIDEO] == true ||
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
                onRequestPermission(MediaType.VIDEO)
            }
        }
    }

    LaunchedEffect(selectedTab) {
        val permissionsNeeded = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> when (selectedTab) {
                0 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                1 -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                2 -> arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
                3 -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
                else -> emptyArray()
            }
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allGranted = permissionsNeeded.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            onRequestPermission(MediaType.entries[selectedTab])
        } else {
            permissionLauncher.launch(permissionsNeeded)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column {
            TabRow(
                selectedTabIndex = selectedTab
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 12.sp) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ImageGrid(
                    items = mediaState[MediaType.IMAGES] ?: emptyList(),
                    onItemSelected = { onMediaSelected(it, MediaType.IMAGES) }
                )
                1 -> DocumentList(
                    documents = mediaState[MediaType.DOCUMENTS] ?: emptyList(),
                    onDocumentSelected = { onMediaSelected(it, MediaType.DOCUMENTS) }
                )
                2 -> AudioList(
                    audioFiles = mediaState[MediaType.AUDIO] ?: emptyList(),
                    onAudioSelected = { onMediaSelected(it, MediaType.AUDIO) }
                )
                3 -> VideoGrid(
                    items = mediaState[MediaType.VIDEO] ?: emptyList(),
                    onItemSelected = { onMediaSelected(it, MediaType.VIDEO) }
                )
            }
        }
    }
}
