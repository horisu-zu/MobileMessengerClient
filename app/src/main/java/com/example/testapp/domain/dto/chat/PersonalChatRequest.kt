package chat.service.course.dto

data class PersonalChatRequest(
    val firstUserId: String,
    val secondUserId: String
)
