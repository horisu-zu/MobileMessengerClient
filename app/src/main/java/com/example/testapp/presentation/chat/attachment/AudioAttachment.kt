package com.example.testapp.presentation.chat.attachment

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.testapp.R
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.utils.CacheManager
import com.example.testapp.utils.Converter.formatAudioProgress
import com.example.testapp.utils.Converter.formatFileSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun AudioAttachment(
    attachment: Attachment,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    val cache = remember { CacheManager.getCache(context) }
    val cacheDataSourceFactory = remember {
        CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    val exoAudioPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build().apply {
                val mediaItem = MediaItem.fromUri(attachment.url)
                setMediaItem(mediaItem)
                prepare()
            }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by rememberSaveable { mutableLongStateOf(0L) }
    var duration by rememberSaveable { mutableLongStateOf(0) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = exoAudioPlayer.currentPosition
            delay(100)
        }
    }

    LaunchedEffect(Unit) {
        exoAudioPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = exoAudioPlayer.duration
                    if (duration <= 0) {
                        launch {
                            while (duration <= 0) {
                                delay(200)
                                duration = exoAudioPlayer.duration
                            }
                        }
                    }
                }
            }
        })
    }

    DisposableEffect(Unit) {
        onDispose {
            exoAudioPlayer.release()
        }
    }

    Column(
        modifier = modifier
    ) {
        Column {
            Text(
                text = attachment.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
            )

            Text(
                text = formatFileSize(attachment.fileSize),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    if (exoAudioPlayer.isPlaying) {
                        exoAudioPlayer.pause()
                    } else {
                        exoAudioPlayer.play()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }

            if (duration > 0) {
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = { newPosition ->
                        currentPosition = newPosition.toLong()
                        exoAudioPlayer.seekTo(currentPosition)
                    },
                    valueRange = 0f..duration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onBackground,
                        activeTrackColor = MaterialTheme.colorScheme.onBackground,
                        inactiveTrackColor = MaterialTheme.colorScheme.surface.copy(0.5f)
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