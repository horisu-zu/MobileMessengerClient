package com.example.testapp.domain.dto.reaction

import com.example.testapp.domain.models.reaction.Reaction

data class ReactionEventWrapper(
    val type: String,
    val reaction: Reaction
)
