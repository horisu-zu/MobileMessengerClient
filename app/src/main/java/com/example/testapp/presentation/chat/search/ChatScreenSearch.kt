package com.example.testapp.presentation.chat.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.InputChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.domain.dto.chat.SearchFilter
import com.example.testapp.presentation.viewmodel.message.ChatSearchViewModel

@Composable
fun ChatScreenSearch(
    chatId: String?,
    chatSearchViewModel: ChatSearchViewModel = hiltViewModel(),
    chatNavController: NavController
) {
    var currentMode by remember { mutableStateOf(SearchScreenMode.MESSAGES) }
    var searchQuery by remember { mutableStateOf("") }
    val showBottomSheet = remember { mutableStateOf(false) }

    LaunchedEffect(chatId) {
        chatId?.let { chatSearchViewModel.getChatUsers(it) }
    }

    val searchMessagesState by chatSearchViewModel.searchMessagesState.collectAsState()
    val searchUsersState by chatSearchViewModel.searchUsersState.collectAsState()
    val chatUsersState by chatSearchViewModel.chatUsersState.collectAsState()
    val filters = remember { mutableStateListOf<SearchFilter>() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ChatSearchAppBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { newValue ->
                    searchQuery = newValue
                    chatId?.let { id ->
                        chatSearchViewModel.searchMessages(
                            chatId = id,
                            query = searchQuery,
                            filters = filters
                        )
                    }
                },
                filters = filters,
                onBackClick = { chatNavController.popBackStack() },
                onFilterIconClick = { showBottomSheet.value = true },
                onFilterRemove = { searchFilter ->
                    filters.remove(searchFilter)
                    //Search Method, maybe I should separate it into a function and not call it
                    //manually every time
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            when(currentMode) {
                SearchScreenMode.MESSAGES -> {
                    LazyColumn(
                        reverseLayout = false,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchMessagesState.messages) { message ->
                            val senderData = searchUsersState.firstOrNull { it.userId == message.senderId }
                            val replyMessage = searchMessagesState.replyMessages[message.replyTo]
                            val replyUserData = searchUsersState.firstOrNull { it.userId == replyMessage?.senderId }
                            val messageAttachments = searchMessagesState.attachments[message.messageId]

                            senderData?.let { userData ->
                                SearchMessageItem(
                                    senderData = userData,
                                    message = message,
                                    messageAttachments = messageAttachments,
                                    replyMessage = replyMessage,
                                    replyUserData = replyUserData,
                                    onMessageClick = { messageid ->
                                        // Navigate to Chat Main Screen
                                    }
                                )
                            }
                        }
                    }
                }
                SearchScreenMode.FILTER_USERS -> {
                    LazyColumn(
                        reverseLayout = false,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(chatUsersState) { user ->
                            SearchUserItem(
                                userData = user,
                                onUserClick = { userId, userName ->
                                    filters.removeIf { it is SearchFilter.FromUser }
                                    filters.add(SearchFilter.FromUser(userId, "from user: $userName"))
                                    currentMode = SearchScreenMode.MESSAGES
                                }
                            )
                        }
                    }
                }
                SearchScreenMode.FILTER_DIRECTION -> {
                    Row (
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InputChip(
                            selected = false,
                            label = { Text(text = "Newest") },
                            onClick = {
                                filters.removeIf { it is SearchFilter.SortDirection }
                                filters.add(SearchFilter.SortDirection.Descending)
                                currentMode = SearchScreenMode.MESSAGES
                            }
                        )
                        InputChip(
                            selected = false,
                            label = { Text(text = "Oldest") },
                            onClick = {
                                filters.removeIf { it is SearchFilter.SortDirection }
                                filters.add(SearchFilter.SortDirection.Ascending)
                                currentMode = SearchScreenMode.MESSAGES
                            }
                        )
                    }
                }
            }
        }
        if (showBottomSheet.value) {
            SearchFilterBottomSheet(
                onDismiss = { showBottomSheet.value = false },
                onFilterClick = { filterType ->
                    when(filterType) {
                        FilterSelectionType.FROM_USER -> {
                            currentMode = SearchScreenMode.FILTER_USERS
                        }
                        FilterSelectionType.HAS_ATTACHMENTS -> {
                            val newFilter = SearchFilter.HasAttachments
                            if (filters.none { it is SearchFilter.HasAttachments }) {
                                filters.add(newFilter)
                            }
                        }
                        FilterSelectionType.SORT_DIRECTION -> {
                            currentMode = SearchScreenMode.FILTER_DIRECTION
                        }
                    }
                }
            )
        }
    }
}

private enum class SearchScreenMode {
    MESSAGES,
    FILTER_USERS,
    FILTER_DIRECTION
}

enum class FilterSelectionType(val prefix: String, val description: String, val icon: ImageVector) {
    FROM_USER(
        prefix = "from:",
        description = "From User",
        icon = Icons.Default.Person
    ),
    HAS_ATTACHMENTS(
        prefix = "hasAttachments",
        description = "Has Attachments",
        icon = Icons.Default.Email
    ),
    SORT_DIRECTION(
        prefix = "direction:",
        description = "Sort Direction",
        icon = Icons.AutoMirrored.Filled.List
    )
}