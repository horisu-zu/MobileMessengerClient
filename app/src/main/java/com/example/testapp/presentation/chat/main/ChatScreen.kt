package com.example.testapp.presentation.chat.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.GroupRole
import com.example.testapp.presentation.chat.bottomsheet.chat.ChatBottomSheet
import com.example.testapp.presentation.chat.message.MessageList
import com.example.testapp.presentation.main.notification.InAppNotificationHost
import com.example.testapp.presentation.viewmodel.gallery.MediaViewModel
import com.example.testapp.presentation.user.bottomsheet.UserBottomSheet
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.presentation.viewmodel.message.MessageInputViewModel
import com.example.testapp.presentation.viewmodel.message.MessageViewModel
import com.example.testapp.presentation.viewmodel.notification.NotificationViewModel
import com.example.testapp.presentation.viewmodel.reaction.ReactionViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import java.util.Locale

@Composable
fun ChatScreen(
    chatId: String?,
    currentUser: UserResponse?,
    userViewModel: UserViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    messageViewModel: MessageViewModel = hiltViewModel(),
    reactionViewModel: ReactionViewModel = hiltViewModel(),
    messageInputViewModel: MessageInputViewModel = hiltViewModel(),
    mediaViewModel: MediaViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel,
    reactionUrls: List<String>,
    mainNavController: NavController,
    chatNavController: NavController
) {
    val context = LocalContext.current
    val userState by userViewModel.participantsState.collectAsState()
    val userStatusState by userViewModel.userStatusState.collectAsState()
    val metadataState by chatViewModel.chatMetadataState.collectAsState()
    val messagesState by messageViewModel.chatMessagesState.collectAsState()
    val chatState by chatViewModel.chatState.collectAsState()
    val participantsState by chatViewModel.chatParticipantsState.collectAsState()
    val userRestrictionsState by chatViewModel.chatUserRestrictionsState.collectAsState()
    val reactionsState by reactionViewModel.chatReactionsState.collectAsState()
    val messageInputState = messageInputViewModel.messageInputState.collectAsState()

    val showBottomSheet = remember { mutableStateOf(false) }
    val showPersonalBottomSheet = remember { mutableStateOf(false) }
    val showRestrictionBottomSheet = remember { mutableStateOf(false) }
    val selectedUser = remember { mutableStateOf<UserResponse?>(null) }

    LaunchedEffect(chatId) {
        chatId?.let {
            currentUser?.userId?.let { userId ->
                messageInputViewModel.initialize(chatId, userId)
                chatViewModel.getUserRestrictionsInChat(chatId, userId)
            }
        }
    }

    val otherUserData = userState.data?.find { it.userId != currentUser?.userId }
    val otherUserStatus by remember(userStatusState) {
        derivedStateOf {
            userStatusState[otherUserData?.userId]
        }
    }

    Scaffold(
        topBar = {
            ChatAppBar(
                currentUserRole = participantsState.data?.find { it.userId == currentUser?.userId }?.role
                    ?: GroupRole.MEMBER,
                chatMetadata = metadataState.data,
                userData = otherUserData,
                userStatus = otherUserStatus,
                onAvatarClick = {
                    if (metadataState.data != null) {
                        showBottomSheet.value = true
                    } else {
                        selectedUser.value = otherUserData
                        showPersonalBottomSheet.value = true
                    }
                },
                onBackClick = { mainNavController.popBackStack() },
                onPinClick = { /**/ },
                onAdminClick = { chatNavController.navigate("chatScreenAdmin") },
                onInfoClick = { showBottomSheet.value = true },
                onSearchClick = { chatNavController.navigate("chatScreenSearch") },
                onLeaveClick = {
                    chatId?.let { chatId ->
                        currentUser?.userId?.let { userId ->
                            chatViewModel.leaveGroupChat(chatId, userId)
                        }
                    }
                },
                context = context
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                )
        ) {
            InAppNotificationHost(
                onNotificationClick = { chatId ->
                    if(chatId != chatState.data?.chatId) {
                        mainNavController.navigate("chatScreen/${chatId}")
                    }
                },
                notificationViewModel = notificationViewModel,
                modifier = Modifier.align(Alignment.TopCenter).zIndex(1f)
            )
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    userState.data?.let {
                        MessageList(
                            chatType = chatState.data?.chatType ?: com.example.testapp.domain.models.chat.ChatType.PERSONAL,
                            currentUserRole = participantsState.data?.find { it.userId == currentUser?.userId }?.role ?: GroupRole.MEMBER,
                            currentUserId = currentUser?.userId ?: "bruh",
                            messages = messagesState.messages,
                            replyMessages = messagesState.replyMessages,
                            usersData = it.associateBy { user -> user.userId },
                            reactionsMap = reactionsState.data ?: emptyMap(),
                            attachments = messagesState.attachments,
                            reactionUrls = reactionUrls,
                            hasMorePages = messagesState.hasMorePages,
                            onAvatarClick = { userResponse ->
                                selectedUser.value = userResponse
                                showPersonalBottomSheet.value = true
                            },
                            onReplyClick = { message ->
                                messageInputViewModel.startReplying(message)
                            },
                            onEditClick = { message ->
                                messageInputViewModel.startEditing(message)
                            },
                            onDeleteClick = { messageId ->
                                messageViewModel.deleteMessage(messageId)
                            },
                            onReactionClick = { messageId, userId, emoji ->
                                reactionViewModel.toggleReaction(messageId, userId, emoji)
                            },
                            onTranslateClick = { messageId ->
                                messageViewModel.translateMessage(messageId, Locale.getDefault())
                            },
                            onReactionLongClick = { /*TODO*/ },
                            onAddRestriction = { userResponse ->
                                selectedUser.value = userResponse
                                showRestrictionBottomSheet.value = true
                            },
                            onLoadMore = {
                                messageViewModel.getMessagesForChat(chatId!!, currentUser?.userId ?: "")
                                reactionViewModel.loadReactionsForChat(chatId)
                                reactionViewModel.setMessageIdsInChat(
                                    messagesState.messages.mapNotNull { it.messageId }
                                )
                            },
                            onMarkMessage = { messageId ->
                                messageViewModel.markMessageAsRead(chatId!!, messageId, currentUser?.userId ?: "")
                            },
                        )
                    }
                }
                messageInputState.value?.let {
                    Log.d("MessageInput", "Restrictions: ${userRestrictionsState.data}")
                    MessageInput(
                        userData = userState.data?.associateBy { it.userId } ?: emptyMap(),
                        mediaViewModel = mediaViewModel,
                        messageInputState = it,
                        currentUserRestriction = userRestrictionsState.data,
                        onSendClick = { messageInputViewModel.sendMessage(context = context) },
                        onMessageInputChange = { newValue ->
                            messageInputViewModel.setMessage(newValue)
                        },
                        onClearEditing = { messageInputViewModel.clearEditing() },
                        onClearReplying = { messageInputViewModel.clearReplying() },
                        onAddAttachment = { newAttachment ->
                            messageInputViewModel.addAttachment(newAttachment)
                        },
                        onClearAttachment = { attachment ->
                            messageInputViewModel.clearAttachment(attachment)
                        }, context = context,
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)
                            .navigationBarsPadding().imePadding()
                    )
                }
            }

            if (showBottomSheet.value) {
                Log.d("ChatScreen", "Metadata: ${metadataState.data}, Users: ${userState.data}")
                metadataState.data?.let { metadata ->
                    chatState.data?.let { chatData ->
                        userState.data?.let { usersList ->
                            ChatBottomSheet(
                                chatData = chatData,
                                chatMetadata = metadata,
                                usersList = usersList,
                                userStatusList = userStatusState,
                                onDismiss = { showBottomSheet.value = false },
                                showBottomSheet = showBottomSheet.value,
                                context = context
                            )
                        }
                    }
                }
            }
            if(showPersonalBottomSheet.value) {
                selectedUser.value.let { userData ->
                    otherUserStatus?.let { userStatus ->
                        if (userData != null) {
                            UserBottomSheet(
                                userData = userData,
                                userStatus = userStatus,
                                isInChat = true,
                                chatParticipant = participantsState.data?.find { it?.userId == userData.userId },
                                avatarUrl = metadataState.data?.avatar,
                                onDismiss = { showPersonalBottomSheet.value = false },
                                showBottomSheet = showPersonalBottomSheet.value,
                                onNavigateToChat = { /*TODO*/ },
                                context = context
                            )
                        }
                    }
                }
            }
            if(showRestrictionBottomSheet.value) {
                selectedUser.value?.let { userData ->
                    chatId?.let { chatId ->
                        RestrictionBottomSheet(
                            chatId = chatId,
                            currentUserId = currentUser?.userId,
                            userData = userData,
                            onDismiss = { showRestrictionBottomSheet.value = false }
                        )
                    }
                }
            }
        }
    }
}