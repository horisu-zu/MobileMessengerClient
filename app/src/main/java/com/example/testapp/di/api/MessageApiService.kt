package com.example.testapp.di.api

import com.example.testapp.domain.dto.message.AttachmentRequest
import com.example.testapp.domain.dto.message.MessageRequest
import com.example.testapp.domain.dto.message.MessageUpdateRequest
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.domain.models.message.Message
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageApiService {
    @GET("api/messages/{chatId}")
    suspend fun getMessagesForChat(
        @Path("chatId") chatId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("direction") direction: String = "DESC"
    ): List<Message>

    @GET("api/messages/{chatId}/attachments")
    suspend fun getAttachmentsForChat(
        @Path("chatId") chatId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): List<Attachment>

    @GET("api/messages")
    suspend fun getMessagesByIds(
        @Query("messageIds") messageIds: List<String>
    ): List<Message>

    @GET("api/messages/lastMessages")
    suspend fun getLastMessages(@Query("chatIds") chatIds: List<String>): List<Message>

    @POST("api/messages")
    suspend fun createMessage(
        @Body request: MessageRequest,
        @Query("attachmentUrls") attachmentUrls: List<String> = emptyList()
    ): Message

    @PUT("api/messages/{messageId}")
    suspend fun updateMessage(
        @Path("messageId") messageId: String,
        @Body request: MessageUpdateRequest
    ): Message

    /*@PUT("api/messages/{messageId}/status")
    suspend fun updateMessageStatus(
        @Path("messageId") messageId: String,
        @Body request: MessageStatusUpdateRequest
    ): Message*/

    @DELETE("api/messages/{messageId}")
    suspend fun deleteMessage(@Path("messageId") messageId: String)

    @GET("api/messages/{messageId}/attachments")
    suspend fun getAttachmentsForMessage(@Path("messageId") messageId: String): List<Attachment>

    @POST("api/messages/{messageId}/attachments")
    suspend fun createAttachment(
        @Path("messageId") messageId: String,
        @Body attachmentRequest: AttachmentRequest
    )

    @POST("api/messages/{chatId}/{messageId}/mark-read")
    fun markMessagesAsRead(
        @Path("chatId") chatId: String,
        @Path("messageId") messageId: String,
        @Body userId: String
    ): Int

    @GET("api/messages/{chatId}/{userId}/count")
    suspend fun getUnreadMessagesCount(
        @Path("chatId") chatId: String,
        @Path("userId") userId: String
    ): Int

    @GET("api/messages/{chatId}/{userId}/messages")
    suspend fun getUnreadMessages(
        @Path("chatId") chatId: String,
        @Path("userId") userId: String,
    ): List<Message>

    @GET("api/messages/{chatId}/{userId}")
    suspend fun getUserMessagesInChat(
        @Path("chatId") chatId: String,
        @Path("userId") userId: String
    ): List<Message>
}