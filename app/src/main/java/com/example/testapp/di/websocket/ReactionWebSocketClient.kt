package com.example.testapp.di.websocket

import android.util.Log
import com.example.testapp.di.InstantAdapter
import com.example.testapp.di.ReactionEventJsonAdapter
import com.example.testapp.domain.dto.reaction.ReactionEvent
import com.example.testapp.domain.dto.reaction.ReactionEventWrapper
import com.example.testapp.presentation.viewmodel.user.AuthInterceptor
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Defaults
import com.example.testapp.utils.Defaults.baseUrl
import com.squareup.moshi.JsonDataException
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

class ReactionWebSocketClient(
    private val dataStoreUtil: DataStoreUtil
) {
    private var webSocket: WebSocket? = null
    private val reactionUpdatesChannel = Channel<Map<String, ReactionEvent>>(Channel.CONFLATED)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(InstantAdapter())
        //.add(ReactionEventJsonAdapter())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(dataStoreUtil))
        .build()

    fun connect(messageIds: List<String>) {
        val request = Request.Builder()
            .url(
                "${baseUrl.trimEnd('/')}:${Defaults.REACTION_SERVICE_PORT}" +
                        "/ws/reactions?messageIds=${messageIds.joinToString(",")}"
            )
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ReactionWebSocket", "Connection opened with protocol: ${response.protocol}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("ReactionWebSocket", "Received message: $text")
                try {
                    val reactionMap = parseReactionUpdate(text)
                    Log.d("ReactionWebSocket", "Parsed message map: $reactionMap")
                    reactionUpdatesChannel.trySend(reactionMap)
                } catch (e: Exception) {
                    Log.e("ReactionWebSocket", "Error parsing message", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ReactionWebSocket", "WebSocket failed", t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("ReactionWebSocket", "WebSocket closed: $reason")
            }
        })
    }

    fun disconnect() {
        Log.d("ReactionWebSocket", "Disconnected")
        webSocket?.close(1000, "Closing connection")
        webSocket = null
    }

    fun getReactionUpdates(): Flow<Map<String, ReactionEvent>> = reactionUpdatesChannel.receiveAsFlow()

    private fun parseReactionUpdate(json: String): Map<String, ReactionEvent> {
        val mapType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            ReactionEventWrapper::class.java
        )
        val mapAdapter = moshi.adapter<Map<String, ReactionEventWrapper>>(mapType)

        return try {
            val wrappedMap = mapAdapter.fromJson(json) ?: return emptyMap()
            wrappedMap.mapValues { (_, wrapper) ->
                when (wrapper.type) {
                    "ReactionAdded" -> { ReactionEvent.ReactionAdded(wrapper.reaction) }
                    "ReactionRemoved" -> { ReactionEvent.ReactionRemoved(wrapper.reaction) }
                    else -> throw JsonDataException("Unknown message event type: ${wrapper.type}")
                }
            }
        } catch (e: Exception) {
            Log.e("ReactionWebSocket", "Error parsing JSON: $json", e)
            throw e
        }
    }
}