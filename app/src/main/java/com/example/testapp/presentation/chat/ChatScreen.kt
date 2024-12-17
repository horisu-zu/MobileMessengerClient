package com.example.testapp.presentation.chat

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.testapp.domain.dto.message.MessageRequest
import com.example.testapp.domain.dto.message.MessageUpdateRequest
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.domain.models.message.Message
import com.example.testapp.presentation.chat.bottomsheet.chat.ChatBottomSheet
import com.example.testapp.presentation.chat.message.MessageList
import com.example.testapp.presentation.user.bottomsheet.UserBottomSheet
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.presentation.viewmodel.message.MessageViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.Resource
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    chatId: String?,
    currentUser: UserResponse?,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    messageViewModel: MessageViewModel,
    mainNavController: NavController
) {
    val context = LocalContext.current
    val userState by userViewModel.participantsState.collectAsState()
    val userStatusState by userViewModel.userStatusState.collectAsState()
    val metadataState by chatViewModel.chatMetadataState.collectAsState()
    val messagesState by messageViewModel.chatMessagesState.collectAsState()
    val chatState by chatViewModel.chatState.collectAsState()
    val participantsState by chatViewModel.chatParticipantsState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val showBottomSheet = remember { mutableStateOf(false) }
    val showPersonalBottomSheet = remember { mutableStateOf(false) }
    val selectedUser = remember { mutableStateOf<UserResponse?>(null) }
    var messageRequest by remember {
        mutableStateOf(
            chatId?.let { chatId ->
                MessageRequest(
                    chatId = chatId,
                    senderId = currentUser?.userId ?: "Shouldn't happen",
                    message = null,
                    replyTo = null
                )
            }
        )
    }
    var replyToMessage by remember { mutableStateOf<Message?>(null) }
    var editingMessage by remember { mutableStateOf<Message?>(null) }

    var messageAttachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }

    fun clearInputState() {
        messageRequest = messageRequest?.copy(
            message = null,
            replyTo = null
        )
        editingMessage = null
        replyToMessage = null
        messageAttachments = emptyList()
    }

    LaunchedEffect(chatId) {
        chatId?.let {
            chatViewModel.getChatParticipants(chatId)
            chatViewModel.getChatById(chatId)
            chatViewModel.getChatMetadata(chatId)
            messageViewModel.getMessagesForChat(chatId)
        }
    }

    LaunchedEffect(participantsState) {
        if (participantsState is Resource.Success) {
            val userIds = participantsState.data?.map { it.userId }
            userIds?.let {
                userViewModel.getUsersByIds(userIds)
            }
        }
    }

    val sortedUsers by remember(userState.data, userStatusState) {
        derivedStateOf {
            userState.data?.sortedWith(compareByDescending<UserResponse> { user ->
                userStatusState[user.userId]?.let { status ->
                    if (status.onlineStatus) Long.MAX_VALUE else status.lastSeen.toEpochMilli()
                } ?: 0L
            })
        }
    }
    val otherUserData = userState.data?.find { it.userId != currentUser?.userId }
    val otherUserStatus by remember(userStatusState, otherUserData) {
        derivedStateOf {
            userStatusState[otherUserData?.userId]
        }
    }
    /*Log.d("CurrentUserId", currentUser?.userId ?: "Some ID")
    Log.d("OtherUserData", otherUserData?.nickname ?: "Some Nickname")
    Log.d("OtherUserStatus", userStatusState.toString())*/

    Scaffold(
        topBar = {
            ChatAppBar(
                chatMetadata = metadataState.data,
                userData = otherUserData,
                userStatus = otherUserStatus,
                onAvatarClick = {
                    if(metadataState.data != null) {
                        showBottomSheet.value = true
                    } else {
                        selectedUser.value = otherUserData
                        showPersonalBottomSheet.value = true
                    }
                },
                onBackClick = { mainNavController.popBackStack() },
                onPinClick = { /**/ },
                onInfoClick = { showBottomSheet.value = true },
                onLeaveClick = {
                    chatId?.let { chatId ->
                        currentUser?.userId?.let { userId ->
                            coroutineScope.launch {
                                chatViewModel.leaveGroupChat(chatId, userId)
                            }
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
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    when(userState) {
                        is Resource.Success -> {
                            userState.data?.let {
                                MessageList(
                                    currentUserId = currentUser?.userId ?: "bruh",
                                    messages = messagesState.data?.associateBy { it.messageId!! } ?: emptyMap(),
                                    usersData = it.associateBy { user -> user.userId },
                                    onAvatarClick = { userResponse ->
                                        selectedUser.value = userResponse
                                        showPersonalBottomSheet.value = true
                                    },
                                    onReplyClick = { message ->
                                        replyToMessage = message
                                        messageRequest = messageRequest?.copy(replyTo = message.messageId)
                                    },
                                    onEditClick = { message ->
                                        editingMessage = message
                                        messageRequest = messageRequest?.copy(message = message.message)
                                    },
                                    onDeleteClick = { messageId ->
                                        coroutineScope.launch {
                                            messageViewModel.deleteMessage(messageId)
                                        }
                                    }
                                )
                            }
                        }
                        is Resource.Error -> { /**/ }
                        is Resource.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
                editingMessage?.let { message ->
                    EditMessage(
                        editingMessage = message,
                        attachments = messageAttachments,
                        onCancelEdit = {
                            editingMessage = null
                            messageRequest = messageRequest?.copy(message = null)
                        }
                    )
                }
                replyToMessage?.let { message ->
                    userState.data?.find { it.userId == message.senderId }?.let { userData ->
                        ReplyToMessage(
                            replyMessage = message,
                            userData = userData,
                            attachments = messageAttachments,
                            onCancelReply = {
                                replyToMessage = null
                                messageRequest = messageRequest?.copy(replyTo = null)
                            }
                        )
                    }
                }
                MessageInput(
                    messageText = editingMessage?.message ?: messageRequest?.message ?: "",
                    editingMessage = editingMessage,
                    messageRequest = messageRequest,
                    onMessageChange = { newMessage ->
                        if (editingMessage != null) {
                            editingMessage = editingMessage?.copy(message = newMessage)
                        } else {
                            messageRequest = messageRequest?.copy(message = newMessage)
                        }
                    },
                    onSendMessage = { request ->
                        coroutineScope.launch {
                            messageViewModel.sendMessage(request)
                            clearInputState()
                        }
                    },
                    onUpdateMessage = { messageId, updateRequest ->
                        coroutineScope.launch {
                            messageViewModel.updateMessage(messageId, updateRequest)
                            clearInputState()
                        }
                    }
                )
            }

            if (showBottomSheet.value) {
                Log.d("ChatScreen", "Metadata: ${metadataState.data}, Users: ${userState.data}")
                metadataState.data?.let { metadata ->
                    sortedUsers?.let { usersList ->
                        chatState.data?.let { chatData ->
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
                                currentUserId = currentUser?.userId ?: "meh",
                                userData = userData,
                                userStatus = userStatus,
                                onDismiss = { showPersonalBottomSheet.value = false },
                                showBottomSheet = showPersonalBottomSheet.value,
                                context = context
                            )
                        }
                    }
                }
            }
        }
    }
}