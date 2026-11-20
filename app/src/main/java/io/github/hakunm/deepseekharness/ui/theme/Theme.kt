package io.github.hakunm.deepseekharness.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hakunm.deepseekharness.ThemeMode

val LocalIsDarkGlass = staticCompositionLocalOf { false }

data class GlassPalette(
    val canvasBackground: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val searchBackground: Color,
    val chipBackground: Color,
    val glassCrystalSurface: Color,
    val glassDockSurface: Color,
    val glassActivePillTint: Color,
    val glassSpecBorder: Color,
    val divider: Color,
    val userBubbleTint: Color,
    val userBubbleText: Color,
    val warningTint: Color,
    val warningText: Color,
    val successTint: Color,
    val successText: Color,
    val dangerTint: Color,
    val dangerText: Color,
    val shadowColor: Color,
    val accentBlue: Color,
    val accentCyan: Color,
    val accentEmerald: Color,
    val accentAmber: Color,
    val accentIndigo: Color,
    val accentCoral: Color,
)

val LocalGlassPalette = staticCompositionLocalOf { LightGlassPalette }

private val LightGlassPalette = GlassPalette(
    canvasBackground = Color(0xFFF3F5F8),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE6EAF0),
    searchBackground = Color(0xFFEAEEF4),
    chipBackground = Color(0xFFFFFFFF),
    glassCrystalSurface = Color(0xFFFFFFFF).copy(alpha = 0.28f),
    glassDockSurface = Color(0xFFFFFFFF).copy(alpha = 0.24f),
    glassActivePillTint = Color(0xFF007AFF).copy(alpha = 0.13f),
    glassSpecBorder = Color(0xFFFFFFFF).copy(alpha = 0.82f),
    divider = Color(0xFF0F172A).copy(alpha = 0.065f),
    userBubbleTint = Color(0xFF007AFF),
    userBubbleText = Color(0xFFFFFFFF),
    warningTint = Color(0xFFFFF6E0),
    warningText = Color(0xFFB45309),
    successTint = Color(0xFFE6F9EF),
    successText = Color(0xFF059669),
    dangerTint = Color(0xFFFEECEC),
    dangerText = Color(0xFFDC2626),
    shadowColor = Color(0xFF0F172A).copy(alpha = 0.10f),
    accentBlue = Color(0xFF007AFF),
    accentCyan = Color(0xFF06B6D4),
    accentEmerald = Color(0xFF10B981),
    accentAmber = Color(0xFFF59E0B),
    accentIndigo = Color(0xFF6366F1),
    accentCoral = Color(0xFFEF4444),
)

private val DarkGlassPalette = GlassPalette(
    canvasBackground = Color(0xFF0A0D14),
    cardBackground = Color(0xFF141923),
    cardBorder = Color(0xFF222A3A),
    searchBackground = Color(0xFF1B2230),
    chipBackground = Color(0xFF181F2C),
    glassCrystalSurface = Color(0xFF161D2B).copy(alpha = 0.34f),
    glassDockSurface = Color(0xFF121824).copy(alpha = 0.34f),
    glassActivePillTint = Color(0xFF38BDF8).copy(alpha = 0.20f),
    glassSpecBorder = Color(0xFFFFFFFF).copy(alpha = 0.16f),
    divider = Color(0xFFFFFFFF).copy(alpha = 0.075f),
    userBubbleTint = Color(0xFF0A84FF),
    userBubbleText = Color(0xFFFFFFFF),
    warningTint = Color(0xFF362107),
    warningText = Color(0xFFFBBF24),
    successTint = Color(0xFF073124),
    successText = Color(0xFF34D399),
    dangerTint = Color(0xFF3B1214),
    dangerText = Color(0xFFF87171),
    shadowColor = Color(0xFF000000).copy(alpha = 0.42f),
    accentBlue = Color(0xFF0A84FF),
    accentCyan = Color(0xFF22D3EE),
    accentEmerald = Color(0xFF34D399),
    accentAmber = Color(0xFFFBBF24),
    accentIndigo = Color(0xFF818CF8),
    accentCoral = Color(0xFFF87171),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF007AFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0EEFF),
    onPrimaryContainer = Color(0xFF00326B),
    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6F9EF),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = Color(0xFFD97706),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFF6E0),
    onTertiaryContainer = Color(0xFF78350F),
    background = Color(0xFFF3F5F8),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFEAEEF4),
    onSurfaceVariant = Color(0xFF64748B),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE8EEF5),
    surfaceContainerHighest = Color(0xFFDCE4EF),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEECEC),
    onErrorContainer = Color(0xFF991B1B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF0F2D54),
    onPrimaryContainer = Color(0xFFD6E9FF),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF042F22),
    secondaryContainer = Color(0xFF073124),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF3B2200),
    tertiaryContainer = Color(0xFF362107),
    onTertiaryContainer = Color(0xFFFDE68A),
    background = Color(0xFF0A0D14),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF141923),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1D2433),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLowest = Color(0xFF0B0F17),
    surfaceContainerLow = Color(0xFF131823),
    surfaceContainer = Color(0xFF19202E),
    surfaceContainerHigh = Color(0xFF20293B),
    surfaceContainerHighest = Color(0xFF283348),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF232C3D),
    error = Color(0xFFF87171),
    onError = Color(0xFF3B0909),
    errorContainer = Color(0xFF3B1214),
    onErrorContainer = Color(0xFFFECACA),
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.25).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp,
        lineHeight = 19.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.5.sp,
        lineHeight = 18.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 21.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 17.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp,
        lineHeight = 13.sp,
    ),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp),
)

@Composable
fun DeepSeekHarnessTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (dark) DarkColors else LightColors
    val glassPalette = if (dark) DarkGlassPalette else LightGlassPalette
    CompositionLocalProvider(
        LocalIsDarkGlass provides dark,
        LocalGlassPalette provides glassPalette,
        LocalContentColor provides colorScheme.onBackground,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
