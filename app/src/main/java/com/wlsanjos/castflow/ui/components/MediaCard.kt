package com.wlsanjos.castflow.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.SubcomposeAsyncImageContent
import com.wlsanjos.castflow.utils.buildCoilImageRequest
import com.wlsanjos.castflow.utils.currentWindowSize
import com.wlsanjos.castflow.utils.WindowSize
import com.wlsanjos.castflow.utils.getVideoDurationMillis
import com.wlsanjos.castflow.utils.formatDuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow

@Composable
fun MediaCard(
    title: String,
    imageUri: Uri? = null,
    isVideo: Boolean = false,
    modifier: Modifier = Modifier,
    targetDpOverride: Dp? = null,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize()
        ) {
            if (imageUri != null) {
                val context = LocalContext.current
                val density = LocalDensity.current
                val windowSize = currentWindowSize()
                val computedTargetDp = targetDpOverride ?: when (windowSize) {
                    WindowSize.Compact -> 200.dp
                    WindowSize.Medium -> 320.dp
                    WindowSize.Expanded -> 480.dp
                }
                val targetPx = with(density) { computedTargetDp.roundToPx() }
                val request = buildCoilImageRequest(
                    context,
                    imageUri,
                    targetPx,
                    precision = coil.size.Precision.EXACT,
                    allowHardware = false
                )

                val durationState = produceState<Long?>(initialValue = null, key1 = imageUri) {
                    value = if (isVideo) getVideoDurationMillis(context, imageUri) else null
                }

                SubcomposeAsyncImage(
                    model = request,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    when (painter.state) {
                        is coil.compose.AsyncImagePainter.State.Loading -> {
                            ImagePlaceholder(modifier = Modifier.matchParentSize())
                        }
                        is coil.compose.AsyncImagePainter.State.Error -> {
                            ImagePlaceholder(modifier = Modifier.matchParentSize())
                        }
                        else -> {
                            SubcomposeAsyncImageContent()
                        }
                    }
                }
                // subtle gradient overlay
                Box(modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                        )
                    )
                )

                if (isVideo) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                    )

                    val durationText = formatDuration(durationState.value)
                    if (durationText != null) {
                        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                            androidx.compose.material3.Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(durationText, color = Color.White, modifier = Modifier.padding(6.dp))
                            }
                        }
                    }
                }

            } else {
                ImagePlaceholder(modifier = Modifier
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp)))
            }

            Text(
                text = title,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}
