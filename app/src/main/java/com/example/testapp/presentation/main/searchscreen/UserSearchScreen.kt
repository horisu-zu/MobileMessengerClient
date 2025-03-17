package com.example.testapp.presentation.main.searchscreen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.templates.Avatar
import com.example.testapp.presentation.user.bottomsheet.UserBottomSheet
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.Resource

@Composable
fun UserSearchScreen(
    currentUserId: String?,
    userViewModel: UserViewModel,
    //chatViewModel: ChatViewModel,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    var showUserBottomSheet by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf("") }

    val searchResults by userViewModel.usersState.collectAsState()
    val userStatuses by userViewModel.userStatusState.collectAsState()

    val selectedUser = (searchResults as? Resource.Success)?.data?.firstOrNull { it.userId == selectedUserId }
    Log.d("UserSearchScreen", "Statuses: $userStatuses")
    val userStatus = userStatuses[selectedUserId]

    LaunchedEffect(selectedUserId) {
        if (selectedUserId.isNotEmpty()) {
            Log.d("UserSearchScreen", "Selected userId: $selectedUserId")
        }
    }

    LaunchedEffect(showUserBottomSheet) {
        Log.d("UserSearchScreen", "showUserBottomSheet: $showUserBottomSheet")
    }

    when(val resource = searchResults) {
        is Resource.Loading -> {}
        is Resource.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val userList = (resource.data) ?: emptyList()

                items(userList) { user ->
                    UserListItem(
                        userData = user,
                        onAvatarClick = {
                            selectedUserId = user.userId
                            showUserBottomSheet = true
                        },
                        onClick = { /*onNavigateToChat()*/ }
                    )
                }
            }
        }
        is Resource.Error -> {
            Log.d("User Search Error", "Error: ${resource.message}")
        }
    }

    if (selectedUser != null && userStatus != null) {
        UserBottomSheet(
            userData = selectedUser,
            userStatus = userStatus,
            isInChat = false,
            showBottomSheet = showUserBottomSheet,
            onDismiss = { showUserBottomSheet = false },
            onNavigateToChat = onNavigateToChat,
            context = context
        )
    }
}

@Composable
fun UserListItem(
    userData: UserResponse,
    onAvatarClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarUrl = userData.avatarUrl,
            onClick = onAvatarClick
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${userData.firstName} ${userData.lastName}")
            Text(text = "@${userData.nickname}", style = MaterialTheme.typography.bodySmall)
        }
    }
}