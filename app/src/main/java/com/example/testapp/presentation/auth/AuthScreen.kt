package com.example.testapp.presentation.auth

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testapp.presentation.viewmodel.ThemeViewModel
import com.example.testapp.presentation.viewmodel.user.AuthViewModel
import com.example.testapp.utils.DataStoreUtil

@Composable
fun AuthScreen(
    parentNavController: NavController,
    dataStoreUtil: DataStoreUtil,
    authViewModel: AuthViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val authNavController = rememberNavController()

    NavHost(
        navController = authNavController,
        startDestination = "login",
        enterTransition = { fadeIn(animationSpec = tween(600)) },
        exitTransition = { fadeOut(animationSpec = tween(600)) },
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable("login") {
            LoginScreen(
                navController = authNavController,
                parentNavController = parentNavController,
                themeViewModel = themeViewModel,
                authViewModel = authViewModel,
                dataStoreUtil = dataStoreUtil
            )
        }
        composable("signup") {
            SignUpScreen(
                navController = authNavController,
                parentNavController = parentNavController,
                themeViewModel = themeViewModel,
                authViewModel = authViewModel,
                dataStoreUtil = dataStoreUtil
            )
        }
    }
}