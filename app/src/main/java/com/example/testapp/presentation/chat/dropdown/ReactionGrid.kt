package com.example.testapp.presentation.chat.dropdown

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReactionGrid(
    reactions: List<String>,
    onReactionSelected: (String) -> Unit,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val itemSize = 48.dp
    val itemPadding = 4.dp
    val totalItemWidth = with(density) { (itemSize + itemPadding * 2).toPx() }

    BoxWithConstraints(modifier = modifier) {
        val containerWidth = constraints.maxWidth

        val maxRowItems = max(1, (containerWidth / totalItemWidth).toInt())

        Column {
            if (expanded) {
                FlowRow(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    reactions.forEach { reaction ->
                        ReactionItem(itemSize, itemPadding, reaction, onReactionSelected)
                    }
                    Box(
                        modifier = Modifier
                            .size(itemSize)
                            .padding(itemPadding)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        IconButton(onClick = onExpandToggle) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Show less reactions"
                            )
                        }
                    }
                }
            } else {
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    reactions.take(maxRowItems).forEach { reaction ->
                        ReactionItem(itemSize, itemPadding, reaction, onReactionSelected)
                    }
                    if (reactions.size > maxRowItems) {
                        Box(
                            modifier = Modifier
                                .size(itemSize)
                                .padding(itemPadding)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            IconButton(onClick = onExpandToggle) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Show more reactions"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionItem(
    itemSize: Dp,
    itemPadding: Dp,
    reactionUrl: String,
    onReactionSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .size(itemSize)
            .padding(itemPadding)
            .clickable { onReactionSelected(reactionUrl) }
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = reactionUrl),
            contentDescription = "Reaction",
            modifier = Modifier.size(32.dp)
        )
    }
}