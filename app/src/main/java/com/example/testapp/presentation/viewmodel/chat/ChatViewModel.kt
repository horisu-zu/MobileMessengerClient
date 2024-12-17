package com.example.testapp.presentation.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chat.service.course.dto.ChatCreateResponse
import chat.service.course.dto.ChatDBResponse
import chat.service.course.dto.ChatJoinRequest
import chat.service.course.dto.GroupChatRequest
import chat.service.course.dto.PersonalChatRequest
import com.example.testapp.domain.models.chat.Chat
import com.example.testapp.domain.models.chat.ChatMetadata
import com.example.testapp.domain.models.chat.ChatParticipant
import com.example.testapp.di.api.ChatApiService
import com.example.testapp.di.websocket.MetadataWebSocketClient
import com.example.testapp.domain.dto.chat.ChatDisplayData
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatApiService,
    private val dataStoreUtil: DataStoreUtil,
    private val userViewModel: UserViewModel,
    private val metadataWebSocketClient: MetadataWebSocketClient
) : ViewModel() {
    private val _chatState = MutableStateFlow<Resource<Chat>>(Resource.Loading())
    val chatState: StateFlow<Resource<Chat>> = _chatState

    private val _chatMetadataState = MutableStateFlow<Resource<ChatMetadata>>(Resource.Loading())
    val chatMetadataState: StateFlow<Resource<ChatMetadata>> = _chatMetadataState

    private val _chatParticipantsState =
        MutableStateFlow<Resource<List<ChatParticipant>>>(Resource.Loading())
    val chatParticipantsState: StateFlow<Resource<List<ChatParticipant>>> = _chatParticipantsState

    private val _groupSearchState =
        MutableStateFlow<Resource<Map<ChatMetadata, Int>>>(Resource.Loading())
    val groupSearchState: StateFlow<Resource<Map<ChatMetadata, Int>>> = _groupSearchState

    private val _userChatsState = MutableStateFlow<Resource<List<Chat>>>(Resource.Loading())
    val userChatsState: StateFlow<Resource<List<Chat>>> = _userChatsState

    private val _userChatsMetadataState =
        MutableStateFlow<Resource<List<ChatMetadata>>>(Resource.Loading())
    val userChatsMetadataState: StateFlow<Resource<List<ChatMetadata>>> = _userChatsMetadataState

    private val _chatDisplayDataState =
        MutableStateFlow<Map<String, Resource<ChatDisplayData>>>(emptyMap())
    val chatDisplayDataState: StateFlow<Map<String, Resource<ChatDisplayData>>> =
        _chatDisplayDataState

    //private val currentUserIdFlow = dataStoreUtil.getUserId()
    private var searchJob: Job? = null

    init {
        Log.d("ChatViewModel", "Initializing WebSocket subscription")
        viewModelScope.launch {
            try {
                metadataWebSocketClient.getMetadataUpdates()
                    .catch { e ->
                        Log.e("ChatViewModel", "Error in metadata updates flow", e)
                    }
                    .collect { metadataUpdates ->
                        Log.d("ChatViewModel", "Processing metadata update batch: $metadataUpdates")
                        updateMetadataStates(metadataUpdates)
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Uncaught error in metadata subscription", e)
            }
        }
    }

    private fun updateMetadataStates(metadataUpdates: Map<String, ChatMetadata>) {
        viewModelScope.launch {
            _chatMetadataState.update { currentState ->
                when (currentState) {
                    is Resource.Success -> {
                        metadataUpdates[currentState.data?.chatId]?.let {
                            Resource.Success(it)
                        } ?: currentState
                    }

                    else -> currentState
                }
            }

            _userChatsMetadataState.update { currentState ->
                when (currentState) {
                    is Resource.Success -> {
                        val updatedList = currentState.data?.map { existing ->
                            metadataUpdates[existing.chatId] ?: existing
                        }
                        Resource.Success(updatedList)
                    }

                    else -> currentState
                }
            }

            _groupSearchState.update { currentState ->
                when (currentState) {
                    is Resource.Success -> {
                        val currentData = currentState.data ?: emptyMap()
                        val updatedMap = currentData.mapKeys { (metadata, count) ->
                            metadataUpdates[metadata.chatId]?.let { updated ->
                                Log.d(
                                    "MetadataUpdate",
                                    "Updating metadata for chat: ${metadata.chatId}"
                                )
                                updated
                            } ?: metadata
                        }
                        Log.d(
                            "MetadataUpdate",
                            "Updated search state with ${updatedMap.size} items"
                        )
                        Resource.Success(updatedMap)
                    }

                    else -> {
                        Log.d("MetadataUpdate", "State not ready for update: $currentState")
                        currentState
                    }
                }
            }
        }
    }

    fun loadChatDisplayData(chat: Chat, currentUserId: String) {
        val chatId = chat.chatId ?: return

        if (_chatDisplayDataState.value[chatId] is Resource.Success) {
            return
        }

        viewModelScope.launch {
            if (chatId !in _chatDisplayDataState.value) {
                _chatDisplayDataState.value += (chatId to Resource.Loading())
            }

            try {
                val displayData = when (chat.chatTypeId) {
                    1 -> {
                        val otherUserId = chatRepository.getChatParticipants(chatId)
                            .filter { it.userId != currentUserId }

                        val otherUser = userViewModel.getUserById(otherUserId.first().userId)
                        ChatDisplayData(
                            chatId = chatId,
                            name = otherUser.nickname ?: "Unknown User",
                            avatarUrl = otherUser.avatarUrl,
                            isLoading = false
                        )
                    }

                    2 -> {
                        val metadata = chatRepository.getChatMetadata(chatId)
                        ChatDisplayData(
                            chatId = chatId,
                            name = metadata.name ?: "Unknown Group",
                            avatarUrl = metadata.avatar,
                            isLoading = false
                        )
                    }

                    else -> throw IllegalStateException("Unknown chat type")
                }

                _chatDisplayDataState.value += (chatId to Resource.Success(displayData))
            } catch (e: Exception) {
                _chatDisplayDataState.value += (chatId to Resource.Error(
                    e.message ?: "Error loading chat data"
                ))
            }
        }
    }

    suspend fun getChatById(chatId: String) {
        _chatState.value = Resource.Loading()
        try {
            val response = chatRepository.getChatById(chatId)
            _chatState.value = Resource.Success(response)
        } catch (e: Exception) {
            _chatState.value = Resource.Error(e.message ?: "Couldn't load chat data")
        }
    }

    fun startObservingChats(userId: String) {
        viewModelScope.launch {
            try {
                val chats = chatRepository.getUserChats(userId)
                _userChatsState.value = Resource.Success(chats)

                /*val groupChatsMetadata = chats
                    .filter { it.chatTypeId == 2 } // GroupTypeId = 2
                    .mapNotNull { chat ->
                        chat.chatId?.let { id ->
                            async { chatRepository.getChatMetadata(id) }
                        }
                    }
                    .awaitAll()
                _userChatsMetadataState.value = Resource.Success(groupChatsMetadata)*/
            } catch (e: Exception) {
                _userChatsState.value = Resource.Error(e.message ?: "Error fetching chats")
                _userChatsMetadataState.value =
                    Resource.Error(e.message ?: "Error fetching chat metadata")
            }
        }
    }

    suspend fun getChatMetadata(chatId: String) {
        _chatMetadataState.value = Resource.Loading()
        try {
            val response = chatRepository.getChatMetadata(chatId)
            _chatMetadataState.value = Resource.Success(response)
            metadataWebSocketClient.connect(listOf(chatId))
        } catch (e: Exception) {
            _chatMetadataState.value = Resource.Error(e.message ?: "Error loading chat metadata")
        }
    }

    suspend fun getChatParticipants(chatId: String) {
        _chatParticipantsState.value = Resource.Loading()
        try {
            val response = chatRepository.getChatParticipants(chatId)
            _chatParticipantsState.value = Resource.Success(response)
        } catch (e: Exception) {
            _chatParticipantsState.value =
                Resource.Error(e.message ?: "Error loading chat participants")
        }
    }

    suspend fun createGroupChat(groupChatRequest: GroupChatRequest): Flow<Resource<ChatCreateResponse>> =
        flow {
            emit(Resource.Loading())
            try {
                val response = chatRepository.createGroupChat(groupChatRequest)
                emit(Resource.Success(response))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Error creating group chat"))
            }
        }

    suspend fun createPersonalChat(personalChatRequest: PersonalChatRequest) {
        chatRepository.createPersonalChat(personalChatRequest)
    }

    suspend fun joinChat(chatId: String, request: ChatJoinRequest): Flow<Resource<ChatDBResponse>> =
        flow {
            emit(Resource.Loading())
            try {
                val response = chatRepository.joinChat(chatId, request)
                emit(Resource.Success(response))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Error joining group chat"))
            }
        }

    fun searchChats(name: String? = null) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _groupSearchState.value = Resource.Loading()

            try {
                val chats = chatRepository.searchChats(name)
                val chatsWithParticipantsCount = chats.map { chat ->
                    async {
                        val participantCount = chatRepository.getParticipantsCount(chat.chatId)
                        chat to participantCount
                    }
                }

                val result = chatsWithParticipantsCount.awaitAll()

                val finalResult = result.associate { it.first to it.second }

                _groupSearchState.value = Resource.Success(finalResult)
                connectWebSocket(chats.map { it.chatId })
            } catch (e: Exception) {
                _groupSearchState.value = Resource.Error("Failed to search users: ${e.message}")
            }
        }
    }

    suspend fun getUserConversations(userId: String): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("GroupAddNavigator", "Fetching conversations for user: $userId")
            val response = chatRepository.getUserConversations(userId)

            Log.d("GroupAddNavigator", "Received response: $response")
            val userIdsList = response.map { it.userId }

            Log.d("GroupAddNavigator", "Extracted userIds: $userIdsList")

            emit(Resource.Success(userIdsList))
        } catch (e: Exception) {
            Log.e("GroupAddNavigator", "Error fetching conversations: ${e.message}")
            emit(Resource.Error(e.message ?: "Error finding selected user conversations"))
        }
    }

    suspend fun getParticipantsCount(chatId: String): Int {
        return chatRepository.getParticipantsCount(chatId)
    }

    suspend fun leaveGroupChat(chatId: String, userId: String) {
        return chatRepository.leaveGroupChat(chatId, userId)
    }

    private fun connectWebSocket(chatIds: List<String>) {
        metadataWebSocketClient.connect(chatIds)
    }

    public override fun onCleared() {
        super.onCleared()
        metadataWebSocketClient.disconnect()
    }
}