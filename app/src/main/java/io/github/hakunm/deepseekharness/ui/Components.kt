package io.github.hakunm.deepseekharness.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hakunm.deepseekharness.R
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette

/**
 * Pure borderless DeepSeek Whale brand mark (NO colored circle badge behind it),
 * tinted DeepSeek Blue (#2563EB) in Porcelain Light mode and Warm Ivory (#E8E5DC) in Night Owl Dark mode.
 */
@Composable
fun WhaleMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    tint: Color = LocalGlassPalette.current.whaleTint,
) {
    Icon(
        painter = painterResource(R.drawable.ic_deepseek_whale),
        contentDescription = null,
        modifier = modifier.size(size),
        tint = tint,
    )
}

@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = 26.dp) {
    WhaleMark(modifier = modifier, size = size)
}

/**
 * Signature asymmetric 2-line menu icon from the approved prototype (top line full, bottom line 58%).
 */
@Composable
fun TwoLineMenuIcon(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {
    Canvas(modifier.size(size)) {
        val stroke = 1.75.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawLine(
            color = tint,
            start = Offset(w * 0.125f, h * 0.33f),
            end = Offset(w * 0.875f, h * 0.33f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.125f, h * 0.64f),
            end = Offset(w * 0.575f, h * 0.64f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

/**
 * Clean 1.6dp stroke outline compose/new-chat icon matching IcNew in the approved prototype.
 */
@Composable
fun NewChatOutlineIcon(
    modifier: Modifier = Modifier,
    size: Dp = 19.dp,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {
    Canvas(modifier.size(size)) {
        val s = this.size.width / 20f
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val boxPath = Path().apply {
            moveTo(10.5f * s, 4.0f * s)
            lineTo(5.6f * s, 4.0f * s)
            quadraticTo(3.8f * s, 4.0f * s, 3.8f * s, 5.8f * s)
            lineTo(3.8f * s, 14.4f * s)
            quadraticTo(3.8f * s, 16.2f * s, 5.6f * s, 16.2f * s)
            lineTo(14.2f * s, 16.2f * s)
            quadraticTo(16.0f * s, 16.2f * s, 16.0f * s, 14.4f * s)
            lineTo(16.0f * s, 9.5f * s)
        }
        drawPath(boxPath, color = tint, style = stroke)
        val penPath = Path().apply {
            moveTo(15.2f * s, 3.4f * s)
            lineTo(9.5f * s, 9.1f * s)
            lineTo(9.5f * s, 11.5f * s)
            lineTo(11.9f * s, 11.5f * s)
            lineTo(17.6f * s, 5.8f * s)
            close()
        }
        drawPath(penPath, color = tint, style = stroke)
    }
}

/**
 * Clean `>_` slash-command terminal icon matching IcSlash in the approved prototype.
 */
@Composable
fun SlashCommandIcon(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Canvas(modifier.size(size)) {
        val s = this.size.width / 16f
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val chevron = Path().apply {
            moveTo(4.3f * s, 5.3f * s)
            lineTo(7.4f * s, 8.0f * s)
            lineTo(4.3f * s, 10.7f * s)
        }
        drawPath(chevron, color = tint, style = stroke)
        drawLine(
            color = tint,
            start = Offset(8.8f * s, 10.8f * s),
            end = Offset(12.2f * s, 10.8f * s),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
    }
}

/**
 * Clean workspace stacked-folder outline icon matching IcWorkspace in the approved prototype.
 */
@Composable
fun WorkspaceStackIcon(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {
    Canvas(modifier.size(size)) {
        val s = this.size.width / 20f
        val sw = 1.55.dp.toPx()
        drawLine(tint, Offset(6f * s, 3.8f * s), Offset(14f * s, 3.8f * s), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(4.5f * s, 6.5f * s), Offset(15.5f * s, 6.5f * s), strokeWidth = sw, cap = StrokeCap.Round)
        drawRoundRect(
            color = tint,
            topLeft = Offset(3.5f * s, 9.0f * s),
            size = Size(13f * s, 7.5f * s),
            cornerRadius = CornerRadius(1.8f * s, 1.8f * s),
            style = Stroke(width = sw),
        )
    }
}

/**
 * Featherlight 46dp borderless top bar shared across all screens.
 */
@Composable
fun MinimalTopBar(
    title: String = "",
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    onNavigationClick: (() -> Unit)? = null,
    useBackIcon: Boolean = false,
    navigationDescription: String = stringResource(if (useBackIcon) R.string.back else R.string.navigation),
    statusDotColor: Color? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .height(46.dp)
            .padding(horizontal = 12.dp),
    ) {
        if (onNavigationClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(
                        role = Role.Button,
                        onClick = onNavigationClick,
                    )
                    .semantics { contentDescription = navigationDescription },
                contentAlignment = Alignment.Center,
            ) {
                if (useBackIcon) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = navigationDescription,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                } else {
                    TwoLineMenuIcon(size = 20.dp, tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        if (title.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (statusDotColor != null) {
                    Box(Modifier.size(6.dp).background(statusDotColor, CircleShape))
                }
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        if (trailing != null) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                content = trailing,
            )
        }
    }
}

@Composable
fun TopBarIconAction(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/**
 * Clean 18dp rounded grouped card (#FFFFFF in Porcelain Light, #232320 in Night Owl Dark).
 */
@Composable
fun GroupedCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    backgroundColor: Color = LocalGlassPalette.current.cardBackground,
    bordered: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalGlassPalette.current
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor, shape)
            .then(
                if (bordered) Modifier.border(1.dp, palette.cardBorder, shape) else Modifier,
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            )
            .padding(contentPadding),
        content = content,
    )
}

/**
 * Solid high-contrast pill CTA button (#0F172A white text in Light, #F2F0EA dark text in Dark).
 */
@Composable
fun PrimaryPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 42.dp,
    backgroundColor: Color = LocalGlassPalette.current.ctaBackground,
    contentColor: Color = LocalGlassPalette.current.ctaForeground,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp),
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.42f)
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(15.dp),
                    strokeWidth = 1.8.dp,
                    color = contentColor,
                )
            }
            content()
        }
    }
}

@Composable
fun SecondaryPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 40.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.42f)
            .clip(CircleShape)
            .background(palette.pillBackground, CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            content()
        }
    }
}

@Composable
fun WarningBanner(message: String, modifier: Modifier = Modifier) {
    val palette = LocalGlassPalette.current
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.warningTint, shape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            modifier = Modifier.size(15.dp).padding(top = 1.dp),
            tint = palette.warningText,
        )
        Text(
            message,
            color = palette.warningText,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun StatusPill(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    dotColor: Color = if (active) LocalGlassPalette.current.successText else MaterialTheme.colorScheme.outline,
) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (active) palette.successTint else palette.pillBackground, CircleShape)
            .padding(horizontal = 9.dp, vertical = 3.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(5.5.dp).background(dotColor, CircleShape))
        Text(
            label,
            color = if (active) palette.successText else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun SmallTagPill(
    text: String,
    backgroundColor: Color = LocalGlassPalette.current.pillBackground,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
            fontSize = 11.sp,
            lineHeight = 14.sp,
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val palette = LocalGlassPalette.current
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(palette.pillBackground, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun SectionPanel(
    @StringRes title: Int,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(title),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.5.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            trailing?.invoke(this)
        }
        GroupedCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(0.dp),
        ) {
            content()
        }
    }
}

/**
 * Monochrome 18dp outline-icon Setting Row matching Prototype Screen 6 (zero rainbow squircle badges!).
 */
@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    value: String = "",
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = false,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else Modifier,
            )
            .padding(horizontal = 16.dp, vertical = 13.5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            tint = MaterialTheme.colorScheme.onBackground,
        )
        Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (value.isNotEmpty()) {
                Text(
                    value,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
        trailing?.invoke(this)
        if (showChevron) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp).size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
