package com.automatelinux.localKnowledge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp

/**
 * A field notebook, not a database client.
 *
 * Warm paper and ink by day; by night a properly dark ground, because half of what
 * this app is for happens after dark at the side of a road — a white screen there
 * costs you your night vision and is the first thing that makes an app get left
 * in the pocket.
 */
private val Pine = Color(0xFF2F5D4A)
private val PineLight = Color(0xFF8FC3AC)
private val Works = Color(0xFF2F6B4F)
private val WorksDark = Color(0xFF7FC49E)
private val Avoid = Color(0xFFA8541E)
private val AvoidDark = Color(0xFFE8A06A)

private val LightColors = lightColorScheme(
    primary = Pine,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E8DE),
    onPrimaryContainer = Color(0xFF12261D),
    secondary = Color(0xFF6B5E4A),
    background = Color(0xFFF7F3EA),   // paper
    onBackground = Color(0xFF241F1A), // ink
    surface = Color(0xFFFFFDF8),
    onSurface = Color(0xFF241F1A),
    surfaceVariant = Color(0xFFEAE3D6),
    onSurfaceVariant = Color(0xFF57503F),
    outline = Color(0xFFBFB5A2),
    error = Avoid,
)

private val DarkColors = darkColorScheme(
    primary = PineLight,
    onPrimary = Color(0xFF0E2419),
    primaryContainer = Color(0xFF234536),
    onPrimaryContainer = Color(0xFFD7E8DE),
    secondary = Color(0xFFCFC2AC),
    background = Color(0xFF15140F),
    onBackground = Color(0xFFEDE7DA),
    surface = Color(0xFF1E1C16),
    onSurface = Color(0xFFEDE7DA),
    surfaceVariant = Color(0xFF33302A),
    onSurfaceVariant = Color(0xFFCAC2B2),
    outline = Color(0xFF6B6455),
    error = AvoidDark,
)

/** The verdict colours, resolved for whichever scheme is up. */
object VerdictColors {
    val works: Color @Composable get() = if (isSystemInDarkTheme()) WorksDark else Works
    val avoid: Color @Composable get() = if (isSystemInDarkTheme()) AvoidDark else Avoid
}

/**
 * The method text is the largest thing on a detail screen on purpose — it is the
 * sentence the whole record exists to carry. Everything else is a label on it.
 */
private val AppTypography = Typography().let { base ->
    base.copy(
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontSize = 18.sp, lineHeight = 27.sp),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Medium),
    )
}

/**
 * Hebrew throughout, so the layout is right-to-left regardless of the phone's
 * locale — a Hebrew app laid out LTR puts every chevron and every swipe on the
 * wrong side.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl, content = content)
    }
}
