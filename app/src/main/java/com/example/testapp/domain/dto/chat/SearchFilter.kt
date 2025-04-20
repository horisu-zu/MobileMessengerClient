package com.example.testapp.domain.dto.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.reflect.KClass

interface FilterOption {
    val displayValue: String
    val value: String
}

sealed class FilterCriterion(
    override val displayValue: String,
    override val value: String
) : FilterOption {
    data class FromUser(val userId: String, val userName: String) : FilterCriterion(userName, userId)
    data class AppliedTo(val userId: String, val userName: String): FilterCriterion(userName, userId)
    data object HasAttachments : FilterCriterion("Has attachments", "true")
    //Should add more tbh...
}

data class SortBy(
    val field: SortField,
    val direction: SortDirection
)

sealed class SortField(
    override val displayValue: String,
    override val value: String
): FilterOption {
    data object Date : SortField("Date", "created_at")
    data object User : SortField("Username", "nickname")
    // maybe I'll add something else...
}

sealed class SortDirection(
    override val displayValue: String,
    override val value: String
) : FilterOption {
    data object Ascending : SortDirection("Oldest", "ASC")
    data object Descending : SortDirection("Newest", "DESC")
}

sealed class SearchMenuOption(
    open val prefix: String,
    open val description: String,
    open val icon: ImageVector
) {
    data class AddFilter(
        val filterCriterionType: KClass<out FilterCriterion>,
        override val prefix: String,
        override val description: String,
        override val icon: ImageVector
    ) : SearchMenuOption(prefix, description, icon)

    data object ConfigureSort : SearchMenuOption(
        prefix = "Sort:",
        description = "Configure Sorting",
        icon = Icons.AutoMirrored.Filled.List
    )
}