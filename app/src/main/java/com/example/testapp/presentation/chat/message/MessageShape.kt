package com.example.testapp.presentation.chat.message

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun messageShape(
    isCurrentUser: Boolean,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean
): Shape {
    return when {
        isCurrentUser -> {
            when {
                isLastInGroup -> {
                    RoundedCornerShape(
                        topEnd = 4.dp,
                        topStart = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp

                    )
                }

                isFirstInGroup -> {
                    RoundedCornerShape(
                        topEnd = 16.dp,
                        topStart = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 4.dp
                    )
                }

                else -> {
                    RoundedCornerShape(
                        topStart = 16.dp,
                        bottomStart = 16.dp,
                        topEnd = 4.dp,
                        bottomEnd = 4.dp
                    )
                }
            }
        }

        else -> {
            when {
                isLastInGroup -> {
                    RoundedCornerShape(
                        topEnd = 16.dp,
                        topStart = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                }

                isFirstInGroup -> {
                    RoundedCornerShape(
                        topEnd = 16.dp,
                        topStart = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                }

                else -> {
                    RoundedCornerShape(
                        topStart = 4.dp,
                        bottomStart = 4.dp,
                        topEnd = 16.dp,
                        bottomEnd = 16.dp
                    )
                }
            }
        }
    }
}

fun attachmentShape(
    isCurrentUser: Boolean,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    hasMessageText: Boolean,
    hasReply: Boolean
): Shape = when {
    !hasMessageText && !hasReply -> messageShape(isCurrentUser, isFirstInGroup, isLastInGroup)
    !hasMessageText -> RoundedCornerShape(
        topStart = 4.dp, topEnd = 4.dp,
        bottomStart = if (isCurrentUser || isLastInGroup) 16.dp else 4.dp,
        bottomEnd = if (!isCurrentUser || isLastInGroup) 16.dp else 4.dp
    )
    hasReply -> RoundedCornerShape(4.dp)
    else -> RoundedCornerShape(
        topStart = if (isCurrentUser || isFirstInGroup) 16.dp else 4.dp,
        topEnd = if (!isCurrentUser || isFirstInGroup) 16.dp else 4.dp,
        bottomStart = 4.dp, bottomEnd = 4.dp
    )
}