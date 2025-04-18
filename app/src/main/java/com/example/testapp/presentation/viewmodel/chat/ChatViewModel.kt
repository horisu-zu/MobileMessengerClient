package com.example.testapp.presentation.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chat.service.course.dto.ChatDBResponse
import com.example.testapp.domain.dto.chat.ChatJoinRequest
import chat.service.course.dto.GroupChatRequest
import chat.service.course.dto.PersonalChatRequest
import com.example.testapp.domain.models.chat.Chat
import com.example.testapp.domain.models.chat.ChatMetadata
import com.example.testapp.di.api.ChatApiService
import com.example.testapp.di.websocket.MetadataWebSocketClient
import com.example.testapp.domain.models.chat.ChatParticipant
import com.example.testapp.domain.models.chat.ChatRestriction
import com.example.testapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatApiService,
    private val metadataWebSocketClient: MetadataWebSocketClient
) : ViewModel() {
    private val _chatState = MutableStateFlow<Resource<Chat>>(Resource.Idle())
    val chatState: StateFlow<Resource<Chat>> = _chatState

    private val _chatMetadataState = MutableStateFlow<Resource<ChatMetadata>>(Resource.Idle())
    val chatMetadataState: StateFlow<Resource<ChatMetadata>> = _chatMetadataState

    private val _chatParticipantsState =
        MutableStateFlow<Resource<List<ChatParticipant>>>(Resource.Idle())
    val chatParticipantsState: StateFlow<Resource<List<ChatParticipant>>> = _chatParticipantsState

    private val _chatUserRestrictionsState = MutableStateFlow<Resource<ChatRestriction>>(Resource.Idle())
    val chatUserRestrictionsState = _chatUserRestrictionsState.asStateFlow()

    private val _groupSearchState =
        MutableStateFlow<Resource<Map<ChatMetadata, Int>>>(Resource.Idle())
    val groupSearchState: StateFlow<Resource<Map<ChatMetadata, Int>>> = _groupSearchState

    private var searchJob: Job? = null
    private var currentChatId: String? = null

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

    suspend fun getChatById(chatId: String) {
        if(currentChatId == chatId) return
        _chatState.value = Resource.Loading()
        try {
            val response = chatRepository.getChatById(chatId)
            _chatState.value = Resource.Success(response)
            currentChatId = chatId
        } catch (e: Exception) {
            _chatState.value = Resource.Error(e.message ?: "Couldn't load chat data")
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

    fun createGroupChat(groupChatRequest: GroupChatRequest): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            chatRepository.createGroupChat(groupChatRequest)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error creating group chat"))
        }
    }

    fun createPersonalChat(personalChatRequest: PersonalChatRequest): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val response = chatRepository.createPersonalChat(personalChatRequest)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error creating personal chat"))
        }
    }

    fun joinChat(chatId: String, request: ChatJoinRequest): Flow<Resource<ChatDBResponse>> =
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

    fun getUserConversations(userId: String): Flow<Resource<List<String>>> = flow {
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

    fun checkIfPrivateChatExists(
        firstUserId: String,
        secondUserId: String
    ): Flow<Resource<String?>> = flow {
        emit(Resource.Loading())
        try {
            val conversationPartners = chatRepository.getUserConversations(firstUserId)
            val matchingPartner = conversationPartners.find { it.userId == secondUserId }

            if (matchingPartner != null) {
                emit(Resource.Success(matchingPartner.chatId))
            } else {
                emit(Resource.Success(null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error checking private chat existence"))
        }
    }

    fun getUserRestrictionsInChat(chatId: String, userId: String) {
        viewModelScope.launch {
            if(currentChatId == chatId) return@launch
            _chatUserRestrictionsState.value = Resource.Loading()

            try {
                val userRestrictions = chatRepository.getUserRestrictionInChat(chatId, userId)
                _chatUserRestrictionsState.value = Resource.Success(userRestrictions)
            } catch (e: Exception) {
                _chatUserRestrictionsState.value = Resource.Error(e.message ?: "Error getting user restrictions")
            }
        }
    }

    fun leaveGroupChat(chatId: String, userId: String) {
        viewModelScope.launch {
            chatRepository.leaveGroupChat(chatId, userId)
        }
    }

    private fun connectWebSocket(chatIds: List<String>) {
        metadataWebSocketClient.connect(chatIds)
    }

    public override fun onCleared() {
        super.onCleared()
        metadataWebSocketClient.disconnect()
    }
}