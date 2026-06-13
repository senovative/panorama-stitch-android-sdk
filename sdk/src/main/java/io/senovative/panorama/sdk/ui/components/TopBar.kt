package io.senovative.panorama.sdk.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.senovative.panorama.sdk.PanoramaDirection
import io.senovative.panorama.sdk.ui.theme.Cyan400
import io.senovative.panorama.sdk.ui.theme.Cyan900
import io.senovative.panorama.sdk.ui.theme.GlassBorder
import io.senovative.panorama.sdk.ui.theme.GlassSurface

@Composable
internal fun TopBar(
    direction: PanoramaDirection,
    frameCount: Int,
    enabled: Boolean,
    onDirectionChange: (PanoramaDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassSurface.copy(alpha = 0.88f))
            .border(1.dp, GlassBorder, shape)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Panorama",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                AnimatedFrameCounter(frameCount)
            }
            SegmentedDirectionControl(selected = direction, enabled = enabled, onSelect = onDirectionChange)
        }
    }
}

@Composable
private fun AnimatedFrameCounter(frameCount: Int) {
    val animatedCount by animateIntAsState(frameCount, tween(300), label = "fCount")
    Text(
        text = if (animatedCount == 0) "Ready" else "$animatedCount frames",
        style = MaterialTheme.typography.bodySmall,
        color = if (frameCount > 0) Cyan400.copy(alpha = 0.9f)
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    )
}

@Composable
private fun SegmentedDirectionControl(
    selected: PanoramaDirection, enabled: Boolean, onSelect: (PanoramaDirection) -> Unit
) {
    val containerShape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .clip(containerShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), containerShape)
            .padding(3.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        PanoramaDirection.entries.forEach { dir ->
            val isSelected = dir == selected
            val bgColor by animateColorAsState(
                if (isSelected) Cyan400 else Color.Transparent, tween(250), label = "segBg"
            )
            val contentColor by animateColorAsState(
                if (isSelected) Cyan900 else Color.White.copy(alpha = 0.55f), tween(250), label = "segC"
            )
            val itemShape = RoundedCornerShape(8.dp)
            Row(
                modifier = Modifier
                    .clip(itemShape)
                    .background(bgColor)
                    .clickable(
                        enabled = enabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(dir) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    if (dir == PanoramaDirection.Horizontal) Icons.Default.SwapHoriz else Icons.Default.SwapVert,
                    null, tint = contentColor, modifier = Modifier.size(16.dp)
                )
                Text(
                    if (dir == PanoramaDirection.Horizontal) "H" else "V",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}
