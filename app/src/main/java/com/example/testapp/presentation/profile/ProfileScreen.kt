package com.example.testapp.presentation.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.example.testapp.R
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.templates.section.Section
import com.example.testapp.presentation.templates.section.SectionItem
import com.example.testapp.presentation.viewmodel.gallery.GalleryViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.AvatarService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ProfileScreen(
    currentUser: UserResponse?,
    userViewModel: UserViewModel,
    galleryViewModel: GalleryViewModel,
    avatarService: AvatarService,
    mainNavController: NavController,
    profileNavController: NavController,
    scope: CoroutineScope
) {
    var showDropdown by remember { mutableStateOf(false) }
    var isGallerySheetVisible by remember { mutableStateOf(false) }
    val images by galleryViewModel.images.collectAsState()
    val context = LocalContext.current

    if (isGallerySheetVisible) {
        GalleryBottomSheet(
            images = images,
            onImageSelected = { uri ->
                scope.launch {
                    try {
                        val avatarName = currentUser?.userId ?: "default"
                        val newAvatarUrl = avatarService.updateUserAvatarFromUri(avatarName, uri, context)
                        currentUser?.let { user ->
                            val updatedUser = user.copy(avatarUrl = newAvatarUrl)
                            userViewModel.updateUser(updatedUser)
                        }
                        isGallerySheetVisible = false
                    } catch (e: Exception) {
                        Log.e("ProfileScreen", "Failed to update avatar", e)
                        Toast.makeText(context, "Failed to update avatar", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { isGallerySheetVisible = false },
            onRequestPermission = { galleryViewModel.loadImages(context) },
            context = context
        )
    }

    Scaffold(
        topBar = {
            ProfileAppBar(
                onBackClick = {
                    mainNavController.popBackStack()
                    userViewModel.onCleared()
                },
                onMoreClick = { showDropdown = !showDropdown },
                onChangeAvatar = { isGallerySheetVisible = !isGallerySheetVisible },
                showDropdown = showDropdown,
                onDismissDropdown = { showDropdown = !showDropdown }
            )
        }
    ) { innerPadding ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val (profile, user, settings, account, avatar) = createRefs()

            currentUser?.let {
                ProfileSection(
                    userData = currentUser,
                    modifier = Modifier.constrainAs(profile) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )

                Section(
                    title = stringResource(R.string.profile_account),
                    modifier = Modifier.constrainAs(user) {
                        top.linkTo(profile.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                    items = listOf(
                        SectionItem.Text(
                            title = "@${currentUser.nickname}",
                            subtitle = stringResource(R.string.profile_username),
                            onClick = {
                                profileNavController.navigate("usernameScreen")
                            }
                        ),
                        SectionItem.Text(
                            title = if(!currentUser.description.isNullOrBlank()) currentUser.description
                                else stringResource(R.string.profile_empty_bio_title),
                            subtitle = if (!currentUser.description.isNullOrBlank()) stringResource(R.string.profile_bio_subtitle)
                                else stringResource(R.string.profile_empty_bio_subtitle),
                            onClick = {
                                profileNavController.navigate("bioScreen")
                            }
                        )
                    )
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickable {
                            isGallerySheetVisible = !isGallerySheetVisible
                        }
                        .padding(12.dp)
                        .constrainAs(avatar) {
                            top.linkTo(profile.bottom)
                            bottom.linkTo(user.top)
                            end.linkTo(parent.end, margin = 16.dp)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_photo),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }

                Section(
                    title = stringResource(R.string.profile_settings),
                    modifier = Modifier.constrainAs(settings) {
                        top.linkTo(user.bottom, margin = 12.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                    items = listOf(
                        SectionItem.Icon(
                            title = stringResource(R.string.profile_chat_settings),
                            icon = painterResource(id = R.drawable.ic_chat_bubble),
                            onClick = {
                                profileNavController.navigate("chatConfigScreen")
                            }
                        ),
                        SectionItem.Icon(
                            title = stringResource(R.string.profile_language),
                            icon = painterResource(id = R.drawable.ic_web),
                            onClick = {
                                profileNavController.navigate("languageScreen")
                            },
                            trailingText = Locale.getDefault().displayLanguage
                        )
                    )
                )
            }

            Section(
                modifier = Modifier.constrainAs(account) {
                    top.linkTo(settings.bottom, margin = 12.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                items = listOf(
                    SectionItem.Icon(
                        title = stringResource(R.string.nav_logout),
                        icon = painterResource(id = R.drawable.ic_logout),
                        tintColor = MaterialTheme.colorScheme.error,
                        onClick = {
                            //profileNavController.navigate("chatConfigScreen")
                        }
                    ),
                    SectionItem.Icon(
                        title = stringResource(R.string.profile_delete_account),
                        icon = painterResource(id = R.drawable.ic_chat_bubble),
                        tintColor = MaterialTheme.colorScheme.error,
                        onClick = {
                            //profileNavController.navigate("languageScreen")
                        }
                    )
                )
            )
        }
    }
}