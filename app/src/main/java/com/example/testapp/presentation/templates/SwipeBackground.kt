package com.example.testapp.presentation.templates

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.testapp.R
import kotlin.math.absoluteValue

@Composable
fun MessageSwipeBackground(
    dismissState: SwipeToDismissBoxState,
    threshold: Float,
    modifier: Modifier = Modifier
) {
    var swipeProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(dismissState) {
        snapshotFlow { dismissState.requireOffset() }
            .collect { offset ->
                swipeProgress = (offset.absoluteValue / threshold).coerceIn(0f, 1f)
            }
    }

    if (swipeProgress > 0f) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
            ) {
                CircularProgress(
                    progress = swipeProgress,
                    modifier = Modifier.size(24.dp)
                )

                Icon(
                    painter = painterResource(R.drawable.ic_reply),
                    contentDescription = "Reply",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun CircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
    progressColor: Color = MaterialTheme.colorScheme.secondary
) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = backgroundColor,
            radius = size.minDimension / 2
        )

        val sweepAngle = progress * 360f
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 2.dp.toPx()),
            size = Size(size.width, size.height)
        )
    }
}