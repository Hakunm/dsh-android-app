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

data class GlassPalette(
    val headerBackground: Color,
    val canvasBackground: Color,
    val cardBackground: Color,
    val cardBorder: Color,
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
    headerBackground = Color(0xFFFFFFFF),
    canvasBackground = Color(0xFFF4F5F7),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0x00000000),
    searchBackground = Color(0xFFF3F4F6),
    chipBackground = Color(0xFFFFFFFF),
    chipSelectedBackground = Color(0xFFE6F7EE),
    glassCrystalSurface = Color(0xFFFFFFFF).copy(alpha = 0.52f),
    glassDockSurface = Color(0xFFFFFFFF).copy(alpha = 0.46f),
    glassActivePillTint = Color(0xFFE2E8E5).copy(alpha = 0.72f),
    glassSpecBorder = Color(0xFFFFFFFF).copy(alpha = 0.88f),
    dockInactiveIcon = Color(0xFF1C1F26),
    dockInactiveLabel = Color(0xFF2D313A),
    divider = Color(0xFF111827).copy(alpha = 0.055f),
    userBubbleTint = Color(0xFFE1F7EC),
    userBubbleBorder = Color(0xFFC5ECD8),
    userBubbleText = Color(0xFF0E291C),
    warningTint = Color(0xFFFFF6E4),
    warningText = Color(0xFFB45309),
    successTint = Color(0xFFE6F7EE),
    successText = Color(0xFF0FA958),
    dangerTint = Color(0xFFFEECEC),
    dangerText = Color(0xFFDC2626),
    shadowColor = Color(0xFF0F172A).copy(alpha = 0.08f),
    accentJade = Color(0xFF0FA958),
    accentJadeBright = Color(0xFF22BE70),
    accentBlue = Color(0xFF2B85FF),
    accentCyan = Color(0xFF17B8B0),
    accentEmerald = Color(0xFF0FA958),
    accentAmber = Color(0xFFF5A623),
    accentIndigo = Color(0xFF5B67F1),
    accentCoral = Color(0xFFF2554C),
    quickBlue = Color(0xFF2B99FF),
    quickGold = Color(0xFFFDB827),
    quickCoral = Color(0xFFF2554C),
    quickCobalt = Color(0xFF3388FF),
    quickTeal = Color(0xFF22C3B5),
)

private val DarkGlassPalette = GlassPalette(
    headerBackground = Color(0xFF13161D),
    canvasBackground = Color(0xFF0B0D12),
    cardBackground = Color(0xFF141820),
    cardBorder = Color(0xFF1F2532),
    searchBackground = Color(0xFF1D222D),
    chipBackground = Color(0xFF171B24),
    chipSelectedBackground = Color(0xFF113524),
    glassCrystalSurface = Color(0xFF181D27).copy(alpha = 0.56f),
    glassDockSurface = Color(0xFF171B24).copy(alpha = 0.52f),
    glassActivePillTint = Color(0xFF252D3B).copy(alpha = 0.76f),
    glassSpecBorder = Color(0xFFFFFFFF).copy(alpha = 0.14f),
    dockInactiveIcon = Color(0xFFD8DEE9),
    dockInactiveLabel = Color(0xFF9EA6B5),
    divider = Color(0xFFFFFFFF).copy(alpha = 0.065f),
    userBubbleTint = Color(0xFF133A27),
    userBubbleBorder = Color(0xFF1E573B),
    userBubbleText = Color(0xFFE5FAF0),
    warningTint = Color(0xFF342007),
    warningText = Color(0xFFFBBF24),
    successTint = Color(0xFF0E3222),
    successText = Color(0xFF24C974),
    dangerTint = Color(0xFF391215),
    dangerText = Color(0xFFF87171),
    shadowColor = Color(0xFF000000).copy(alpha = 0.38f),
    accentJade = Color(0xFF22C56E),
    accentJadeBright = Color(0xFF34D37E),
    accentBlue = Color(0xFF3B92FF),
    accentCyan = Color(0xFF26C9BE),
    accentEmerald = Color(0xFF22C56E),
    accentAmber = Color(0xFFFBBF24),
    accentIndigo = Color(0xFF818CF8),
    accentCoral = Color(0xFFF87171),
    quickBlue = Color(0xFF339CFF),
    quickGold = Color(0xFFFBB829),
    quickCoral = Color(0xFFF45D54),
    quickCobalt = Color(0xFF428EFF),
    quickTeal = Color(0xFF24C7B9),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0FA958),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE3F7EC),
    onPrimaryContainer = Color(0xFF064E29),
    secondary = Color(0xFF2B85FF),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6F1FF),
    onSecondaryContainer = Color(0xFF0A3977),
    tertiary = Color(0xFFD97706),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFF6E4),
    onTertiaryContainer = Color(0xFF78350F),
    background = Color(0xFFF4F5F7),
    onBackground = Color(0xFF15181E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF15181E),
    surfaceVariant = Color(0xFFF1F3F6),
    onSurfaceVariant = Color(0xFF6E7582),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F9FB),
    surfaceContainer = Color(0xFFF2F4F7),
    surfaceContainerHigh = Color(0xFFEBEEF2),
    surfaceContainerHighest = Color(0xFFE2E6EC),
    outline = Color(0xFF9AA1AE),
    outlineVariant = Color(0xFFE6E9EF),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEECEC),
    onErrorContainer = Color(0xFF991B1B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF22C56E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF103723),
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = Color(0xFF3B92FF),
    onSecondary = Color(0xFF062958),
    secondaryContainer = Color(0xFF112C52),
    onSecondaryContainer = Color(0xFFD6E9FF),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF3B2200),
    tertiaryContainer = Color(0xFF342007),
    onTertiaryContainer = Color(0xFFFDE68A),
    background = Color(0xFF0B0D12),
    onBackground = Color(0xFFECEFF4),
    surface = Color(0xFF141820),
    onSurface = Color(0xFFECEFF4),
    surfaceVariant = Color(0xFF1D222D),
    onSurfaceVariant = Color(0xFF8F97A6),
    surfaceContainerLowest = Color(0xFF0B0E14),
    surfaceContainerLow = Color(0xFF12161E),
    surfaceContainer = Color(0xFF181D27),
    surfaceContainerHigh = Color(0xFF1F2633),
    surfaceContainerHighest = Color(0xFF273040),
    outline = Color(0xFF666E7D),
    outlineVariant = Color(0xFF222937),
    error = Color(0xFFF87171),
    onError = Color(0xFF3B0909),
    errorContainer = Color(0xFF391215),
    onErrorContainer = Color(0xFFFECACA),
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        letterSpacing = (-0.15).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.5.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.5.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 13.sp,
    ),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(9.dp),
    medium = RoundedCornerShape(11.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp),
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

    // Normalize high-DPI narrow viewports (e.g. 360dp) to a crisp ~428dp flagship density
    // and slightly tighten overall UI scale so text and controls look delicate and refined.
    val config = LocalConfiguration.current
    val baseDensity = LocalDensity.current
    val dpScale = if (config.screenWidthDp in 280..440) {
        (config.screenWidthDp / 430f).coerceIn(0.82f, 0.96f)
    } else {
        0.95f
    }
    val refinedDensity = Density(
        density = baseDensity.density * dpScale,
        fontScale = baseDensity.fontScale * 0.96f,
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
