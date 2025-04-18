package com.example.testapp.di.api

import chat.service.course.dto.ChatDBResponse
import com.example.testapp.domain.dto.chat.ChatJoinRequest
import chat.service.course.dto.GroupChatRequest
import chat.service.course.dto.PersonalChatRequest
import com.example.testapp.domain.models.chat.Chat
import com.example.testapp.domain.models.chat.ChatMetadata
import com.example.testapp.domain.dto.chat.ChatRestrictionRequest
import com.example.testapp.domain.dto.chat.RestrictionExpireType
import com.example.testapp.domain.dto.chat.ChatRestrictionUpdateRequest
import com.example.testapp.domain.dto.chat.ConversationPartner
import com.example.testapp.domain.models.chat.ChatParticipant
import com.example.testapp.domain.models.chat.ChatRestriction
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApiService {
    @POST("api/chats/personal")
    suspend fun createPersonalChat(@Body request: PersonalChatRequest): String

    @POST("api/chats/group")
    suspend fun createGroupChat(@Body request: GroupChatRequest): Unit

    @GET("api/chats/{userId}/conversations")
    suspend fun getUserConversations(@Path("userId") userId: String): List<ConversationPartner>

    @DELETE("api/chats/{chatId}/{userId}")
    suspend fun deleteChat(
        @Path("chatId") chatId: String,
        @Path("userId") userId: String
    ): ChatDBResponse

    @POST("api/chats/join/{chatId}")
    suspend fun joinChat(
        @Path("chatId") chatId: String,
        @Body request: ChatJoinRequest
    ): ChatDBResponse

    @GET("api/chats/{chatId}/participants")
    suspend fun getChatParticipants(@Path("chatId") chatId: String): List<ChatParticipant>

    @GET("api/chats/participants/{userId}")
    suspend fun getUserChats(@Path("userId") userId: String): List<Chat>

    @GET("api/chats/{chatId}/metadata")
    suspend fun getChatMetadata(@Path("chatId") chatId: String): ChatMetadata

    @DELETE("api/chats/{chatId}/participants/{userId}")
    suspend fun leaveGroupChat(
        @Path("chatId") chatId: String,
        @Path("userId") userId: String
    )

    @GET("api/chats/search")
    suspend fun searchChats(@Query("name") name: String? = null): List<ChatMetadata>

    @GET("api/chats/{chatId}")
    suspend fun getChatById(@Path("chatId") chatId: String): Chat

    @PUT("api/chats/{chatId}")
    suspend fun updateMetadata(
        @Path("chatId") chatId: String,
        @Body newMetadata: ChatMetadata
    ): ChatMetadata

    @GET("api/chats/{chatId}/participants-count")
    suspend fun getParticipantsCount(@Path("chatId") chatId: String): Int

    @GET("api/chats/{chatId}/restrictions")
    suspend fun getChatRestrictions(
        @Path("chatId") chatId: String,
        @Query("expireType") expireType: RestrictionExpireType,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<ChatRestriction>

    @POST("api/chats/{chatId}/restrictions")
    suspend fun createRestriction(
        @Path("chatId") chatId: String,
        @Body request: ChatRestrictionRequest
    ): ChatRestriction

    @PATCH("api/chats/restrictions/{restrictionId}")
    suspend fun updateRestriction(
        @Path("restrictionId") restrictionId: String,
        @Body request: ChatRestrictionUpdateRequest
    ): ChatRestriction

    @GET("api/chats/{chatId}/restriction/{userId}")
    suspend fun getUserRestrictionInChat(
        @Path("chatId") chatId: String,
        @Path("userId") userId: String
    ): ChatRestriction
}