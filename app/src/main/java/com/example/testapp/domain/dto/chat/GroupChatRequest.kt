package chat.service.course.dto

data class GroupChatRequest(
    val name: String,
    val creatorId: String,
    val participants: List<String>,
    val description: String? = null,
    val maxMembers: Int? = null,
    val avatarUrl: String,
    val isPublic: Boolean
)
