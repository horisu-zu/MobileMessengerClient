package com.example.testapp.di

import com.example.testapp.domain.dto.reaction.ReactionEvent
import com.example.testapp.domain.dto.reaction.ReactionEventWrapper
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson

class ReactionEventJsonAdapter {
    @FromJson
    fun fromJson(wrapper: ReactionEventWrapper): ReactionEvent {
        return when (wrapper.type) {
            "ReactionAdded" -> ReactionEvent.ReactionAdded(wrapper.reaction)
            "ReactionRemoved" -> ReactionEvent.ReactionRemoved(wrapper.reaction)
            else -> throw JsonDataException("Unknown message event type: ${wrapper.type}")
        }
    }

    @ToJson
    fun toJson(reactionEvent: ReactionEvent): ReactionEventWrapper {
        return ReactionEventWrapper(
            type = reactionEvent.type,
            reaction = when (reactionEvent) {
                is ReactionEvent.ReactionAdded -> reactionEvent.reaction
                is ReactionEvent.ReactionRemoved -> reactionEvent.reaction
            }
        )
    }
}