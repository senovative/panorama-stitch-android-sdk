package io.senovative.panorama.sdk.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.senovative.panorama.sdk.PanoramaDirection
import io.senovative.panorama.sdk.ui.theme.Cyan400
import io.senovative.panorama.sdk.ui.theme.RecordRed

@Composable
internal fun PanoramaGuideOverlay(
    direction: PanoramaDirection,
    guideBitmap: Bitmap?,
    liveBitmap: Bitmap?,
    frameCount: Int,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = Color.White.copy(alpha = 0.12f)
            val sw = 0.5f
            listOf(size.width / 3f, size.width * 2f / 3f).forEach { x ->
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), sw, StrokeCap.Butt)
            }
            listOf(size.height / 3f, size.height * 2f / 3f).forEach { y ->
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), sw, StrokeCap.Butt)
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
                    endY = size.height * 0.14f
                )
            )
            drawRect(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                    startY = size.height * 0.86f
                )
            )
        }

        if (frameCount == 0 || guideBitmap == null) {
            liveBitmap?.let {
                GuideLiveThumbnail(
                    bitmap = it,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(width = 78.dp, height = 108.dp)
                )
            }
        } else {
            when (direction) {
                PanoramaDirection.Horizontal -> HorizontalFrameGuide(
                    bitmap = guideBitmap,
                    liveBitmap = liveBitmap,
                    modifier = Modifier.align(Alignment.Center)
                )

                PanoramaDirection.Vertical -> VerticalFrameGuide(
                    bitmap = guideBitmap,
                    liveBitmap = liveBitmap,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        if (isRecording) {
            RecordingIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 80.dp, end = 20.dp)
            )
        }
    }
}

@Composable
private fun RecordingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "rec")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recAlpha"
    )
    Box(
        modifier = modifier
            .size(10.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(RecordRed)
    )
}

@Composable
private fun HorizontalFrameGuide(
    bitmap: Bitmap, liveBitmap: Bitmap?, modifier: Modifier = Modifier
) {
    Row(modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        GuideReferenceThumbnail(bitmap, Modifier.size(width = 68.dp, height = 94.dp))
        Spacer(Modifier.width(3.dp))
        GuideLiveThumbnail(liveBitmap ?: bitmap, Modifier.size(width = 68.dp, height = 94.dp))
        Spacer(Modifier.width(3.dp))
        GuideTargetBox(Modifier.size(width = 68.dp, height = 94.dp), Alignment.CenterStart) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                tint = Cyan400.copy(alpha = 0.8f), modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun VerticalFrameGuide(
    bitmap: Bitmap, liveBitmap: Bitmap?, modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        GuideReferenceThumbnail(bitmap, Modifier.size(width = 94.dp, height = 68.dp))
        Spacer(Modifier.height(3.dp))
        GuideLiveThumbnail(liveBitmap ?: bitmap, Modifier.size(width = 94.dp, height = 68.dp))
        Spacer(Modifier.height(3.dp))
        GuideTargetBox(Modifier.size(width = 94.dp, height = 68.dp), Alignment.TopCenter) {
            Icon(
                Icons.Default.KeyboardArrowDown, null,
                tint = Cyan400.copy(alpha = 0.8f), modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun GuideTargetBox(
    modifier: Modifier = Modifier, contentAlignment: Alignment, content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "dash")
    val phase by transition.animateFloat(
        0f, 14f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "dashPhase"
    )
    val shape = RoundedCornerShape(8.dp)
    Box(modifier.clip(shape).background(Color.Black.copy(alpha = 0.2f)), contentAlignment) {
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.55f),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(8.dp.toPx(), 6.dp.toPx()), phase * density
                    )
                ),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
        }
        content()
    }
}

@Composable
private fun GuideReferenceThumbnail(bitmap: Bitmap, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Image(
        bitmap = bitmap.asImageBitmap(), contentDescription = "Last captured frame",
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.25f), shape)
            .background(Color.Black).alpha(0.5f)
    )
}

@Composable
private fun GuideLiveThumbnail(bitmap: Bitmap, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Image(
        bitmap = bitmap.asImageBitmap(), contentDescription = "Live preview",
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(shape)
            .border(2.dp, Brush.linearGradient(listOf(Cyan400, Cyan400.copy(alpha = 0.5f))), shape)
            .background(Color.Black)
    )
}
