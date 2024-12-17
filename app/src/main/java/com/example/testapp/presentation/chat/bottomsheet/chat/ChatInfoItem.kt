package com.example.testapp.presentation.chat.bottomsheet.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ChatInfoItem(
    icon: ImageVector,
    label: String,
    text: String,
    modifier: Modifier = Modifier,
    textSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize
) {
    ConstraintLayout(modifier = modifier
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.background)
        .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        val (iconRef, labelRef, textRef) = createRefs()

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
                .constrainAs(iconRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(labelRef) {
                start.linkTo(iconRef.end, margin = 12.dp)
                top.linkTo(parent.top)
            }
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = textSize),
            modifier = Modifier.constrainAs(textRef) {
                start.linkTo(labelRef.start)
                top.linkTo(labelRef.bottom, margin = 4.dp)
            }
        )
    }
}

fun formatDate(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}