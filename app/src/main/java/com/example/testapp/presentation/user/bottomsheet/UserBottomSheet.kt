package com.example.testapp.presentation.user.bottomsheet

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.Chat
import com.example.testapp.domain.models.chat.ChatParticipant
import com.example.testapp.domain.models.user.UserStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBottomSheet(
    currentUserId: String,
    userData: UserResponse,
    userStatus: UserStatus,
    chatParticipant: ChatParticipant? = null,
    avatarUrl: String? = null,
    showBottomSheet: Boolean = false,
    onDismiss: () -> Unit,
    context: Context
    //onNavigateToChat: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    if(showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = Modifier.heightIn(min = 480.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserCard(
                    userData = userData,
                    userStatus = userStatus,
                    context = context
                )
                avatarUrl?.let {
                    MemberInfo(
                        userData = userData,
                        chatParticipant = chatParticipant!!,
                        avatarUrl = it
                    )
                }
                UserInteraction(
                    userData = userData,
                    onMessageClick = { },
                    onReportClick = { }
                )
            }
        }
    }
}