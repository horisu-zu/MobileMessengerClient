package com.example.testapp.presentation.main.group

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import chat.service.course.dto.GroupChatRequest
import com.example.testapp.domain.models.chat.GroupRole
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.main.group.groupcustomization.AvatarComponent
import com.example.testapp.presentation.main.group.groupcustomization.ImageSourceDialog
import com.example.testapp.presentation.main.group.groupcustomization.MemberItem
import com.google.firebase.storage.FirebaseStorage

@Composable
fun GroupCustomization(
    membersList: List<UserResponse>,
    chatRequest: GroupChatRequest,
    onRequestChange: (GroupChatRequest) -> Unit
) {
    var chatName by remember { mutableStateOf(chatRequest.name) }
    var description by remember { mutableStateOf(chatRequest.description) }
    var avatar by remember { mutableStateOf(chatRequest.avatarUrl) }
    var maxMembers by remember { mutableStateOf(chatRequest.maxMembers.toString()) }
    var isPublic by remember { mutableStateOf(chatRequest.isPublic) }
    var showDialog by remember { mutableStateOf(false) }
    //var memberRoles by remember { mutableStateOf(chatMetadata.members.associate { it.userId to it.role }) }

    val storage = FirebaseStorage.getInstance().reference
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val fileName = it.lastPathSegment ?: "default_avatar"
            val imageRef = storage.child("groups/avatars/$fileName")
            val uploadTask = imageRef.putFile(it)

            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    avatar = uri.toString()
                    Log.d("Avatar URL", avatar)
                }
            }
        }
    }

    LaunchedEffect(chatName, description, avatar, maxMembers, isPublic) {
        val maxMembersInt = maxMembers.toIntOrNull() ?: 2
        val updatedMetadata = chatRequest.copy(
            name = chatName,
            description = description,
            avatarUrl = avatar,
            maxMembers = maxMembersInt,
            isPublic = isPublic
        )
        onRequestChange(updatedMetadata)
        Log.d("GroupCustomization", "Updated ChatMetadata: $updatedMetadata")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarComponent(
                avatar = avatar,
                onAvatarClick = { showDialog = true },
                isClickable = true
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = chatName,
                    onValueChange = { chatName = it },
                    placeholder = { Text("Name") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description ?: "",
                    onValueChange = { description = it },
                    placeholder = { Text("Description") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 3
                )

                OutlinedTextField(
                    value = maxMembers,
                    onValueChange = { newMaxMembers ->
                        maxMembers = try {
                            when {
                                newMaxMembers.isEmpty() -> ""
                                newMaxMembers.toInt() > 100 -> "100"
                                newMaxMembers.toInt() < 2 -> "2"
                                else -> newMaxMembers
                            }
                        } catch (e: NumberFormatException) {
                            maxMembers
                        }
                    },
                    placeholder = { Text("Max Members") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Group Privacy"
                    )
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it }
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            membersList.forEach { member ->
                MemberItem(
                    userData = member,
                    role = GroupRole.MEMBER,
                    onRoleChange = {},
                    isConfirmation = true
                )
            }
        }
    }

    if (showDialog) {
        ImageSourceDialog(
            onDismiss = { showDialog = false },
            onGallerySelected = {
                launcher.launch("image/*")
                showDialog = false
            },
            onLinkEntered = { link ->
                avatar = link
                showDialog = false
            }
        )
    }
}