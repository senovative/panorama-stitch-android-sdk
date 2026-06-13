package io.senovative.panorama.sdk.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.senovative.panorama.sdk.ui.model.PanoramaUiState
import io.senovative.panorama.sdk.ui.theme.Cyan400
import io.senovative.panorama.sdk.ui.theme.Cyan900
import io.senovative.panorama.sdk.ui.theme.GlassBorder
import io.senovative.panorama.sdk.ui.theme.GlassSurface
import io.senovative.panorama.sdk.ui.theme.RecordRed

@Composable
internal fun BottomPanel(
    state: PanoramaUiState,
    canRecord: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onFinish: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassSurface.copy(alpha = 0.90f))
            .border(1.dp, GlassBorder, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatusMessage(state.savedUri ?: state.message, state.isProcessing)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallActionButton(Icons.Default.RestartAlt, "Reset",
                !state.isProcessing && !state.isRecording, onReset)
            HeroRecordButton(state.isRecording,
                canRecord && !state.isProcessing && state.resultBitmap == null,
                if (state.isRecording) onStopRecording else onStartRecording)
            SmallActionButton(Icons.Default.Check, "Render",
                state.frameCount >= 2 && !state.isProcessing && !state.isRecording && state.resultBitmap == null,
                onFinish)
        }
    }
}

@Composable
private fun StatusMessage(message: String, isProcessing: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        AnimatedVisibility(isProcessing, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Cyan400)
        }
        AnimatedContent(
            message,
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically { it / 2 })
                    .togetherWith(fadeOut(tween(200)) + slideOutVertically { -it / 2 })
            }, label = "statusMsg", modifier = Modifier.weight(1f)
        ) { msg ->
            Text(msg, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun HeroRecordButton(isRecording: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val ringScale by pulse.animateFloat(1f, 1.20f,
        infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "rS")
    val ringAlpha by pulse.animateFloat(0.6f, 0.15f,
        infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "rA")
    val btnColor by animateColorAsState(
        if (isRecording) RecordRed else Cyan400, tween(350), label = "hClr")
    val iconColor by animateColorAsState(
        if (isRecording) Color.White else Cyan900, tween(350), label = "hIcn")

    Box(contentAlignment = Alignment.Center) {
        if (isRecording) {
            Box(Modifier.size(78.dp).scale(ringScale).clip(CircleShape)
                .background(RecordRed.copy(alpha = ringAlpha)))
        }
        Box(
            Modifier.size(64.dp).clip(CircleShape)
                .background(if (enabled) btnColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = enabled, onClick = onClick),
            Alignment.Center
        ) {
            AnimatedContent(isRecording, transitionSpec = {
                (scaleIn(tween(200)) + fadeIn()).togetherWith(scaleOut(tween(200)) + fadeOut())
            }, label = "hIcon") { recording ->
                Icon(
                    if (recording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    if (recording) "Stop" else "Record",
                    tint = if (enabled) iconColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
                    modifier = Modifier.size(if (recording) 28.dp else 22.dp)
                )
            }
        }
    }
}

@Composable
private fun SmallActionButton(
    icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit
) {
    val alpha by animateFloatAsState(if (enabled) 1f else 0.28f, tween(250), label = "saA")
    Column(
        Modifier.clip(RoundedCornerShape(12.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            Modifier.size(42.dp).clip(CircleShape).background(
                if (enabled) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.03f)
            ), Alignment.Center
        ) {
            Icon(icon, label, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                modifier = Modifier.size(20.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.65f))
    }
}
