package com.example.testapp.domain.dto.chat

sealed class SearchFilter(val displayValue: String, val value: String) {
    data class FromUser(val userId: String, val userName: String) : SearchFilter(userName, userId)
    data object HasAttachments: SearchFilter("has attachments:", "true")

    sealed class SortDirection(displayValue: String, value: String) : SearchFilter(displayValue, value) {
        data object Ascending : SortDirection("Oldest", "ASC")
        data object Descending : SortDirection("Newest", "DESC")
    }
}
