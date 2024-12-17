package com.example.testapp.domain.models.chat


data class ChatMetadata(
    val chatId: String,
    val name: String,
    val avatar: String,
    val description: String?,
    val creatorId: String,
    val maxMembers: Int,
    val inviteCode: String? = null,
    val isPublic: Boolean = false
)

fun ChatMetadata.toUpdatedData(newData: ChatMetadata): ChatMetadata = this.copy(
    name = if (this.name != newData.name) newData.name else this.name,
    avatar = if (this.avatar != newData.avatar) newData.avatar else this.avatar,
    description = if (this.description != newData.description) newData.description else this.description,
    maxMembers = if (this.maxMembers != newData.maxMembers) newData.maxMembers else this.maxMembers,
    isPublic = if (this.isPublic != newData.isPublic) newData.isPublic else this.isPublic
)
