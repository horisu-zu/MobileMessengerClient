package com.example.testapp.presentation.main.searchscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.R
import com.example.testapp.domain.models.chat.ChatMetadata
import com.example.testapp.presentation.templates.Avatar
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.utils.Resource

@Composable
fun GroupSearchScreen(
    chatViewModel: ChatViewModel,
    onRequestClick: (String, String) -> Unit,
    onJoinClick: (String) -> Unit
) {
    val chatsState by chatViewModel.groupSearchState.collectAsStateWithLifecycle()
    var showInviteDialog by remember { mutableStateOf(false) }
    var selectedGroupId by remember { mutableStateOf("") }

    when (val resource = chatsState) {
        is Resource.Idle -> { /*TODO*/ }
        is Resource.Loading -> { /*TODO*/ }
        is Resource.Success -> {
            val groupList = resource.data.orEmpty()
            if (groupList.isNotEmpty()) {
                val groupListWithParticipants = groupList.entries.toList()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(groupListWithParticipants) { (group, participantsCount) ->
                        GroupListItem(
                            groupData = group,
                            participantsCount = participantsCount,
                            onJoinClick = onJoinClick,
                            onRequestClick = { groupId ->
                                selectedGroupId = groupId
                                showInviteDialog = true
                            }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "No groups found",
                        modifier = Modifier.align(Alignment.Center),
                        style = TextStyle(fontSize = 18.sp)
                    )
                }
            }
        }
        is Resource.Error -> {
            Text("Error: ${resource.message}")
        }
    }

    if (showInviteDialog) {
        RequestDialog(
            onDismiss = { showInviteDialog = false },
            onSubmit = { inviteCode ->
                onRequestClick(selectedGroupId, inviteCode)
                showInviteDialog = false
            }
        )
    }
}

@Composable
fun GroupListItem(
    groupData: ChatMetadata,
    participantsCount: Int,
    onRequestClick: (String) -> Unit,
    onJoinClick: (String) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clip(CircleShape)
    ) {
        val (avatar, name, memberCount, actionIcon) = createRefs()

        Avatar(
            avatarUrl = groupData.avatar,
            isGroupChat = false,
            modifier = Modifier.constrainAs(avatar) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )

        Text(
            text = groupData.name,
            modifier = Modifier.constrainAs(name) {
                start.linkTo(avatar.end, margin = 18.dp)
                end.linkTo(actionIcon.start, margin = 18.dp)
                top.linkTo(parent.top)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = "$participantsCount/${groupData.maxMembers}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.constrainAs(memberCount) {
                start.linkTo(avatar.end, margin = 18.dp)
                end.linkTo(actionIcon.start, margin = 18.dp)
                top.linkTo(name.bottom)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
            }
        )

        Box(
            modifier = Modifier.constrainAs(actionIcon) {
                end.linkTo(parent.end, margin = 8.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            IconButton(
                onClick = { if (groupData.isPublic) onJoinClick(groupData.chatId)
                    else onRequestClick(groupData.chatId) }
            ) {
                Icon(
                    painter = painterResource(id = if (groupData.isPublic)
                        R.drawable.ic_logo else R.drawable.ic_request),
                    contentDescription = if (groupData.isPublic)
                        "Join Group" else "Closed Group",
                    modifier = Modifier.padding(if(groupData.isPublic) 4.dp else 0.dp),
                    tint = if (groupData.isPublic) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}