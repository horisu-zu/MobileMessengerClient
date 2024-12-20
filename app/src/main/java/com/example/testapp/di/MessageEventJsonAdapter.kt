package com.example.testapp.di

import com.example.testapp.domain.dto.message.MessageEvent
import com.example.testapp.domain.dto.message.MessageEventWrapper
import com.squareup.moshi.*

class MessageEventJsonAdapter {
    @FromJson
    fun fromJson(wrapper: MessageEventWrapper): MessageEvent {
        return when (wrapper.type) {
            "MessageCreated" -> MessageEvent.MessageCreated(wrapper.message)
            "MessageUpdated" -> MessageEvent.MessageUpdated(wrapper.message)
            "MessageDeleted" -> MessageEvent.MessageDeleted(wrapper.message)
            else -> throw JsonDataException("Unknown message event type: ${wrapper.type}")
        }
    }

    @ToJson
    fun toJson(messageEvent: MessageEvent): MessageEventWrapper {
        return MessageEventWrapper(
            type = messageEvent.type,
            message = when (messageEvent) {
                is MessageEvent.MessageCreated -> messageEvent.message
                is MessageEvent.MessageUpdated -> messageEvent.message
                is MessageEvent.MessageDeleted -> messageEvent.message
            }
        )
    }
}