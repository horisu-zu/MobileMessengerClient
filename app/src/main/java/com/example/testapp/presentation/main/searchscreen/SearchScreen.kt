package com.example.testapp.presentation.main.searchscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import chat.service.course.dto.ChatJoinRequest
import com.example.testapp.R
import com.example.testapp.presentation.main.SearchType
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.Resource
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    onSearchTypeChange: (SearchType) -> Unit,
    currentUserId: String?,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    mainNavController: NavController
) {
    val tabs = listOf(
        stringResource(R.string.tab_users),
        stringResource(R.string.tab_groups)
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    var isFabExpanded by remember { mutableStateOf(false) }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { if (isFabExpanded) isFabExpanded = false }
    ) {
        val (tabRow, pager, addFab) = createRefs()

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.constrainAs(tabRow) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier
                        .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                        .padding(horizontal = 48.dp)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                            onSearchTypeChange(
                                when (index) {
                                    1 -> SearchType.GROUPS
                                    else -> SearchType.USERS
                                }
                            )
                        }
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.constrainAs(pager) {
                top.linkTo(tabRow.bottom)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                height = Dimension.fillToConstraints
            },
        ) { page ->
            when(page) {
                0 -> UserSearchScreen(
                    currentUserId = currentUserId,
                    userViewModel = userViewModel,
                    onNavigateToChat = {
                        //mainNavController.navigate("chatScreen")
                    }
                )
                1 -> {
                    GroupSearchScreen(
                        chatViewModel = chatViewModel,
                        onRequestClick = { chatId, inviteCode ->
                            scope.launch {
                                chatViewModel.joinChat(chatId, ChatJoinRequest(currentUserId!!, inviteCode)).collect { result ->
                                    when (result) {
                                        is Resource.Success -> {
                                            mainNavController.navigate("chatScreen/${chatId}")
                                        }
                                        is Resource.Error -> {}
                                        is Resource.Loading -> {}
                                    }
                                }
                            }
                        },
                        onJoinClick = {}
                    )
                    // GroupSearchScreen will be here once implemented
                }
            }
        }
        AddFab(
            mainNavController = mainNavController,
            expanded = isFabExpanded,
            onExpandedChange = { isFabExpanded = it },
            modifier = Modifier
                .constrainAs(addFab) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
                .padding(16.dp)
        )
    }
}