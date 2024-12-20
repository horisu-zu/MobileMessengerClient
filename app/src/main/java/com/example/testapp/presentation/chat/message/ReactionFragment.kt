package com.example.testapp.presentation.chat.message

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.reaction.Reaction
import com.example.testapp.presentation.templates.Avatar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReactionFragment(
    groupedReactions: Map<String, List<Reaction>>,
    usersData: Map<String, UserResponse>,
    messageId: String,
    currentUserId: String,
    onReactionClick: (String, String, String) -> Unit,
    onReactionLongClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalReactions = remember(groupedReactions) {
        groupedReactions.values.flatten().size
    }

    FlowRow(
        modifier = modifier
            .padding(top = 2.dp)
            .animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        groupedReactions.forEach { (emoji, reactionList) ->
            key(emoji) {
                val reactionUsers by remember(reactionList, usersData) {
                    derivedStateOf {
                        reactionList.mapNotNull { reaction ->
                            usersData[reaction.userId]
                        }
                    }
                }
                val hasCurrentUserReacted by remember(reactionList, currentUserId) {
                    derivedStateOf { reactionList.any { it.userId == currentUserId } }
                }

                AnimatedVisibility(
                    visible = reactionList.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.padding(4.dp)
                ) {
                    ReactionFragmentItem(
                        reactionUrl = emoji,
                        reactionCount = reactionList.size,
                        usersData = reactionUsers,
                        hasCurrentUserReacted = hasCurrentUserReacted,
                        totalReactions = totalReactions,
                        onClick = {
                            onReactionClick(messageId, currentUserId, emoji)
                        },
                        onLongClick = {
                            onReactionLongClick(emoji)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReactionFragmentItem(
    reactionUrl: String,
    reactionCount: Int,
    usersData: List<UserResponse?>,
    hasCurrentUserReacted: Boolean,
    totalReactions: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (hasCurrentUserReacted)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .background(backgroundColor, CircleShape)
            .clip(CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(reactionUrl),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        if (totalReactions > 3) {
            AnimatedReactionCount(reactionCount = reactionCount)
        } else {
            ReactionAvatarsGroup(
                usersData = usersData,
                backgroundColor = backgroundColor
            )
        }
    }
}

@Composable
fun AnimatedReactionCount(
    reactionCount: Int,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = reactionCount,
        transitionSpec = {
            slideInVertically { height -> height } + fadeIn() togetherWith
                    slideOutVertically { height -> -height } + fadeOut()
        },
        modifier = modifier, label = ""
    ) { targetCount ->
        Text(
            text = targetCount.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ReactionAvatarsGroup(
    usersData: List<UserResponse?>,
    backgroundColor: Color,
    avatarSize: Dp = 20.dp,
    overlapOffset: Dp = 12.dp
) {
    val visibleAvatars = usersData.take(3)
    val modifier = if (visibleAvatars.size > 1) {
        val totalWidth = avatarSize + (overlapOffset * (visibleAvatars.size - 1))
        Modifier.width(totalWidth)
    } else {
        Modifier
    }

    Box(modifier = modifier) {
        visibleAvatars.forEachIndexed { index, userData ->
            ReactionAvatarWrapper(
                backgroundColor = backgroundColor,
                modifier = Modifier.offset(x = (overlapOffset * index))
            ) {
                Avatar(
                    avatarUrl = userData?.avatarUrl ?: "",
                    modifier = Modifier.size(avatarSize),
                    isGroupChat = true
                )
            }
        }
    }
}

@Composable
private fun ReactionAvatarWrapper(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(2.dp)
    ) {
        content()
    }
}