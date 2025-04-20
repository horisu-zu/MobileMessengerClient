package com.example.testapp.presentation.chat.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.domain.dto.chat.FilterCriterion
import com.example.testapp.domain.dto.chat.SearchMenuOption
import com.example.testapp.domain.dto.chat.SortBy
import com.example.testapp.presentation.viewmodel.message.ChatSearchViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel

@Composable
fun ChatScreenSearch(
    chatId: String?,
    userViewModel: UserViewModel,
    chatSearchViewModel: ChatSearchViewModel = hiltViewModel(),
    chatNavController: NavController
) {
    var currentMode by remember { mutableStateOf(SearchScreenMode.MESSAGES) }
    var searchQuery by remember { mutableStateOf("") }
    val showBottomSheet = remember { mutableStateOf(false) }

    val searchMessagesState by chatSearchViewModel.searchMessagesState.collectAsState()
    val usersState by userViewModel.participantsState.collectAsState()
    val filters = remember { mutableStateListOf<FilterCriterion>() }
    var sortBy: SortBy? by remember { mutableStateOf(null) }

    val messageSearchOptions: List<SearchMenuOption> = remember {
        listOf(
            SearchMenuOption.AddFilter(
                FilterCriterion.FromUser::class,
                prefix = "from:", description = "From User", icon = Icons.Default.Person
            ),
            SearchMenuOption.AddFilter(
                FilterCriterion.HasAttachments::class,
                prefix = "has:", description = "Has Attachments", icon = Icons.Default.Email
            ),
            SearchMenuOption.ConfigureSort
        )
    }

    fun triggerSearch() {
        chatId?.let { id ->
            chatSearchViewModel.searchMessages(
                chatId = id,
                query = searchQuery,
                filters = filters,
                sortBy = sortBy
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ChatSearchAppBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { newValue ->
                    searchQuery = newValue
                    triggerSearch()
                },
                filters = filters,
                sortBy = sortBy,
                onBackClick = { chatNavController.popBackStack() },
                onFilterIconClick = { showBottomSheet.value = true },
                onFilterRemove = { searchFilter ->
                    filters.remove(searchFilter)
                    triggerSearch()
                },
                onSortRemove = {
                    sortBy = null
                    triggerSearch()
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(
                top = innerPadding.calculateTopPadding(),
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
            )
        ) {
            when(currentMode) {
                SearchScreenMode.MESSAGES -> {
                    LazyColumn(
                        reverseLayout = false,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchMessagesState.messages) { message ->
                            val senderData = usersState.data?.firstOrNull { it.userId == message.senderId }
                            val replyMessage = searchMessagesState.replyMessages[message.replyTo]
                            val replyUserData = usersState.data?.firstOrNull { it.userId == replyMessage?.senderId }
                            val messageAttachments = searchMessagesState.attachments[message.messageId]

                            senderData?.let { userData ->
                                SearchMessageItem(
                                    senderData = userData,
                                    message = message,
                                    messageAttachments = messageAttachments,
                                    replyMessage = replyMessage,
                                    replyUserData = replyUserData,
                                    onMessageClick = { messageid -> }
                                )
                            }
                        }
                    }
                }
                SearchScreenMode.FILTER_USERS -> {
                    FilterUsersScreen(
                        usersList = usersState.data ?: emptyList(),
                        onUserClick = { userId, userName ->
                            val userFilter = FilterCriterion.FromUser(userId, userName)
                            filters.removeIf { it is FilterCriterion.FromUser }
                            filters.add(userFilter)

                            currentMode = SearchScreenMode.MESSAGES
                            triggerSearch()
                        }
                    )
                }
                SearchScreenMode.FILTER_DIRECTION -> {
                    FilterDirectionScreen(
                        sortBy = sortBy,
                        onClick = { sortCriterion ->
                            sortBy = sortCriterion
                            currentMode = SearchScreenMode.MESSAGES
                        }
                    )
                }
            }
        }
        if (showBottomSheet.value) {
            SearchFilterBottomSheet(
                options = messageSearchOptions,
                onOptionSelected = { selectedOption ->
                    when(selectedOption) {
                        is SearchMenuOption.AddFilter -> {
                            if (selectedOption.filterCriterionType == FilterCriterion.HasAttachments::class) {
                                if (filters.none { it is FilterCriterion.HasAttachments }) {
                                    filters.add(FilterCriterion.HasAttachments)
                                    triggerSearch()
                                }
                            } else {
                                when (selectedOption.filterCriterionType) {
                                    FilterCriterion.FromUser::class -> currentMode = SearchScreenMode.FILTER_USERS
                                }
                            }
                        }
                        is SearchMenuOption.ConfigureSort -> {
                            currentMode = SearchScreenMode.FILTER_DIRECTION
                        }
                    }
                    showBottomSheet.value = false
                },
                onDismiss = { showBottomSheet.value = false }
            )
        }
    }
}

private enum class SearchScreenMode {
    MESSAGES,
    FILTER_USERS,
    FILTER_DIRECTION
}