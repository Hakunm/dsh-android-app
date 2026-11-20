package io.github.hakunm.deepseekharness.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.shapes.RoundedRectangle
import io.github.hakunm.deepseekharness.R
import io.github.hakunm.deepseekharness.ui.glass.CrispCard
import io.github.hakunm.deepseekharness.ui.glass.SquircleIconBadge
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette

@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val palette = LocalGlassPalette.current
    val shape = RoundedRectangle(size * 0.28f)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(listOf(palette.accentBlue, palette.accentCyan)),
                shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(size * 0.72f),
            tint = Color.White,
        )
    }
}

@Composable
fun WarningBanner(message: String, modifier: Modifier = Modifier) {
    val palette = LocalGlassPalette.current
    val shape = RoundedRectangle(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.warningTint, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            modifier = Modifier.size(16.dp).padding(top = 1.dp),
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
            .background(if (active) palette.successTint else palette.searchBackground, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(6.dp).background(dotColor, CircleShape))
        Text(
            label,
            color = if (active) palette.successText else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun SmallTagPill(
    text: String,
    backgroundColor: Color = LocalGlassPalette.current.searchBackground,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.typography.labelSmall,
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
                .size(48.dp)
                .clip(RoundedRectangle(15.dp))
                .background(palette.cardBackground, RoundedRectangle(15.dp))
                .border(0.6.dp, palette.cardBorder, RoundedRectangle(15.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
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
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(title),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
            trailing?.invoke(this)
        }
        CrispCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            contentPadding = PaddingValues(0.dp),
        ) {
            content()
        }
    }
}

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    iconStartColor: Color = LocalGlassPalette.current.accentBlue,
    iconEndColor: Color = iconStartColor,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SquircleIconBadge(
            icon = icon,
            startColor = iconStartColor,
            endColor = iconEndColor,
            size = 32.dp,
            iconSize = 17.dp,
            cornerRadius = 9.dp,
        )
        Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (value.isNotEmpty()) {
                Text(
                    value,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        trailing?.invoke(this)
    }
}
