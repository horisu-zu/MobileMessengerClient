package com.example.testapp.presentation.chat.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.domain.models.message.Message
import com.example.testapp.presentation.chat.bottomsheet.chat.formatDate
import com.example.testapp.presentation.templates.Avatar
import com.example.testapp.utils.MarkdownString
import com.example.testapp.utils.UserColorGenerator.toColor

@Composable
fun SearchMessageItem(
    senderData: UserResponse,
    message: Message,
    messageAttachments: List<Attachment>?,
    replyMessage: Message?,
    replyUserData: UserResponse?,
    onMessageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val lineColor = MaterialTheme.colorScheme.onSurface
    val lineWidth = with(density) { 2.dp.toPx() }
    val avatarSizePx = with(density) { 48.dp.toPx() }
    val verticalSpacingPx = with(density) { 12.dp.toPx() }
    val horizontalLineIndentPx = with(density) { 24.dp.toPx() }
    val lineStartX = avatarSizePx / 2

    Column(
        modifier = modifier.fillMaxWidth()
            .clickable { onMessageClick(message.messageId!!) }
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .drawBehind {
                if(replyMessage != null) {
                    val startY = verticalSpacingPx * 1.5f

                    drawReplyLine(
                        lineColor = lineColor,
                        lineWidthPx = lineWidth,
                        startX = lineStartX,
                        startY = startY,
                        cornerY = verticalSpacingPx,
                        endX = lineStartX + horizontalLineIndentPx
                    )
                }
            }
    ) {
        if(replyMessage != null) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min)
                    .padding(start = with(density) { (lineStartX + horizontalLineIndentPx + 8f).toDp() }),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Avatar(
                    avatarUrl = replyUserData?.avatarUrl ?: "",
                    modifier = Modifier.size(24.dp)
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "@${replyUserData?.nickname}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = replyUserData?.userColor?.toColor() ?: Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if(!replyMessage.message.isNullOrEmpty()) {
                        Text(
                            text = MarkdownString.parseMarkdown(replyMessage.message, MaterialTheme.colorScheme.primary),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            /*Box(
                modifier = Modifier.size(48.dp).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )*/
            Avatar(
                avatarUrl = senderData.avatarUrl,
                modifier = Modifier.size(48.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = senderData.nickname,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = senderData.userColor.toColor(),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatDate(message.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    )
                }
                if(message.message.isNullOrBlank()) {
                    // Attachment
                } else {
                    Text(
                        text = MarkdownString.parseMarkdown(message.message, MaterialTheme.colorScheme.primary),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ), maxLines = 5
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawReplyLine(
    lineColor: Color,
    lineWidthPx: Float,
    startX: Float,
    startY: Float,
    cornerY: Float,
    endX: Float,
    cornerRadius: Float = 16f
) {
    val path = Path().apply {
        moveTo(startX, startY)

        lineTo(startX, cornerY + cornerRadius)

        quadraticTo(
            startX, cornerY,
            startX + cornerRadius, cornerY
        )

        lineTo(endX, cornerY)
    }

    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(
            width = lineWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}