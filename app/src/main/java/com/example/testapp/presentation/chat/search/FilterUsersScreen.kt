package com.example.testapp.presentation.chat.search

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.testapp.domain.dto.user.UserResponse

@Composable
fun FilterUsersScreen(
    usersList: List<UserResponse>,
    onUserClick: (String, String) -> Unit
) {
    LazyColumn(
        reverseLayout = false,
        modifier = Modifier.fillMaxSize()
    ) {
        items(usersList) { user ->
            SearchUserItem(
                userData = user,
                onUserClick = { userId, userName -> onUserClick(userId, userName) }
            )
        }
    }
}