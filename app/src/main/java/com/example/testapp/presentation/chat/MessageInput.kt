package com.example.testapp.presentation.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.testapp.R
import com.example.testapp.domain.dto.message.MessageRequest
import com.example.testapp.domain.dto.message.MessageUpdateRequest
import com.example.testapp.domain.models.message.Message

@Composable
fun MessageInput(
    messageText: String,
    editingMessage: Message?,
    messageRequest: MessageRequest?,
    onMessageChange: (String) -> Unit,
    onSendMessage: (MessageRequest) -> Unit,
    onMediaClick: () -> Unit,
    onUpdateMessage: (String, MessageUpdateRequest) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = messageText,
            onValueChange = { newValue ->
                Log.d("MessageInput", "TextField value changed: '$newValue'")
                onMessageChange(newValue)
            },
            modifier = Modifier
                .weight(1f)
                .background(color = MaterialTheme.colorScheme.surfaceVariant),
            placeholder = { Text("Type a message") },
            maxLines = 4,
            //visualTransformation = MessageVisualTransformation(availableStyles),
            colors = messageInputColors()
        )
        IconButton(onClick = onMediaClick ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_clip),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        IconButton(
            onClick = {
                if (editingMessage != null) {
                    editingMessage.messageId?.let { messageId ->
                        onUpdateMessage(
                            messageId,
                            MessageUpdateRequest(message = messageText)
                        )
                    }
                } else {
                    messageRequest?.copy(message = messageText)?.let { messageText ->
                        onSendMessage(messageText)
                    }
                }
            },
            enabled = messageText.isNotBlank()
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun messageInputColors() = TextFieldDefaults.textFieldColors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedIndicatorColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary
)