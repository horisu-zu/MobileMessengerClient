package com.example.testapp.presentation.main.group.groupcustomization

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.GroupRole
import com.example.testapp.presentation.templates.Avatar

@Composable
fun MemberItem(
    userData: UserResponse,
    role: GroupRole,
    onRoleChange: (GroupRole) -> Unit,
    isConfirmation: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.Start)
    ) {
        Avatar(avatarUrl = userData.avatarUrl)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${userData.firstName} ${userData.lastName}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = "@${userData.nickname}", style = MaterialTheme.typography.bodySmall)
        }
        Box {
            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(
                    text = role.name,
                    modifier = Modifier
                        .pointerInput(isConfirmation) {
                            if (!isConfirmation) {
                                detectTapGestures(onTap = { expanded = true })
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                GroupRole.entries.forEach { roleOption ->
                    DropdownMenuItem(
                        onClick = {
                            onRoleChange(roleOption)
                            expanded = false
                        },
                        enabled = roleOption != GroupRole.ADMIN,
                        text = { Text(roleOption.name) }
                    )
                }
            }
        }
    }
}