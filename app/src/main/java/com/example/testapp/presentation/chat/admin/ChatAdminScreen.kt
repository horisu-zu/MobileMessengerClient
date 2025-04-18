package com.example.testapp.presentation.chat.admin

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.testapp.data.local.entity.ChatRestrictionEntity.Companion.toEntity
import com.example.testapp.domain.dto.chat.RestrictionExpireType
import com.example.testapp.domain.models.chat.ChatRestriction
import com.example.testapp.presentation.chat.main.RestrictionBottomSheet
import com.example.testapp.presentation.templates.BasicTextInput
import com.example.testapp.presentation.viewmodel.chat.ChatRestrictionViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAdminScreen(
    chatId: String?,
    currentUserId: String?,
    chatNavController: NavController,
    userViewModel: UserViewModel,
    chatRestrictionViewModel: ChatRestrictionViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val pagerState: PagerState = rememberPagerState { RestrictionExpireType.entries.size }
    val tabs = RestrictionExpireType.entries.map { type ->
        val displayName = type.name.lowercase().replaceFirstChar { it.uppercase() }
        Pair(type, displayName)
    }
    var isRefreshing by remember { mutableStateOf(false) }
    val search = remember { mutableStateOf("") }
    val usersState by userViewModel.participantsState.collectAsState()

    var selectedRestriction by remember { mutableStateOf<ChatRestriction?>(null) }
    val showBottomSheet = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BasicTextInput(
                        value = search.value,
                        onValueChange = { newValue ->
                            search.value = newValue
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { chatNavController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            ChatRestrictionTabRow(
                tabs = tabs.map{ it.second },
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
                val restrictionItems = chatId?.let {
                    chatRestrictionViewModel.getChatRestrictionsFlow(it, expireType)
                }?.collectAsLazyPagingItems()

                LaunchedEffect(Unit) {
                    chatRestrictionViewModel.updateEvent.collect { affectedType ->
                        restrictionItems?.refresh()
                    }
                }

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
                                showBottomSheet.value = true
                            },
                            onClearRestriction = { restrictionId ->
                                chatRestrictionViewModel.updateRestriction(restrictionId, Duration.ZERO)
                            }
                        )
                    }
                }
            }
        }

        showBottomSheet.value.let {
            selectedRestriction?.let {
                RestrictionBottomSheet(
                    chatId = chatId!!,
                    existingRestriction = selectedRestriction,
                    userData = usersState.data!!.first { it.userId == selectedRestriction!!.userId },
                    currentUserId = currentUserId,
                    onDismiss = {
                        showBottomSheet.value = false
                        selectedRestriction = null
                    },
                    onUpdate = { restriction ->
                        chatRestrictionViewModel.updateLocalRestriction(restriction.toEntity())
                    }
                )
            }
        }
    }
}