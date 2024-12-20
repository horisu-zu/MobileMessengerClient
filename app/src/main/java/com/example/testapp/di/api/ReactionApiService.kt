package com.example.testapp.di.api

import com.example.testapp.domain.dto.reaction.ReactionRequest
import com.example.testapp.domain.dto.reaction.ReactionResponse
import com.example.testapp.domain.models.reaction.Reaction
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReactionApiService {
    @POST("api/reactions/toggle")
    suspend fun toggleReaction(@Body reactionRequest: ReactionRequest): ReactionResponse

    @GET("api/reactions/{messageId}")
    suspend fun getReactionsForMessage(@Path("messageId") messageId: String): List<Reaction>
}