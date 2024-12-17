package chat.service.course.dto

data class ChatCreateResponse(
    val chatId: String,
    val status: String,
    val message: String
)
