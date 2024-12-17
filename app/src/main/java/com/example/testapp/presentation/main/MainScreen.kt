package com.example.testapp.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.navigation.navigationItemList
import com.example.testapp.presentation.main.navigationdrawer.MainNavigationDrawer
import com.example.testapp.presentation.viewmodel.ThemeViewModel
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.presentation.viewmodel.message.MessageViewModel
import com.example.testapp.presentation.viewmodel.user.AuthManager
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.DataStoreUtil
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    parentNavController: NavController,
    mainNavController: NavController,
    userViewModel: UserViewModel,
    currentUser: UserResponse?,
    chatViewModel: ChatViewModel,
    messageViewModel: MessageViewModel,
    themeViewModel: ThemeViewModel,
    dataStoreUtil: DataStoreUtil,
    authManager: AuthManager
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentSearchType by remember { mutableStateOf(SearchType.USERS) }

    ModalNavigationDrawer(
        drawerContent = {
            MainNavigationDrawer(
                currentUser = currentUser,
                navigationData = navigationItemList(),
                drawerState = drawerState,
                scope = scope,
                authManager = authManager,
                parentNavController = parentNavController,
                userViewModel = userViewModel,
                themeViewModel = themeViewModel
            )
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                MainAppBar(
                    onDrawerClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    searchType = currentSearchType,
                    onSearchActiveChange = {
                        isSearchActive = it
                    },
                    onSearchQueryChange = { query, type ->
                        searchQuery = query
                        when (type) {
                            SearchType.USERS -> userViewModel.searchUsers(query)
                            SearchType.GROUPS -> { chatViewModel.searchChats(query) }
                            else -> {}
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                MainScreenContent(
                    onSearchTypeChange = { currentSearchType = it },
                    isSearchActive = isSearchActive,
                    userViewModel = userViewModel,
                    chatViewModel = chatViewModel,
                    messageViewModel = messageViewModel,
                    userId = currentUser?.userId,
                    mainNavController = mainNavController
                )
            }
        }
    }
}

enum class SearchType {
    USERS, GROUPS
}

enum class ScreenState {
    MAIN, SEARCH, PROFILE
}