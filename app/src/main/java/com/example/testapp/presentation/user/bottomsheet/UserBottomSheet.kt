package com.example.testapp.presentation.user.bottomsheet

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.ChatParticipant
import com.example.testapp.domain.models.user.UserStatus
import com.example.testapp.presentation.viewmodel.user.UserPortraitViewModel
import com.example.testapp.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBottomSheet(
    userData: UserResponse,
    userStatus: UserStatus,
    isInChat: Boolean,
    chatParticipant: ChatParticipant? = null,
    avatarUrl: String? = null,
    showBottomSheet: Boolean = false,
    onDismiss: () -> Unit,
    context: Context,
    onNavigateToChat: (String) -> Unit,
    userPortraitViewModel: UserPortraitViewModel = hiltViewModel()
) {
    val portraitState by userPortraitViewModel.portraitState.collectAsState()
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
                    isInChat = isInChat,
                    userData = userData,
                    onMessageClick = onNavigateToChat,
                    onPortraitClick = { userId ->
                        if(portraitState is Resource.Idle) {
                            chatParticipant?.chatId?.let { chatId ->
                                userPortraitViewModel.createUserPortrait(chatId, userId)
                            }
                        }
                    },
                )
                if (portraitState !is Resource.Idle) {
                    PortraitFragment(
                        portraitState = portraitState,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}