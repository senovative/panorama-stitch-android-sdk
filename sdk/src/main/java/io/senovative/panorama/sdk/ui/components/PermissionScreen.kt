package io.senovative.panorama.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.senovative.panorama.sdk.ui.theme.Cyan400
import io.senovative.panorama.sdk.ui.theme.Cyan900
import io.senovative.panorama.sdk.ui.theme.Navy900

@Composable
internal fun PermissionScreen(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Navy900, Color(0xFF0E1A26), Navy900))
        ), Alignment.Center
    ) {
        Column(
            Modifier.padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(Modifier.size(96.dp).clip(CircleShape).background(Cyan400.copy(alpha = 0.08f)),
                Alignment.Center) {
                Box(Modifier.size(72.dp).clip(CircleShape).background(Cyan400.copy(alpha = 0.14f)),
                    Alignment.Center) {
                    Icon(Icons.Outlined.PhotoCamera, null, tint = Cyan400, modifier = Modifier.size(36.dp))
                }
            }
            Text("Camera Access", style = MaterialTheme.typography.titleLarge,
                color = Color.White, fontWeight = FontWeight.Bold)
            Text("Panorama Camera needs access to your camera to capture and stitch panoramic photos.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.55f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRequestPermission, shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Cyan900),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)) {
                Text("Enable Camera", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}
