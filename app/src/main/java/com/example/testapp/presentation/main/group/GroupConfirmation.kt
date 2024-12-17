package com.example.testapp.presentation.main.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import chat.service.course.dto.GroupChatRequest
import com.example.testapp.domain.models.chat.GroupRole
import com.example.testapp.R
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.main.group.groupconfirmation.GroupInfoItem
import com.example.testapp.presentation.main.group.groupcustomization.AvatarComponent
import com.example.testapp.presentation.main.group.groupcustomization.MemberItem

@Composable
fun GroupConfirmation(
    membersList: List<UserResponse>,
    chatRequestData: GroupChatRequest,
    currentUserId: String
) {
    val membersWithRoles by remember {
        mutableStateOf(
            membersList.associateWith { member ->
                if (member.userId == currentUserId) GroupRole.ADMIN else GroupRole.MEMBER
            }.toList().sortedBy { (_, role) ->
                when (role) {
                    GroupRole.ADMIN -> 1
                    else -> 2
                }
            }.toMap()
        )
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Group Chat Info",
                style = MaterialTheme.typography.headlineSmall
            )
            AvatarComponent(
                avatar = chatRequestData.avatarUrl,
                onAvatarClick = {},
                isClickable = false
            )
            GroupInfoItem(
                icon = painterResource(id = R.drawable.ic_title),
                label = "Chat Title",
                value = chatRequestData.name
            )
            if(!chatRequestData.description.isNullOrEmpty()) {
                GroupInfoItem(
                    icon = painterResource(id = R.drawable.ic_description),
                    label = "Chat Description",
                    value = chatRequestData.description
                )
            }
            GroupInfoItem(
                icon = painterResource(id = R.drawable.ic_members),
                label = "Chat Members Max Count",
                value = chatRequestData.maxMembers.toString()
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 12.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                membersWithRoles.forEach { (member, role) ->
                    MemberItem(
                        userData = member,
                        role = role,
                        onRoleChange = {},
                        isConfirmation = true
                    )
                }
            }
        }
    }
}