package com.example.testapp.presentation.viewmodel.user

import android.util.Base64
import android.util.Log
import com.example.testapp.di.api.UserApiService
import com.example.testapp.domain.dto.user.RefreshTokenRequest
import com.example.testapp.utils.DataStoreUtil
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject
import retrofit2.HttpException
import java.nio.charset.Charset
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val dataStoreUtil: DataStoreUtil,
    private val apiService: UserApiService
) {
    suspend fun getValidAccessToken(): String? {
        val accessToken = dataStoreUtil.getAccessToken().firstOrNull()
        if (accessToken == null) {
            Log.d("TokenManager", "Access token is null.")
        } else if (isTokenExpired(accessToken)) {
            Log.d("TokenManager", "Access token is expired.")
            return tryRefreshingTokens()
        } else if (shouldRefreshToken(accessToken)) {
            Log.d("TokenManager", "Access token is nearing expiration.")
            return tryRefreshingTokens()
        } else {
            Log.d("TokenManager", "Access token is still valid.")
        }

        return accessToken
    }

    suspend fun tryRefreshingTokens(): String? {
        val refreshToken = dataStoreUtil.getRefreshToken().firstOrNull()
        if (refreshToken != null) {
            try {
                Log.d("TokenManager", "Attempting to refresh tokens.")
                val tokenResponse = apiService.refreshTokens(RefreshTokenRequest(refreshToken))
                Log.d("TokenManager", "Tokens refreshed successfully: ${tokenResponse.accessToken}")
                dataStoreUtil.saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
                return tokenResponse.accessToken
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> {
                        Log.e("TokenManager", "HTTP Error: ${e.code()}")
                        Log.e("TokenManager", "Error Body: ${e.response()?.errorBody()?.string()}")
                    }
                    else -> Log.e("TokenManager", "Error: ${e.message}")
                }
                return null
            }
        } else {
            Log.d("TokenManager", "Refresh token is null, cannot refresh access token.")
            return null
        }
    }

    private fun isTokenExpired(token: String): Boolean {
        val tokenParts = token.split(".")
        if (tokenParts.size != 3) return true

        val payloadJson = String(Base64.decode(tokenParts[1], Base64.DEFAULT), Charset.defaultCharset())
        val payload = JSONObject(payloadJson)
        val expiration = payload.optLong("exp", 0L) * 1000

        val now = Date().time
        return now > expiration
    }

    fun shouldRefreshToken(token: String, threshold: Double = 0.5): Boolean {
        val tokenParts = token.split(".")
        if (tokenParts.size != 3) return false

        val payloadJson = String(Base64.decode(tokenParts[1], Base64.DEFAULT), Charset.defaultCharset())
        val payload = JSONObject(payloadJson)
        val expiration = payload.optLong("exp", 0L) * 1000
        val issuedAt = payload.optLong("iat", 0L) * 1000

        val now = Date().time
        val tokenLifetime = expiration - issuedAt
        val remainingLifetime = expiration - now

        val remainingPercentage = remainingLifetime.toDouble() / tokenLifetime
        return remainingPercentage <= threshold
    }
}
