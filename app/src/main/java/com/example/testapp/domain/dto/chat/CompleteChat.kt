package chat.service.course.dto

import com.example.testapp.domain.models.chat.Chat
import com.example.testapp.domain.models.chat.ChatMetadata
import com.example.testapp.domain.models.chat.ChatParticipant

data class CompleteChat(
    val chatData: Chat,
    val chatMetadata: ChatMetadata?,
    val chatParticipants: List<ChatParticipant>
)