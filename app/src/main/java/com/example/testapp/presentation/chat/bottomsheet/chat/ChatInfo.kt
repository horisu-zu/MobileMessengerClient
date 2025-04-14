package com.example.testapp.presentation.chat.bottomsheet.chat

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.testapp.R
import com.example.testapp.domain.models.chat.Chat
import com.example.testapp.domain.models.chat.ChatMetadata
import com.example.testapp.presentation.templates.DashedDivider
import com.example.testapp.presentation.user.bottomsheet.DescriptionItem

@Composable
fun ChatInfo(
    chatData: Chat,
    chatMetadata: ChatMetadata,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if(!chatMetadata.description.isNullOrEmpty()) {
            DescriptionItem(
                description = chatMetadata.description,
                context = context
            )
            DashedDivider(modifier = Modifier.fillMaxWidth())
        }

        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ChatInfoItem(
                icon = Icons.Default.DateRange,
                label = context.getString(R.string.cbs_created_at),
                text = formatDate(chatData.createdAt),
                textSize = 12.sp
            )

            ChatInfoItem(
                icon = Icons.Default.Person,
                label = context.getString(R.string.cbs_chat_type),
                text = if(chatMetadata.isPublic) context.getString(R.string.cbs_public_type) else context.getString(R.string.cbs_private_type),
                textSize = 12.sp
            )
        }
    }
}