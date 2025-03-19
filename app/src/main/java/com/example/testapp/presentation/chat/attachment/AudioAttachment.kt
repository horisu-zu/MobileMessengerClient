package com.example.testapp.presentation.chat.attachment

import android.media.MediaPlayer
import android.text.format.Formatter.formatFileSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.utils.Converter
import com.example.testapp.utils.Converter.formatAudioProgress
import com.example.testapp.utils.Converter.formatFileSize
import kotlinx.coroutines.delay

@Composable
fun AudioAttachment(
    attachment: Attachment,
    modifier: Modifier = Modifier
) {
    //Expected UI: Play/Pause Button -> Interactive Slider with position and duration -> Time

    val audioPlayer = remember {
        MediaPlayer().apply {
            setDataSource(attachment.url)
            prepareAsync()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by rememberSaveable { mutableStateOf(0) }
    var duration by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(audioPlayer) {
        audioPlayer.setOnPreparedListener { player ->
            duration = player.duration
        }

        while (true) {
            delay(1000)
            if (isPlaying) {
                currentPosition = audioPlayer.currentPosition
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = formatFileSize(attachment.fileSize),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    if (isPlaying) {
                        audioPlayer.pause()
                    } else {
                        audioPlayer.start()
                    }
                    isPlaying = !isPlaying
                }
            ) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }

            if (duration > 0) {
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = { newPosition ->
                        currentPosition = newPosition.toInt()
                        audioPlayer.seekTo(currentPosition)
                    },
                    valueRange = 0f..duration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(max = 32.dp)
                )
            }
        }

        Text(
            text = formatAudioProgress(currentPosition, duration),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp
            )
        )
    }
}