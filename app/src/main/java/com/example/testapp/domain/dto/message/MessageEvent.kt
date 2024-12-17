package com.example.testapp.domain.dto.message

import com.example.testapp.domain.models.message.Message
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
sealed class MessageEvent {
    abstract val type: String

    @JsonClass(generateAdapter = true)
    data class MessageCreated(val message: Message) : MessageEvent() {
        override val type: String = "MessageCreated"
    }

    @JsonClass(generateAdapter = true)
    data class MessageUpdated(val message: Message) : MessageEvent() {
        override val type: String = "MessageUpdated"
    }

    @JsonClass(generateAdapter = true)
    data class MessageDeleted(val message: Message) : MessageEvent() {
        override val type: String = "MessageDeleted"
    }
}