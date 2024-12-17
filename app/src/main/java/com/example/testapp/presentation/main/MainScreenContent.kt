package com.example.testapp.presentation.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.testapp.presentation.main.maincontent.MainContent
import com.example.testapp.presentation.main.searchscreen.SearchScreen
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.presentation.viewmodel.message.MessageViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel

@Composable
fun MainScreenContent(
    onSearchTypeChange: (SearchType) -> Unit,
    isSearchActive: Boolean,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    messageViewModel: MessageViewModel,
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
            /*Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LogoIcon(LogoSize.LARGE)
            }*/
            userId?.let {
                MainContent(
                    userViewModel = userViewModel,
                    chatViewModel = chatViewModel,
                    messageViewModel = messageViewModel,
                    currentUserId = userId,
                    mainNavController = mainNavController
                )
            }
        }
    }
}