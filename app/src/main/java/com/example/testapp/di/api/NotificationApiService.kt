package com.example.testapp.di.api

import com.example.testapp.domain.dto.notification.NotificationRequest
import com.example.testapp.domain.models.user.UserToken
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApiService {
    @POST("api/notification")
    suspend fun sendNotification(@Body notificationRequest: NotificationRequest): String

    @POST("api/notification/multicast")
    suspend fun sendMulticastNotification(@Body notificationRequest: NotificationRequest): List<String>

    @GET("api/notification/tokens/{chatId}")
    suspend fun getTokensByChatId(@Path("chatId") chatId: String): List<UserToken>

    @POST("api/notification/tokens/{userId}")
    suspend fun saveToken(@Path("userId") userId: String, @Body token: UserToken): UserToken

    @DELETE("api/notification/tokens/{userId}/{token}")
    suspend fun deleteToken(@Path("userId") userId: String, @Path("token") token: String): Void
}