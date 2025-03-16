package com.example.testapp.presentation.profile

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testapp.presentation.viewmodel.gallery.GalleryViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.storage.AvatarService

@Composable
fun ProfileNavigator(
    userViewModel: UserViewModel = hiltViewModel(),
    galleryViewModel: GalleryViewModel = hiltViewModel(),
    avatarService: AvatarService,
    mainNavController: NavController
) {
    val profileNavController = rememberNavController()
    val currentUserState by userViewModel.currentUserState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = profileNavController,
        startDestination = "profileScreen"
    ) {
        composable(
            route = "profileScreen",
            enterTransition = { slideInHorizontally { -it } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it } + fadeOut() }
        ) {
            ProfileScreen(
                currentUser = currentUserState.data,
                userViewModel = userViewModel,
                galleryViewModel = galleryViewModel,
                mainNavController = mainNavController,
                profileNavController = profileNavController,
                avatarService = avatarService,
                scope = coroutineScope
            )
        }
        composable(
            route = "languageScreen",
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) {
            LanguageScreen(
                onLanguageSelected = {},
                profileNavController = profileNavController
            )
        }
        composable(
            route = "bioScreen",
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) {
            BioScreen(
                userData = currentUserState.data,
                profileNavController = profileNavController,
                userViewModel = userViewModel,
                scope = coroutineScope
            )
        }
        composable(
            route = "usernameScreen",
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) {
            UsernameScreen(
                userData = currentUserState.data,
                profileNavController = profileNavController,
                userViewModel = userViewModel,
                scope = coroutineScope
            )
        }
        composable(
            route = "chatConfigScreen",
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) {
            ChatConfigScreen(
                userData = currentUserState.data,
                profileNavController = profileNavController,
                userViewModel = userViewModel,
                scope = coroutineScope
            )
        }
    }
}