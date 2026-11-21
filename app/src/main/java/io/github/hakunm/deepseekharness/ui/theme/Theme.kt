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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hakunm.deepseekharness.ThemeMode

val LocalIsDarkGlass = staticCompositionLocalOf { false }

/**
 * Approved Dual-Mode Minimalist Token System:
 * - Light: 方案 B · 清透冰瓷 (Crisp Porcelain, #F6F8FB canvas + #FFFFFF cards + #2563EB DeepSeek Blue)
 * - Dark:  方案 C · 静谧夜枭 (Quiet Night Owl, #171715 velvet graphite + #232320 soft cards + #F2F0EA warm chalk)
 */
data class GlassPalette(
    val headerBackground: Color,
    val canvasBackground: Color,
    val settingsCanvas: Color,
    val sheetBackground: Color,
    val drawerBackground: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val pillBackground: Color,
    val circleButton: Color,
    val composerBackground: Color,
    val composerBorder: Color,
    val ctaBackground: Color,
    val ctaForeground: Color,
    val whaleTint: Color,
    val accentSoft: Color,
    val accentInk: Color,
    val switchOff: Color,
    val searchBackground: Color,
    val chipBackground: Color,
    val chipSelectedBackground: Color,
    val glassCrystalSurface: Color,
    val glassDockSurface: Color,
    val glassActivePillTint: Color,
    val glassSpecBorder: Color,
    val dockInactiveIcon: Color,
    val dockInactiveLabel: Color,
    val divider: Color,
    val userBubbleTint: Color,
    val userBubbleBorder: Color,
    val userBubbleText: Color,
    val warningTint: Color,
    val warningText: Color,
    val successTint: Color,
    val successText: Color,
    val dangerTint: Color,
    val dangerText: Color,
    val shadowColor: Color,
    val accentJade: Color,
    val accentJadeBright: Color,
    val accentBlue: Color,
    val accentCyan: Color,
    val accentEmerald: Color,
    val accentAmber: Color,
    val accentIndigo: Color,
    val accentCoral: Color,
    val quickBlue: Color,
    val quickGold: Color,
    val quickCoral: Color,
    val quickCobalt: Color,
    val quickTeal: Color,
)

val LocalGlassPalette = staticCompositionLocalOf { LightGlassPalette }

private val LightGlassPalette = GlassPalette(
    headerBackground = Color(0xFFF6F8FB),
    canvasBackground = Color(0xFFF6F8FB),
    settingsCanvas = Color(0xFFEEF2F7),
    sheetBackground = Color(0xFFEFF3F8),
    drawerBackground = Color(0xFFF5F8FC),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE2E8F0),
    pillBackground = Color(0xFFE8EEF6),
    circleButton = Color(0xFFEEF2F7),
    composerBackground = Color(0xFFFFFFFF),
    composerBorder = Color(0xFFDFE6F0),
    ctaBackground = Color(0xFF0F172A),
    ctaForeground = Color(0xFFFFFFFF),
    whaleTint = Color(0xFF2563EB),
    accentSoft = Color(0xFFE2ECFF),
    accentInk = Color(0xFF1E40AF),
    switchOff = Color(0xFFCBD5E1),
    searchBackground = Color(0xFFE8EEF6),
    chipBackground = Color(0xFFE8EEF6),
    chipSelectedBackground = Color(0xFFE2ECFF),
    glassCrystalSurface = Color(0xFFFFFFFF).copy(alpha = 0.92f),
    glassDockSurface = Color(0xFFFFFFFF).copy(alpha = 0.92f),
    glassActivePillTint = Color(0xFFE8EEF6),
    glassSpecBorder = Color(0xFFE2E8F0),
    dockInactiveIcon = Color(0xFF475569),
    dockInactiveLabel = Color(0xFF475569),
    divider = Color(0xFFE2E8F0),
    userBubbleTint = Color(0xFFE8EEF6),
    userBubbleBorder = Color.Transparent,
    userBubbleText = Color(0xFF0F172A),
    warningTint = Color(0xFFFEF3C7),
    warningText = Color(0xFFB45309),
    successTint = Color(0xFFE6F6EE),
    successText = Color(0xFF065F46),
    dangerTint = Color(0xFFFEE2E2),
    dangerText = Color(0xFF991B1B),
    shadowColor = Color(0xFF0F172A).copy(alpha = 0.05f),
    accentJade = Color(0xFF2563EB),
    accentJadeBright = Color(0xFF3B82F6),
    accentBlue = Color(0xFF2563EB),
    accentCyan = Color(0xFF0284C7),
    accentEmerald = Color(0xFF059669),
    accentAmber = Color(0xFFD97706),
    accentIndigo = Color(0xFF4F46E5),
    accentCoral = Color(0xFFDC2626),
    quickBlue = Color(0xFF2563EB),
    quickGold = Color(0xFFD97706),
    quickCoral = Color(0xFFDC2626),
    quickCobalt = Color(0xFF1E40AF),
    quickTeal = Color(0xFF059669),
)

private val DarkGlassPalette = GlassPalette(
    headerBackground = Color(0xFF171715),
    canvasBackground = Color(0xFF171715),
    settingsCanvas = Color(0xFF141412),
    sheetBackground = Color(0xFF1E1E1C),
    drawerBackground = Color(0xFF1B1B19),
    cardBackground = Color(0xFF232320),
    cardBorder = Color(0xFF30302C),
    pillBackground = Color(0xFF262623),
    circleButton = Color(0xFF32322E),
    composerBackground = Color(0xFF232320),
    composerBorder = Color(0xFF33332F),
    ctaBackground = Color(0xFFF2F0EA),
    ctaForeground = Color(0xFF141413),
    whaleTint = Color(0xFFE8E5DC),
    accentSoft = Color(0xFF22314C),
    accentInk = Color(0xFFA8CAFF),
    switchOff = Color(0xFF3E3E3A),
    searchBackground = Color(0xFF262623),
    chipBackground = Color(0xFF262623),
    chipSelectedBackground = Color(0xFF22314C),
    glassCrystalSurface = Color(0xFF232320).copy(alpha = 0.94f),
    glassDockSurface = Color(0xFF232320).copy(alpha = 0.94f),
    glassActivePillTint = Color(0xFF2F2F2B),
    glassSpecBorder = Color(0xFF33332F),
    dockInactiveIcon = Color(0xFFB5B1A8),
    dockInactiveLabel = Color(0xFFB5B1A8),
    divider = Color(0xFF2F2F2B),
    userBubbleTint = Color(0xFF262623),
    userBubbleBorder = Color.Transparent,
    userBubbleText = Color(0xFFF2F0EA),
    warningTint = Color(0xFF332410),
    warningText = Color(0xFFFBBF24),
    successTint = Color(0xFF132D22),
    successText = Color(0xFF86EFAC),
    dangerTint = Color(0xFF341B1D),
    dangerText = Color(0xFFFCA5A5),
    shadowColor = Color(0xFF000000).copy(alpha = 0.34f),
    accentJade = Color(0xFF4C8BF5),
    accentJadeBright = Color(0xFF60A5FA),
    accentBlue = Color(0xFF4C8BF5),
    accentCyan = Color(0xFF38BDF8),
    accentEmerald = Color(0xFF34D399),
    accentAmber = Color(0xFFFBBF24),
    accentIndigo = Color(0xFF818CF8),
    accentCoral = Color(0xFFF87171),
    quickBlue = Color(0xFF4C8BF5),
    quickGold = Color(0xFFFBBF24),
    quickCoral = Color(0xFFF87171),
    quickCobalt = Color(0xFFA8CAFF),
    quickTeal = Color(0xFF34D399),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE2ECFF),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = Color(0xFF0F172A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8EEF6),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFFB45309),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),
    background = Color(0xFFF6F8FB),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE8EEF6),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F8FB),
    surfaceContainer = Color(0xFFEEF2F7),
    surfaceContainerHigh = Color(0xFFE8EEF6),
    surfaceContainerHighest = Color(0xFFE2E8F0),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4C8BF5),
    onPrimary = Color(0xFF08142C),
    primaryContainer = Color(0xFF22314C),
    onPrimaryContainer = Color(0xFFA8CAFF),
    secondary = Color(0xFFF2F0EA),
    onSecondary = Color(0xFF141413),
    secondaryContainer = Color(0xFF262623),
    onSecondaryContainer = Color(0xFFF2F0EA),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF332410),
    tertiaryContainer = Color(0xFF332410),
    onTertiaryContainer = Color(0xFFFDE68A),
    background = Color(0xFF171715),
    onBackground = Color(0xFFF2F0EA),
    surface = Color(0xFF232320),
    onSurface = Color(0xFFF2F0EA),
    surfaceVariant = Color(0xFF262623),
    onSurfaceVariant = Color(0xFFB5B1A8),
    surfaceContainerLowest = Color(0xFF141412),
    surfaceContainerLow = Color(0xFF1B1B19),
    surfaceContainer = Color(0xFF1E1E1C),
    surfaceContainerHigh = Color(0xFF262623),
    surfaceContainerHighest = Color(0xFF32322E),
    outline = Color(0xFF78756E),
    outlineVariant = Color(0xFF30302C),
    error = Color(0xFFF87171),
    onError = Color(0xFF341B1D),
    errorContainer = Color(0xFF341B1D),
    onErrorContainer = Color(0xFFFCA5A5),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.5.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.15).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.5.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 19.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 21.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 17.5.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp,
        lineHeight = 16.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
    ),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(18.dp),
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

    // Normalize 360dp viewports (e.g. 1440x2560 @ 640dpi) to the exact 390dp prototype proportion
    // so text, margins, and controls match the approved prototype 1:1 without feeling oversized.
    val config = LocalConfiguration.current
    val baseDensity = LocalDensity.current
    val dpScale = if (config.screenWidthDp in 280..410) {
        (config.screenWidthDp / 390f).coerceIn(0.88f, 1.0f)
    } else {
        1.0f
    }
    val refinedDensity = Density(
        density = baseDensity.density * dpScale,
        fontScale = baseDensity.fontScale,
    )

    CompositionLocalProvider(
        LocalDensity provides refinedDensity,
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
