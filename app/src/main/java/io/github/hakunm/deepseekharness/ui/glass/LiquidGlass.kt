package io.github.hakunm.deepseekharness.ui.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import io.github.hakunm.deepseekharness.ui.theme.LocalGlassPalette
import io.github.hakunm.deepseekharness.ui.theme.LocalIsDarkGlass
import kotlin.math.roundToInt

/**
 * Backdrop capturing the scrollable page layer underneath floating Liquid Glass chrome.
 * Provided ONLY to [LiquidGlassStage]'s `overlay` slot so descendant content can NEVER trigger a cycle.
 */
val LocalContentBackdrop = compositionLocalOf<Backdrop?> { null }

@Composable
fun LiquidGlassStage(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val contentBackdrop: LayerBackdrop = rememberLayerBackdrop()
    val palette = LocalGlassPalette.current
    Box(modifier.fillMaxSize().background(palette.canvasBackground)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(palette.canvasBackground)
                .layerBackdrop(contentBackdrop),
        ) {
            CompositionLocalProvider(LocalContentBackdrop provides null) {
                content()
            }
        }
        CompositionLocalProvider(LocalContentBackdrop provides contentBackdrop) {
            overlay()
        }
    }
}

/**
 * Clean, borderless (in light mode) editorial card matching Coolapk v15 feed surfaces.
 */
@Composable
fun CrispCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    backgroundColor: Color = LocalGlassPalette.current.cardBackground,
    borderColor: Color = LocalGlassPalette.current.cardBorder,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedRectangle(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && pressed) 0.99f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
        label = "crispCardScale",
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(backgroundColor, shape)
            .then(
                if (borderColor.alpha > 0.01f) Modifier.border(0.5.dp, borderColor, shape)
                else Modifier,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else Modifier,
            )
            .padding(contentPadding),
        content = content,
    )
}

/**
 * Refined Frosted Liquid Glass floating bar (smooth blur + clean curved rim refraction, zero rainbow smear).
 */
@Composable
fun LiquidGlassFloatingBar(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    capsule: Boolean = false,
    backdrop: Backdrop? = LocalContentBackdrop.current,
    surfaceTint: Color = LocalGlassPalette.current.glassCrystalSurface,
    blurRadius: Dp = 9.dp,
    refractionHeight: Dp = 11.dp,
    refractionAmount: Dp = 22.dp,
    chromaticAberration: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalGlassPalette.current
    val dark = LocalIsDarkGlass.current
    val fallbackShape = if (capsule) Capsule() else RoundedRectangle(cornerRadius)
    val glassModifier = if (backdrop != null) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { if (capsule) Capsule() else RoundedRectangle(cornerRadius) },
            effects = {
                vibrancy()
                blur(blurRadius.toPx())
                lens(
                    refractionHeight = refractionHeight.toPx(),
                    refractionAmount = refractionAmount.toPx(),
                    depthEffect = true,
                    chromaticAberration = chromaticAberration,
                )
            },
            highlight = {
                Highlight.Default.copy(alpha = if (dark) 0.36f else 0.88f)
            },
            innerShadow = {
                InnerShadow(radius = 4.dp, alpha = if (dark) 0.12f else 0.04f)
            },
            shadow = {
                Shadow(radius = 14.dp, color = palette.shadowColor)
            },
            onDrawSurface = { drawRect(surfaceTint) },
        )
    } else {
        Modifier
            .background(palette.cardBackground.copy(alpha = 0.94f), fallbackShape)
            .border(0.6.dp, palette.glassSpecBorder, fallbackShape)
    }
    Box(
        modifier = modifier
            .then(glassModifier)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backdrop: Backdrop? = LocalContentBackdrop.current,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = LocalGlassPalette.current.glassCrystalSurface,
    contentColor: Color = if (tint != Color.Unspecified) Color.White else MaterialTheme.colorScheme.onSurface,
    height: Dp = 36.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val palette = LocalGlassPalette.current
    val dark = LocalIsDarkGlass.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 600f),
        label = "glassBtnScale",
    )
    val glassModifier = if (backdrop != null) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { Capsule() },
            effects = {
                vibrancy()
                blur(10.dp.toPx())
                lens(7.dp.toPx(), 14.dp.toPx(), depthEffect = true, chromaticAberration = false)
            },
            highlight = {
                Highlight.Default.copy(alpha = if (tint != Color.Unspecified) 0.55f else if (dark) 0.34f else 0.85f)
            },
            shadow = {
                Shadow(
                    radius = 10.dp,
                    color = if (tint != Color.Unspecified) tint.copy(alpha = 0.22f) else palette.shadowColor,
                )
            },
            onDrawSurface = {
                if (tint != Color.Unspecified) {
                    drawRect(tint.copy(alpha = 0.90f))
                } else {
                    drawRect(surfaceColor)
                }
            },
        )
    } else {
        Modifier
            .background(
                if (tint != Color.Unspecified) tint else palette.searchBackground,
                Capsule(),
            )
    }

    Row(
        modifier = modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.45f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(glassModifier)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backdrop: Backdrop? = LocalContentBackdrop.current,
    tint: Color = Color.Unspecified,
    iconTint: Color = if (tint != Color.Unspecified) Color.White else MaterialTheme.colorScheme.onSurface,
    size: Dp = 28.dp,
    iconSize: Dp = 15.5.dp,
) {
    val palette = LocalGlassPalette.current
    val dark = LocalIsDarkGlass.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.91f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "glassIconBtnScale",
    )
    val glassModifier = if (backdrop != null) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { Capsule() },
            effects = {
                vibrancy()
                blur(8.dp.toPx())
                lens(5.dp.toPx(), 10.dp.toPx(), depthEffect = true, chromaticAberration = false)
            },
            highlight = { Highlight.Default.copy(alpha = if (dark) 0.36f else 0.85f) },
            onDrawSurface = {
                if (tint != Color.Unspecified) drawRect(tint.copy(alpha = 0.88f))
                else drawRect(palette.glassCrystalSurface)
            },
        )
    } else {
        Modifier.background(
            if (tint != Color.Unspecified) tint else palette.searchBackground,
            CircleShape,
        )
    }
    Box(
        modifier = modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.40f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(glassModifier)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                if (contentDescription != null) this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, Modifier.size(iconSize), tint = iconTint)
    }
}

/**
 * Sleek 30dp inline search pill inside the seamless white top header (exact reference proportions).
 */
@Composable
fun CompactSearchPill(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    placeholder: String,
    searching: Boolean = false,
    clearContentDescription: String = "Clear",
    modifier: Modifier = Modifier,
) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(palette.pillBackground, CircleShape)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(Icons.Outlined.Search, null, Modifier.size(15.5.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (searching) {
            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 1.6.dp)
        } else if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onClear)
                    .semantics { contentDescription = clearContentDescription },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Close, clearContentDescription, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Reference-style crisp horizontal text tabs with compact emerald underline bar (`头条` style).
 */
@Composable
fun CompactFilterTabs(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        items.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val textColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "filterTabColor",
            )
            val indicatorWidth by animateDpAsState(
                targetValue = if (selected) 15.dp else 0.dp,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 550f),
                label = "filterTabWidth",
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(index) },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = if (selected) 14.sp else 12.5.sp,
                    lineHeight = 16.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .width(indicatorWidth)
                        .height(2.5.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        trailing?.invoke(this)
    }
}

@Composable
fun CompactSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
) {
    if (items.isEmpty()) return
    val palette = LocalGlassPalette.current
    val dark = LocalIsDarkGlass.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(palette.pillBackground, CircleShape)
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / items.size
        val density = LocalDensity.current
        val targetOffsetPx = with(density) { (segmentWidth * selectedIndex.coerceIn(0, items.lastIndex)).toPx() }
        val animatedOffsetPx by animateFloatAsState(
            targetValue = targetOffsetPx,
            animationSpec = spring(dampingRatio = 0.76f, stiffness = Spring.StiffnessMedium),
            label = "segOffset",
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetPx.roundToInt(), 0) }
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(
                    if (dark) Color(0xFF343430) else Color.White,
                    CircleShape,
                ),
        )
        Row(Modifier.fillMaxSize()) {
            items.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "segTextColor",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(index) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Refined soft-tinted or solid rounded icon badge (delicate 26dp–32dp proportions).
 */
@Composable
fun SquircleIconBadge(
    icon: ImageVector,
    startColor: Color,
    endColor: Color = startColor,
    size: Dp = 28.dp,
    iconSize: Dp = 15.dp,
    cornerRadius: Dp = 9.dp,
    softTint: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val dark = LocalIsDarkGlass.current
    val shape = RoundedRectangle(cornerRadius)
    val bgModifier = if (softTint) {
        Modifier.background(startColor.copy(alpha = if (dark) 0.20f else 0.12f), shape)
    } else {
        Modifier.background(Brush.linearGradient(listOf(startColor, endColor)), shape)
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .then(bgModifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = if (softTint) startColor else Color.White,
        )
    }
}

data class LiquidDockTab(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
)

/**
 * Sleek 48dp 5-slot Frosted Liquid Glass Floating Dock modeled directly after Coolapk v15:
 *   [ 首页/会话 ]   [ 工作区 ]   ( + Jade Capsule )   [ 文件 ]   [ 设置 ]
 *
 * Uses smooth frosted blur + clean curved rim lens refraction (NO chromatic rainbow smear),
 * a soft shaded inner capsule for the active tab with emerald icon/text, crisp solid dark icons
 * for inactive tabs, and a compact jade-green center `+` capsule.
 */
@Composable
fun LiquidGlassDock(
    tabs: List<LiquidDockTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onCenterAction: () -> Unit,
    centerActionDescription: String,
    centerActionEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = LocalContentBackdrop.current,
) {
    require(tabs.size == 4) { "LiquidGlassDock expects 4 tabs flanking the center action pill." }
    val palette = LocalGlassPalette.current
    val dark = LocalIsDarkGlass.current
    val dockInnerBackdrop = rememberLayerBackdrop()
    val combinedBackdrop = if (backdrop != null) {
        rememberCombinedBackdrop(backdrop, dockInnerBackdrop)
    } else {
        dockInnerBackdrop
    }

    val barModifier = if (backdrop != null) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { Capsule() },
            effects = {
                vibrancy()
                blur(9.dp.toPx())
                lens(
                    refractionHeight = 11.dp.toPx(),
                    refractionAmount = 22.dp.toPx(),
                    depthEffect = true,
                    chromaticAberration = false,
                )
            },
            highlight = { Highlight.Default.copy(alpha = if (dark) 0.36f else 0.90f) },
            innerShadow = { InnerShadow(radius = 4.dp, alpha = if (dark) 0.12f else 0.04f) },
            shadow = { Shadow(radius = 16.dp, color = palette.shadowColor) },
            onDrawSurface = { drawRect(palette.glassDockSurface) },
        )
    } else {
        Modifier
            .background(palette.cardBackground.copy(alpha = 0.94f), Capsule())
            .border(0.6.dp, palette.glassSpecBorder, Capsule())
    }

    val centerInteraction = remember { MutableInteractionSource() }
    val centerPressed by centerInteraction.collectIsPressedAsState()
    val centerScale by animateFloatAsState(
        targetValue = if (centerActionEnabled && centerPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "centerPlusScale",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(barModifier)
            .padding(horizontal = 4.dp, vertical = 3.dp),
    ) {
        val slotWidth = maxWidth / 5
        val density = LocalDensity.current
        val visualSlot = if (selectedIndex < 2) selectedIndex else selectedIndex + 1
        val targetOffsetPx = with(density) { (slotWidth * visualSlot).toPx() }
        val animatedOffsetPx by animateFloatAsState(
            targetValue = targetOffsetPx,
            animationSpec = spring(dampingRatio = 0.74f, stiffness = 480f),
            label = "dockIndicatorOffset",
        )

        // Soft shaded frosted active pill BEHIND the active tab (exact Coolapk v15 look!)
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetPx.roundToInt(), 0) }
                .width(slotWidth)
                .fillMaxHeight()
                .drawBackdrop(
                    backdrop = combinedBackdrop,
                    shape = { Capsule() },
                    effects = {
                        lens(
                            refractionHeight = 3.dp.toPx(),
                            refractionAmount = 6.dp.toPx(),
                            depthEffect = false,
                            chromaticAberration = false,
                        )
                    },
                    highlight = { Highlight.Default.copy(alpha = if (dark) 0.30f else 0.70f) },
                    onDrawSurface = { drawRect(palette.glassActivePillTint) },
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(dockInnerBackdrop),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DockTabCell(tabs[0], selectedIndex == 0, Modifier.weight(1f)) { onSelect(0) }
            DockTabCell(tabs[1], selectedIndex == 1, Modifier.weight(1f)) { onSelect(1) }
            Spacer(Modifier.weight(1f))
            DockTabCell(tabs[2], selectedIndex == 2, Modifier.weight(1f)) { onSelect(2) }
            DockTabCell(tabs[3], selectedIndex == 3, Modifier.weight(1f)) { onSelect(3) }
        }

        // Compact Jade Green Center (+) Capsule (44dp × 31dp)
        val jadeStart = palette.accentJadeBright
        val jadeEnd = palette.accentJade
        val centerGlassModifier = if (backdrop != null) {
            Modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    blur(6.dp.toPx())
                    lens(6.dp.toPx(), 12.dp.toPx(), depthEffect = true, chromaticAberration = false)
                },
                highlight = { Highlight.Default.copy(alpha = 0.55f) },
                shadow = { Shadow(radius = 8.dp, color = jadeEnd.copy(alpha = 0.28f)) },
                onDrawSurface = {
                    drawRect(
                        Brush.verticalGradient(
                            listOf(
                                jadeStart.copy(alpha = 0.92f),
                                jadeEnd.copy(alpha = 0.94f),
                            ),
                        ),
                    )
                },
            )
        } else {
            Modifier.background(
                Brush.verticalGradient(listOf(jadeStart, jadeEnd)),
                Capsule(),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(44.dp)
                .height(31.dp)
                .alpha(if (centerActionEnabled) 1f else 0.45f)
                .graphicsLayer {
                    scaleX = centerScale
                    scaleY = centerScale
                }
                .then(centerGlassModifier)
                .clickable(
                    enabled = centerActionEnabled,
                    interactionSource = centerInteraction,
                    indication = null,
                    role = Role.Button,
                    onClick = onCenterAction,
                )
                .semantics { contentDescription = centerActionDescription },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = centerActionDescription,
                modifier = Modifier.size(18.dp),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun DockTabCell(
    tab: LiquidDockTab,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    val iconColor by animateColorAsState(
        targetValue = if (selected) palette.accentJade else palette.dockInactiveIcon,
        label = "dockIconTint",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) palette.accentJade else palette.dockInactiveLabel,
        label = "dockLabelTint",
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
    ) {
        Icon(
            if (selected) tab.selectedIcon else tab.icon,
            contentDescription = tab.label,
            modifier = Modifier.size(18.dp),
            tint = iconColor,
        )
        Text(
            text = tab.label,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 9.5.sp,
            lineHeight = 11.5.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
