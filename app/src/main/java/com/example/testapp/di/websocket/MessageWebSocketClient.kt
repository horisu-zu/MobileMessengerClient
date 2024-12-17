package com.example.testapp.di.websocket

import android.util.Log
import com.example.testapp.di.InstantAdapter
import com.example.testapp.domain.dto.message.MessageEvent
import com.example.testapp.domain.dto.message.MessageStreamMode
import com.example.testapp.presentation.viewmodel.user.AuthInterceptor
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Defaults
import com.example.testapp.utils.Defaults.baseUrl
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MessageWebSocketClient(
    private val dataStoreUtil: DataStoreUtil
) {
    private var webSocket: WebSocket? = null
    private val messageUpdatesChannel = Channel<Map<String, MessageEvent>>(Channel.CONFLATED)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(InstantAdapter())
        .add(
            PolymorphicJsonAdapterFactory.of(MessageEvent::class.java, "type")
                .withSubtype(MessageEvent.MessageCreated::class.java, "MessageCreated")
                .withSubtype(MessageEvent.MessageUpdated::class.java, "MessageUpdated")
                .withSubtype(MessageEvent.MessageDeleted::class.java, "MessageDeleted")
        )
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(dataStoreUtil))
        .build()

    fun connect(chatIds: List<String>, mode: MessageStreamMode) {
        val request = Request.Builder()
            .url(
                "${baseUrl.trimEnd('/')}:${Defaults.MESSAGE_SERVICE_PORT}/ws/messages?chatIds=${
                    chatIds.joinToString(
                        ","
                    )
                }&mode=${mode.name}"
            )
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("MessageWebSocket", "Connection opened with protocol: ${response.protocol}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("MessageWebSocket", "Received message: $text")
                try {
                    val messageMap = parseMessageUpdate(text)
                    Log.d("MessageWebSocket", "Parsed message map: $messageMap")
                    messageUpdatesChannel.trySend(messageMap)
                } catch (e: Exception) {
                    Log.e("MessageWebSocket", "Error parsing message", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("MessageWebSocket", "WebSocket failed", t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("MessageWebSocket", "WebSocket closed: $reason")
            }
        })
    }

    fun disconnect() {
        Log.d("MessageWebSocket", "Disconnected")
        webSocket?.close(1000, "Closing connection")
        webSocket = null
    }

    fun getMessageUpdates(): Flow<Map<String, MessageEvent>> = messageUpdatesChannel.receiveAsFlow()

    private fun parseMessageUpdate(json: String): Map<String, MessageEvent> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, MessageEvent::class.java)
        val adapter = moshi.adapter<Map<String, MessageEvent>>(type)
        return adapter.fromJson(json) ?: emptyMap()
    }

    /*private fun parseMessageEvent(json: String): MessageEvent {
        val jsonAdapter = moshi.adapter(MessageEvent::class.java)
        return jsonAdapter.fromJson(json) ?: throw IllegalArgumentException("Invalid message event")
    }*/
}