package com.example.testapp.presentation.profile

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.testapp.presentation.viewmodel.gallery.GalleryViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.AvatarService

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun ProfileNavigator(
    userViewModel: UserViewModel,
    galleryViewModel: GalleryViewModel,
    avatarService: AvatarService,
    mainNavController: NavController
) {
    val profileNavController = rememberNavController()
    val currentBackStackEntry by profileNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "profileScreen"
    val currentUserState by userViewModel.currentUserState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            if (targetState != "profileScreen") {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut())
            } else {
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut())
            }.using(SizeTransform(clip = false))
        },
        label = ""
    ) {
        NavHost(
            navController = profileNavController,
            startDestination = "profileScreen"
        ) {
            composable(route = "profileScreen") {
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
            composable(route = "languageScreen") {
                LanguageScreen(
                    onLanguageSelected = {
                    },
                    profileNavController = profileNavController
                )
            }
            composable(route = "bioScreen") {
                BioScreen(
                    userData = currentUserState.data,
                    profileNavController = profileNavController,
                    userViewModel = userViewModel,
                    scope = coroutineScope
                )
            }
            composable(route = "usernameScreen") {
                UsernameScreen(
                    userData = currentUserState.data,
                    profileNavController = profileNavController,
                    userViewModel = userViewModel,
                    scope = coroutineScope
                )
            }
            composable(route = "chatConfigScreen") {
                ChatConfigScreen(
                    userData = currentUserState.data,
                    profileNavController = profileNavController,
                    userViewModel = userViewModel,
                    scope = coroutineScope
                )
            }
        }
    }
}