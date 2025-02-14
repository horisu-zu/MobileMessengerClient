package com.example.testapp.di.websocket

import android.util.Log
import com.example.testapp.di.InstantAdapter
import com.example.testapp.domain.models.user.UserStatus
import com.example.testapp.presentation.viewmodel.user.AuthInterceptor
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Defaults
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class UserStatusWebSocketClient @Inject constructor(
    private val dataStoreUtil: DataStoreUtil
) {
    private var webSocket: WebSocket? = null
    private val statusUpdatesChannel = Channel<Map<String, UserStatus>>(Channel.CONFLATED)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(InstantAdapter())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(dataStoreUtil))
        .build()

    fun connect(userIds: List<String>) {
        val request = Request.Builder()
            .url("${Defaults.baseUrl.trimEnd('/')}:${Defaults.USER_SERVICE_PORT}/ws/status?userIds=${userIds.joinToString(",")}")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connection opened with protocol: ${response.protocol}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received message: $text")
                try {
                    val statusMap = parseStatusUpdate(text)
                    Log.d("WebSocket", "Parsed status map: $statusMap")
                    statusUpdatesChannel.trySend(statusMap)
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parsing message", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "WebSocket failed", t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "WebSocket closed: $reason")
            }
        })
    }

    fun disconnect() {
        Log.d("UserWebSocket", "Disconnected")
        webSocket?.close(1000, "Closing connection")
        webSocket = null
    }

    fun getStatusUpdates(): Flow<Map<String, UserStatus>> = statusUpdatesChannel.receiveAsFlow()

    /*fun updateUserStatus(userId: String, isOnline: Boolean) {
        val statusUpdate = JSONObject().apply {
            put("userId", userId)
            put("onlineStatus", isOnline)
        }
        webSocket?.send(statusUpdate.toString())
    }*/

    private fun parseStatusUpdate(json: String): Map<String, UserStatus> {
        val type = moshi.adapter<Map<String, UserStatus>>(
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                UserStatus::class.java
            )
        )
        return type.fromJson(json) ?: emptyMap()
    }
}