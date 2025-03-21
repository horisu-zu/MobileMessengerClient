package com.example.testapp.presentation.chat.attachment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.utils.CacheManager
import com.example.testapp.utils.Converter.formatDuration
import com.example.testapp.utils.Converter.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun VideoAttachment(
    attachment: Attachment,
    modifier: Modifier,
    shape: Shape,
    context: Context = LocalContext.current
) {
    var isFullScreen by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        VideoThumbnail(
            attachment = attachment,
            modifier = Modifier.fillMaxWidth(),
            context = context,
            shape = shape,
            onVideoClick = { isFullScreen = true }
        )
    }

    if(isFullScreen) {
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black)
            ) {
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                val transformableState =
                    rememberTransformableState { zoomChange, panChange, rotationChange ->
                        scale = (scale * zoomChange).coerceIn(1f, 3f)

                        val maxX = (scale - 1) * 500
                        val maxY = (scale - 1) * 500

                        offset = Offset(
                            x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                            y = (offset.y + panChange.y).coerceIn(-maxY, maxY)
                        )
                    }

                val cache = remember { CacheManager.getCache(context) }
                val cacheDataSourceFactory = remember {
                    CacheDataSource.Factory()
                        .setCache(cache)
                        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                }

                val videoPlayer = remember {
                    ExoPlayer.Builder(context)
                        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
                        .build()
                }

                LaunchedEffect(videoPlayer) {
                    videoPlayer.playWhenReady = true
                    videoPlayer.setMediaItem(MediaItem.fromUri(attachment.url))
                    videoPlayer.prepare()
                }

                DisposableEffect(Unit) {
                    onDispose {
                        videoPlayer.release()
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = videoPlayer
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .transformable(transformableState)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                )
            }
        }
    }
}

@Composable
fun VideoThumbnail(
    attachment: Attachment,
    shape: Shape,
    context: Context,
    onVideoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var aspectRatio by remember { mutableFloatStateOf(16f/9f) }
    var duration by remember { mutableLongStateOf(0) }

    LaunchedEffect(attachment.url) {
        withContext(Dispatchers.IO) {
            try {
                bitmap = loadBitmapFromCache(context, attachment.url)

                if (bitmap == null) {
                    val retriever = MediaMetadataRetriever()
                    val headers = HashMap<String, String>()
                    headers["Range"] = "bytes=0-1000000"

                    retriever.setDataSource(attachment.url, headers)
                    bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST)

                    try {
                        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toFloatOrNull() ?: 16f
                        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toFloatOrNull() ?: 9f
                        duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                        aspectRatio = width / height
                    } catch (e: Exception) {
                        Log.e("VideoThumbnail", "Error: ${e.message}")
                    }

                    retriever.release()

                    bitmap?.let { saveBitmapToCache(context, attachment.url, it) }
                } else {
                    bitmap?.let {
                        aspectRatio = it.width.toFloat() / it.height.toFloat()
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoThumbnail", "Error: ${e.message}")
            }
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .padding(2.dp)
            .clip(shape)
            .clickable { onVideoClick() }
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = attachment.name,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${formatDuration(duration)} | ${formatFileSize(attachment.fileSize)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play video",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
                .padding(8.dp)
        )
    }
}

private fun getCacheFile(context: Context, url: String): File {
    return File(context.cacheDir, "video_thumb_${url.hashCode()}.jpg")
}

private fun saveBitmapToCache(context: Context, url: String, bitmap: Bitmap) {
    try {
        getCacheFile(context, url).outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
    } catch (e: Exception) {
        Log.e("Cache", "Save error: ${e.message}")
    }
}

private fun loadBitmapFromCache(context: Context, url: String): Bitmap? {
    val file = getCacheFile(context, url)
    return if (file.exists()) {
        try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    } else null
}

/*
@Composable
fun VideoThumbnail(
    attachment: Attachment,
    modifier: Modifier,
    context: Context
) {
    val videoPreviewLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }.build()
    }

    val previewRequest = remember(attachment.url) {
        ImageRequest.Builder(context)
            .data(attachment.url)
            .size(600)
            .memoryCacheKey("${attachment.url}_preview")
            .diskCacheKey("${attachment.url}_preview")
            .build()
    }

    Box(
        modifier = modifier
    ) {
        AsyncImage(
            model = previewRequest,
            imageLoader = videoPreviewLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(),
            onError = {
                Log.e("ThumbnailTest", "Failed to load preview: ${it.result.throwable}")
            }
        )
    }
}*/