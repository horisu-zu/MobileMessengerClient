package com.example.testapp.presentation.chat.main

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.ChatMetadata
import com.example.testapp.domain.models.chat.GroupRole
import com.example.testapp.domain.models.user.UserStatus
import com.example.testapp.presentation.chat.dropdown.ChatDropdown
import com.example.testapp.presentation.templates.Avatar
import com.example.testapp.utils.Defaults.calculateUserStatus
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppBar(
    currentUserRole: GroupRole,
    chatMetadata: ChatMetadata?,
    userData: UserResponse?,
    userStatus: UserStatus?,
    onAvatarClick: () -> Unit,
    onBackClick: () -> Unit,
    onPinClick: () -> Unit,
    onAdminClick: () -> Unit,
    onInfoClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLeaveClick: () -> Unit,
    context: Context
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    TopAppBar(title = {
        ChatInfoHeader(
            chatData = chatMetadata,
            userData = userData,
            userStatus = userStatus,
            onAvatarClick = onAvatarClick,
            context = context
        )
    },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onPinClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pin),
                    contentDescription = "Pinned Messages",
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { dropdownExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }

            ChatDropdown(
                isGroupChat = chatMetadata != null,
                isAdmin = currentUserRole != GroupRole.MEMBER,
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                onAdminClick = {
                    dropdownExpanded = false
                    onAdminClick()
                },
                onInfoClick = {
                    dropdownExpanded = false
                    onInfoClick()
                },
                onSearchClick = {
                    dropdownExpanded = false
                    onSearchClick()
                },
                onLeaveClick = {
                    dropdownExpanded = false
                    onLeaveClick()
                }
            )
        }
    )
}

@Composable
fun ChatInfoHeader(
    chatData: ChatMetadata?,
    userData: UserResponse?,
    userStatus: UserStatus?,
    onAvatarClick: () -> Unit,
    context: Context
) {
    when {
        chatData != null -> {
            GroupChatHeader(
                chatData = chatData,
                onAvatarClick = onAvatarClick
            )
        }

        else -> {
            PersonalChatHeader(
                userData = userData,
                userStatus = userStatus,
                onAvatarClick = onAvatarClick,
                context = context
            )
        }
    }
}

@Composable
fun GroupChatHeader(
    chatData: ChatMetadata,
    onAvatarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarUrl = chatData.avatar,
            isGroupChat = true,
            onClick = onAvatarClick
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = chatData.name,
                style = MaterialTheme.typography.titleSmall
            )
            /*Text(
                text = "${chatData.members.size} members",
                style = MaterialTheme.typography.bodySmall
            )*/
        }
    }
}

@Composable
fun PersonalChatHeader(
    userData: UserResponse?,
    userStatus: UserStatus?,
    onAvatarClick: () -> Unit,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarUrl = userData?.avatarUrl ?: "empty",
            onClick = onAvatarClick
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = "${userData?.firstName ?: ""} ${userData?.lastName ?: ""}",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            UserStatus(
                context = context,
                userStatus = userStatus
            )
        }
    }
}

@Composable
fun UserStatus(context: Context, userStatus: UserStatus?) {
    var currentStatus by remember { mutableStateOf(calculateUserStatus(context, userStatus)) }

    LaunchedEffect(userStatus) {
        while (true) {
            currentStatus = calculateUserStatus(context, userStatus)
            delay(60000L)
        }
    }

    Text(
        text = currentStatus,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
