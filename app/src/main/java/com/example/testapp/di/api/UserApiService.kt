package com.example.testapp.di.api

import com.example.testapp.domain.dto.user.RefreshTokenRequest
import com.example.testapp.domain.dto.user.AuthResponse
import com.example.testapp.domain.dto.user.UserLogin
import com.example.testapp.domain.dto.user.UserRequest
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.dto.user.UserStatusRequest
import com.example.testapp.domain.models.user.UserStatus
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {
    @POST("api/users/auth/login")
    suspend fun login(@Body userLogin: UserLogin): AuthResponse

    @POST("api/users/auth/register")
    suspend fun register(@Body user: UserRequest): AuthResponse

    @POST("api/users/auth/refresh")
    suspend fun refreshTokens(@Body request: RefreshTokenRequest): AuthResponse

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: String): UserResponse

    @GET("api/users/search")
    suspend fun getUsersByNickname(@Query("nickname") nickname: String? = null): List<UserResponse>

    @GET("api/users/chat/{chatId}")
    suspend fun getUsersByChatId(@Path("chatId") chatId: String): List<UserResponse>

    @PATCH("api/users/{id}/status")
    suspend fun updateUserStatus(@Path("id") id: String, @Body userStatusRequest: UserStatusRequest): UserStatus

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body userResponse: UserResponse): UserResponse

    @PUT("api/users/{id}/nickname")
    suspend fun updateNickname(@Path("id") id: String, @Body userResponse: UserResponse): UserResponse

    @GET("api/users/ids")
    suspend fun getByIds(@Query("userIds") userIds: List<String> = emptyList()): List<UserResponse>

    //Was using this temporary before implementing WebSocket
    /*@GET("api/users/{id}/status")
    suspend fun getUserStatus(@Path("id") id: String): UserStatus*/
}