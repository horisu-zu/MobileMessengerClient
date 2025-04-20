package com.example.testapp.presentation.chat.admin

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.testapp.data.local.entity.ChatRestrictionEntity.Companion.toEntity
import com.example.testapp.domain.dto.chat.FilterCriterion
import com.example.testapp.domain.dto.chat.RestrictionExpireType
import com.example.testapp.domain.dto.chat.SearchMenuOption
import com.example.testapp.domain.dto.chat.SortBy
import com.example.testapp.domain.dto.chat.SortDirection
import com.example.testapp.domain.dto.chat.SortField
import com.example.testapp.domain.models.chat.ChatRestriction
import com.example.testapp.presentation.chat.main.RestrictionBottomSheet
import com.example.testapp.presentation.chat.search.ChatSearchAppBar
import com.example.testapp.presentation.chat.search.FilterDirectionScreen
import com.example.testapp.presentation.chat.search.FilterUsersScreen
import com.example.testapp.presentation.chat.search.SearchFilterBottomSheet
import com.example.testapp.presentation.viewmodel.chat.ChatRestrictionViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAdminScreen(
    chatId: String?,
    currentUserId: String?,
    chatNavController: NavController,
    userViewModel: UserViewModel,
    chatRestrictionViewModel: ChatRestrictionViewModel = hiltViewModel()
) {
    var currentMode by remember { mutableStateOf(RestrictionSearchScreenMode.RESTRICTIONS) }
    val scope = rememberCoroutineScope()
    val pagerState: PagerState = rememberPagerState { RestrictionExpireType.entries.size }
    val tabs = RestrictionExpireType.entries.map { type ->
        val displayName = type.name.lowercase().replaceFirstChar { it.uppercase() }
        Pair(type, displayName)
    }
    var isRefreshing by remember { mutableStateOf(false) }
    val usersState by userViewModel.participantsState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val filters = remember { mutableStateListOf<FilterCriterion>() }
    var sortBy: SortBy? by remember { mutableStateOf(null) }
    var selectedRestriction by remember { mutableStateOf<ChatRestriction?>(null) }
    val showRestrictionBottomSheet = remember { mutableStateOf(false) }
    val showFilterBottomSheet = remember { mutableStateOf(false) }

    var currentFilterCriterionType: KClass<out FilterCriterion>? = null

    val restrictionSearchOptions: List<SearchMenuOption> = remember {
        listOf(
            SearchMenuOption.AddFilter(
                FilterCriterion.FromUser::class,
                prefix = "from:", description = "From User", icon = Icons.Default.Person
            ),
            SearchMenuOption.AddFilter(
                FilterCriterion.AppliedTo::class,
                prefix = "applied to:", description = "Applied To", icon = Icons.Default.AccountBox
            ),
            SearchMenuOption.ConfigureSort
        )
    }

    Scaffold(
        topBar = {
            ChatSearchAppBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { newValue ->
                    searchQuery = newValue
                },
                filters = filters,
                sortBy = sortBy,
                onBackClick = { chatNavController.popBackStack() },
                onFilterIconClick = { showFilterBottomSheet.value = true },
                onFilterRemove = { searchFilter ->
                    filters.remove(searchFilter)
                },
                onSortRemove = { sortBy = null }
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(
                top = innerPadding.calculateTopPadding(),
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
            )
        ) {
            when(currentMode) {
                RestrictionSearchScreenMode.RESTRICTIONS -> {
                    ChatRestrictionTabRow(
                        tabs = tabs.map { it.second },
                        selectedTab = pagerState.currentPage,
                        onTabSelected = { page ->
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    page = page,
                                    animationSpec = tween(
                                        durationMillis = 500,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            }
                        }
                    )
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        val expireType = tabs[page].first
                        val restrictionItems = chatId?.let { chatId ->
                            chatRestrictionViewModel.getChatRestrictions(
                                chatId = chatId,
                                expire = expireType,
                                filters = filters,
                                sortBy = sortBy ?: SortBy(SortField.Date, SortDirection.Descending)
                            )
                        }?.collectAsLazyPagingItems()

                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = {
                                scope.launch {
                                    isRefreshing = true
                                    restrictionItems?.refresh()
                                    delay(300)
                                    isRefreshing = false
                                }
                            }
                        ) {
                            restrictionItems?.let {
                                ChatRestrictionsPage(
                                    restrictionItems = it,
                                    expireType = expireType,
                                    usersList = usersState.data ?: emptyList(),
                                    onUpdateRestriction = { restriction ->
                                        selectedRestriction = restriction
                                        showRestrictionBottomSheet.value = true
                                    },
                                    onClearRestriction = { restrictionId ->
                                        chatRestrictionViewModel.updateRestriction(restrictionId, Duration.ZERO)
                                    }
                                )
                            }
                        }
                    }
                }
                RestrictionSearchScreenMode.FILTER_USERS -> {
                    FilterUsersScreen(
                        usersList = usersState.data ?: emptyList(),
                        onUserClick = { userId, userName ->
                            val userFilter = when(currentFilterCriterionType) {
                                FilterCriterion.FromUser::class -> FilterCriterion.FromUser(userId, userName)
                                else -> FilterCriterion.AppliedTo(userId, userName)
                            }

                            filters.removeIf { filter -> filter::class == currentFilterCriterionType }
                            filters.add(userFilter)

                            currentMode = RestrictionSearchScreenMode.RESTRICTIONS
                        }
                    )
                }
                RestrictionSearchScreenMode.FILTER_DIRECTION -> {
                    FilterDirectionScreen(
                        sortBy = sortBy,
                        onClick = { sortCriterion ->
                            sortBy = sortCriterion
                            currentMode = RestrictionSearchScreenMode.RESTRICTIONS
                        }
                    )
                }
            }
        }

        showRestrictionBottomSheet.value.let {
            selectedRestriction?.let {
                RestrictionBottomSheet(
                    chatId = chatId!!,
                    existingRestriction = selectedRestriction,
                    userData = usersState.data!!.first { it.userId == selectedRestriction!!.userId },
                    currentUserId = currentUserId,
                    onDismiss = {
                        showRestrictionBottomSheet.value = false
                        selectedRestriction = null
                    },
                    onUpdate = { restriction ->
                        chatRestrictionViewModel.updateLocalRestriction(restriction.toEntity())
                    }
                )
            }
        }
        if(showFilterBottomSheet.value) {
            SearchFilterBottomSheet(
                options = restrictionSearchOptions,
                onOptionSelected = { selectedOption ->
                    when(selectedOption) {
                        is SearchMenuOption.AddFilter -> {
                            currentMode = RestrictionSearchScreenMode.FILTER_USERS
                            currentFilterCriterionType = selectedOption.filterCriterionType
                        }
                        is SearchMenuOption.ConfigureSort -> {
                            currentMode = RestrictionSearchScreenMode.FILTER_DIRECTION
                        }
                    }
                    showFilterBottomSheet.value = false
                },
                onDismiss = { showFilterBottomSheet.value = false },
            )
        }
    }
}

private enum class RestrictionSearchScreenMode {
    RESTRICTIONS,
    FILTER_USERS,
    FILTER_DIRECTION
}