package com.example.testapp.domain.dto.reaction

import com.example.testapp.domain.models.reaction.Reaction
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
sealed class ReactionEvent {
    abstract val type: String

    @JsonClass(generateAdapter = true)
    data class ReactionAdded(val reaction: Reaction) : ReactionEvent() {
        override val type: String = "ReactionAdded"
    }

    @JsonClass(generateAdapter = true)
    data class ReactionRemoved(val reaction: Reaction) : ReactionEvent() {
        override val type: String = "ReactionRemoved"
    }
}