package com.example.testapp.presentation.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.presentation.main.maincontent.MainContent
import com.example.testapp.presentation.main.searchscreen.SearchScreen
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel

@Composable
fun MainScreenContent(
    onSearchTypeChange: (SearchType) -> Unit,
    isSearchActive: Boolean,
    userViewModel: UserViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    userId: String?,
    mainNavController: NavController
) {
    AnimatedContent(
        targetState = isSearchActive,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut())
            } else {
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut())
            }.using(SizeTransform(clip = false))
        }, label = ""
    ) { targetState ->
        if (targetState) {
            SearchScreen(
                onSearchTypeChange = onSearchTypeChange,
                currentUserId = userId,
                userViewModel = userViewModel,
                chatViewModel = chatViewModel,
                mainNavController = mainNavController
            )
        } else {
            userId?.let {
                MainContent(
                    currentUserId = userId,
                    mainNavController = mainNavController
                )
            }
        }
    }
}