package com.example.testapp.presentation.chat.bottomsheet.chat

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.Chat
import com.example.testapp.domain.models.chat.ChatMetadata
import com.example.testapp.domain.models.user.UserStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomSheet(
    chatData: Chat,
    chatMetadata: ChatMetadata,
    usersList: List<UserResponse>,
    userStatusList: Map<String, UserStatus>,
    onDismiss: () -> Unit,
    showBottomSheet: Boolean,
    context: Context
) {
    val sheetState = rememberModalBottomSheetState()
    //val chatMetadata = chatData.metadata as ChatMetadata

    if(showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = Modifier
                .heightIn(min = 480.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChatCard(chatMetadata = chatMetadata)
                ChatInfo(
                    chatData = chatData,
                    chatMetadata = chatMetadata,
                    context
                )
                MembersFragment(
                    members = usersList,
                    userStatusList = userStatusList,
                    maxMembers = chatMetadata.maxMembers.toString(),
                    context = context
                )
            }
        }
    }
}