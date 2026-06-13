package io.senovative.panorama.sdk.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Premium Colour Palette ──────────────────────────────────────────────────────
val Cyan400 = Color(0xFF5BEAD6)
val Cyan500 = Color(0xFF3DD8C5)
val Cyan900 = Color(0xFF0A2E2B)
val Gold400 = Color(0xFFF0C56D)
val Gold900 = Color(0xFF2D2210)
val Navy50 = Color(0xFFE4EBF1)
val Navy200 = Color(0xFF8A9BB0)
val Navy800 = Color(0xFF141B24)
val Navy900 = Color(0xFF0A0E14)

val GlassSurface = Color(0xFF161D27)
val GlassBorder = Color(0x14FFFFFF)      // white 8 %
val RecordRed = Color(0xFFFF4757)
val ErrorRed = Color(0xFFFF6B6B)

// ── Colour Scheme ───────────────────────────────────────────────────────────────
private val PanoramaDarkScheme = darkColorScheme(
    primary = Cyan400,
    onPrimary = Cyan900,
    secondary = Gold400,
    onSecondary = Gold900,
    background = Navy900,
    onBackground = Navy50,
    surface = Navy800,
    onSurface = Navy50,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = Navy200,
    error = ErrorRed,
    onError = Color.White
)

// ── Typography ──────────────────────────────────────────────────────────────────
private val PanoramaTypography = Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )
)

// ── Shapes ───────────────────────────────────────────────────────────────────────
private val PanoramaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// ── Entry Point ──────────────────────────────────────────────────────────────────
@Composable
internal fun PanoramaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PanoramaDarkScheme,
        typography = PanoramaTypography,
        shapes = PanoramaShapes,
        content = content
    )
}
