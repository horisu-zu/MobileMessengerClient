package chat.service.course.dto

data class ChatJoinRequest(
    val userId: String,
    val inviteCode: String? = null
)
