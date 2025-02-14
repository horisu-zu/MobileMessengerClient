package com.example.testapp.presentation.main.group

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import chat.service.course.dto.GroupChatRequest
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.Resource
import kotlinx.coroutines.launch

@Composable
fun GroupAddNavigator(
    currentUser: UserResponse?,
    userViewModel: UserViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    mainNavController: NavController
) {
    var currentStep by remember { mutableIntStateOf(1) }
    val currentUserId = currentUser?.userId as String
    var selectedUserIds by remember { mutableStateOf<List<String>>(listOf()) }
    var chatCreateData by remember {
        mutableStateOf(
            GroupChatRequest(
                creatorId = currentUserId,
                name = "",
                avatarUrl = "",
                participants = selectedUserIds,
                description = null,
                maxMembers = 0,
                isPublic = true
            )
        )
    }

    val totalSteps = 3
    val usersState by userViewModel.participantsState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var membersList by remember { mutableStateOf<List<UserResponse>>(listOf(currentUser)) }
    LaunchedEffect(selectedUserIds) {
        usersState.data?.let { data ->
            membersList = data.filter { it.userId in selectedUserIds } + currentUser
        }
    }

    LaunchedEffect(Unit) {
        chatViewModel.getUserConversations(currentUserId).collect { resource ->
            Log.d("GroupAddNavigator", "Resource received: $resource")
            if (resource is Resource.Success) {
                Log.d("GroupAddNavigator", "Successfully fetched conversations")
                userViewModel.getUsersByIds(resource.data ?: emptyList())
            }
        }
    }

    val handleBackNavigation = {
        when (currentStep) {
            1 -> mainNavController.navigate("mainScreen") {
                popUpTo("mainScreen") { inclusive = false }
            }

            else -> currentStep--
        }
    }

    Scaffold(
        topBar = {
            StepAppBar(
                currentStep = currentStep,
                totalSteps = totalSteps,
                onBackPressed = { handleBackNavigation() },
                onNextPressed = {
                    if (currentStep < 3) {
                        currentStep++
                    } else {
                        scope.launch {
                            chatViewModel.createGroupChat(groupChatRequest = chatCreateData)
                                .collect { resource ->
                                    if (resource is Resource.Success) {
                                        mainNavController.navigate("mainScreen")
                                    }
                                }
                        }
                    }
                },
                isNextEnabled = when (currentStep) {
                    2 -> isChatMetadataValid(chatCreateData)
                    else -> true
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentStep) {
                1 -> {
                    usersState.data?.let { usersList ->
                        GroupUserAdd(
                            usersList = usersList.filter { it.userId != currentUserId },
                            onUserSelectionChange = { selectedUsers ->
                                selectedUserIds = listOf(currentUserId) + selectedUsers
                                Log.d("Selected", "SelectedUserIds: $selectedUserIds")
                                chatCreateData = chatCreateData.copy(
                                    participants = listOf(currentUserId) + selectedUsers
                                )
                            }
                        )
                    }
                }

                2 -> {
                    GroupCustomization(
                        membersList = membersList.filter { it.userId != currentUserId },
                        chatRequest = chatCreateData,
                        onRequestChange = { updatedRequest ->
                            chatCreateData = updatedRequest
                        }
                    )
                }

                3 -> {
                    GroupConfirmation(
                        membersList = membersList,
                        chatRequestData = chatCreateData,
                        currentUserId = currentUserId
                    )
                }
            }
        }
    }
}

fun isChatMetadataValid(request: GroupChatRequest): Boolean {
    return request.avatarUrl.isNotBlank() &&
            request.name.isNotBlank() &&
            request.maxMembers != null
}