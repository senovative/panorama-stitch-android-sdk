package io.senovative.panorama.sdk.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.senovative.panorama.sdk.ui.theme.Cyan400
import io.senovative.panorama.sdk.ui.theme.GlassBorder
import io.senovative.panorama.sdk.ui.theme.GlassSurface
import io.senovative.panorama.sdk.ui.theme.Gold400

@Composable
internal fun ResultPreview(
    bitmap: Bitmap,
    savedUri: String?,
    isProcessing: Boolean,
    onSave: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.background(Color(0xF0000000)), Alignment.Center) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Spacer(Modifier.weight(0.08f))

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Panorama result",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(Modifier.height(24.dp))

            val barShape = RoundedCornerShape(16.dp)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    .clip(barShape)
                    .background(GlassSurface.copy(alpha = 0.92f))
                    .border(1.dp, GlassBorder, barShape)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionChip(Icons.Default.Save,
                    if (savedUri != null) "Saved ✓" else "Save to Gallery",
                    !isProcessing && savedUri == null,
                    if (savedUri != null) Cyan400 else Gold400, onSave)
                ActionChip(Icons.Default.Close, "Close", !isProcessing,
                    Color.White.copy(alpha = 0.7f), onClose)
            }

            Spacer(Modifier.navigationBarsPadding().height(28.dp))
        }
    }
}

@Composable
private fun ActionChip(
    icon: ImageVector, label: String, enabled: Boolean, accent: Color, onClick: () -> Unit
) {
    TextButton(onClick, enabled = enabled, shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = accent)
    }
}
