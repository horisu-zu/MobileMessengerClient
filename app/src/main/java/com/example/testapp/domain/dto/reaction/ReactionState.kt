package com.example.testapp.domain.dto.reaction

import com.example.testapp.domain.models.reaction.Reaction

data class ReactionState(
    val reactions: Map<String, List<Reaction>> = emptyMap(),
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)
