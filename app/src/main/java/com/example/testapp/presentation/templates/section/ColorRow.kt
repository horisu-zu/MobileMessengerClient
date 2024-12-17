package com.example.testapp.presentation.templates.section

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.testapp.R

@Composable
fun ColorRow(
    colors: List<Color>,
    currentColor: Color? = null,
    modifier: Modifier,
    onColorSelected: (Color) -> Unit
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        val (desc, row) = createRefs()

        Text(
            text = stringResource(id = R.string.chat_color_title),
            modifier = Modifier.constrainAs(desc) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        LazyRow(
            modifier = Modifier.constrainAs(row) {
                top.linkTo(desc.bottom, margin = 4.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(colors) { color ->
                ColorItem(
                    color = color,
                    isCurrent = (color == currentColor),
                    onSelect = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
fun ColorItem(
    color: Color,
    isCurrent: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable { onSelect() }
            .background(color)
            .border(
                width = if (isCurrent) 2.dp else 0.dp,
                color = if (isCurrent) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = CircleShape
            )
    )
}