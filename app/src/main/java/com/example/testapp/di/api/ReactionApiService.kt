package com.example.testapp.di.api

import com.example.testapp.domain.dto.reaction.ReactionRequest
import com.example.testapp.domain.dto.reaction.ReactionResponse
import com.example.testapp.domain.models.reaction.Reaction
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReactionApiService {
    @POST("api/reactions/toggle")
    suspend fun toggleReaction(@Body reactionRequest: ReactionRequest): ReactionResponse

    @GET("api/reactions/chat/{chatId}")
    suspend fun getReactionsForChat(
        @Path("chatId") chatId: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<Reaction>

    @GET("api/reactions/{messageId}")
    suspend fun getReactionsForMessage(@Path("messageId") messageId: String): List<Reaction>
}