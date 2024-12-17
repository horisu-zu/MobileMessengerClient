package com.example.testapp.presentation.main.group

import androidx.compose.runtime.Composable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.templates.Avatar

@Composable
fun GroupUserAdd(
    usersList: List<UserResponse>,
    onUserSelectionChange: (List<String>) -> Unit
) {
    var selectedUserIds by remember { mutableStateOf(setOf<String>()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(usersList) { user ->
            val isSelected = selectedUserIds.contains(user.userId)

            UserItem(
                userData = user,
                isSelected = isSelected,
                onSelectChange = { isChecked ->
                    selectedUserIds = if (isChecked) {
                        selectedUserIds + user.userId
                    } else {
                        selectedUserIds - user.userId
                    }
                    onUserSelectionChange(selectedUserIds.toList())
                }
            )
        }
    }
}

@Composable
fun UserItem(
    userData: UserResponse,
    isSelected: Boolean,
    onSelectChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp)
            .clip(CircleShape),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.Start)
    ) {
        Avatar(avatarUrl = userData.avatarUrl)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${userData.firstName} ${userData.lastName}")
            Text(text = "@${userData.nickname}", style = MaterialTheme.typography.bodySmall)
        }
        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelectChange
        )
    }
}
