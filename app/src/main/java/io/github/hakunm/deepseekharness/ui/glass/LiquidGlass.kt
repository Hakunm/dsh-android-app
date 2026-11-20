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
import androidx.compose.ui.graphics.BlendMode
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
 * Backdrop capturing the entire crisp scrollable page layer underneath floating Liquid Glass chrome.
 * Provided ONLY to [LiquidGlassStage]'s `overlay` slot so descendant content can NEVER trigger a cycle.
 */
val LocalContentBackdrop = compositionLocalOf<Backdrop?> { null }

/**
 * Clean high-craft canvas stage that captures the full content layer into [LocalContentBackdrop]
 * so floating crystal Liquid Glass bars (dock, headers, composers, toasts) exhibit vivid optical
 * refraction, chromatic dispersion, and specular depth over real scrolling UI content.
 */
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
 * Clean, refined, compact card for structured content lists & grouped panels.
 * Crisp white/dark surface with subtle hairline border ensures maximum scannability and provides
 * vibrant contrast underneath floating crystal Liquid Glass chrome.
 */
@Composable
fun CrispCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 15.dp,
    backgroundColor: Color = LocalGlassPalette.current.cardBackground,
    borderColor: Color = LocalGlassPalette.current.cardBorder,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedRectangle(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && pressed) 0.988f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 550f),
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
            .border(0.6.dp, borderColor, shape)
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
 * True transparent Crystal Liquid Glass floating surface powered by Kyant0/AndroidLiquidGlass.
 * Low surface alpha + crisp light blur + pronounced optical lens refraction + chromatic aberration
 * makes scrolling content beneath unmistakably bend and shimmer like sculpted glass.
 */
@Composable
fun LiquidGlassFloatingBar(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    capsule: Boolean = false,
    backdrop: Backdrop? = LocalContentBackdrop.current,
    surfaceTint: Color = LocalGlassPalette.current.glassCrystalSurface,
    blurRadius: Dp = 6.dp,
    refractionHeight: Dp = 14.dp,
    refractionAmount: Dp = 30.dp,
    chromaticAberration: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
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
                Highlight.Default.copy(alpha = if (dark) 0.48f else 0.94f)
            },
            innerShadow = {
                InnerShadow(radius = 5.dp, alpha = if (dark) 0.14f else 0.06f)
            },
            shadow = {
                Shadow(radius = 22.dp, color = palette.shadowColor)
            },
            onDrawSurface = { drawRect(surfaceTint) },
        )
    } else {
        Modifier
            .background(palette.cardBackground.copy(alpha = 0.92f), fallbackShape)
            .border(0.8.dp, palette.glassSpecBorder, fallbackShape)
    }
    Box(
        modifier = modifier
            .then(glassModifier)
            .padding(contentPadding),
        content = content,
    )
}

/**
 * Compact tactile Liquid Glass capsule button with strong optical refraction and spring press.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backdrop: Backdrop? = LocalContentBackdrop.current,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = LocalGlassPalette.current.glassCrystalSurface,
    contentColor: Color = if (tint != Color.Unspecified) Color.White else MaterialTheme.colorScheme.onSurface,
    height: Dp = 42.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val palette = LocalGlassPalette.current
    val dark = LocalIsDarkGlass.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.955f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 550f),
        label = "glassBtnScale",
    )
    val glassModifier = if (backdrop != null) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { Capsule() },
            effects = {
                vibrancy()
                blur(5.dp.toPx())
                lens(11.dp.toPx(), 24.dp.toPx(), depthEffect = true, chromaticAberration = true)
            },
            highlight = {
                Highlight.Default.copy(alpha = if (tint != Color.Unspecified) 0.65f else if (dark) 0.44f else 0.90f)
            },
            shadow = {
                Shadow(
                    radius = 14.dp,
                    color = if (tint != Color.Unspecified) tint.copy(alpha = 0.28f) else palette.shadowColor,
                )
            },
            onDrawSurface = {
                if (tint != Color.Unspecified) {
                    drawRect(tint, blendMode = BlendMode.Hue)
                    drawRect(tint.copy(alpha = 0.78f))
                } else {
                    drawRect(surfaceColor)
                }
            },
        )
    } else {
        Modifier
            .background(
                if (tint != Color.Unspecified) tint else palette.cardBackground,
                Capsule(),
            )
            .border(
                0.8.dp,
                if (tint != Color.Unspecified) Color.White.copy(alpha = 0.28f) else palette.cardBorder,
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
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

/**
 * Compact circular crystal glass icon button (34dp default for crisp density).
 */
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
    size: Dp = 34.dp,
    iconSize: Dp = 18.dp,
) {
    val palette = LocalGlassPalette.current
    val dark = LocalIsDarkGlass.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 550f),
        label = "glassIconBtnScale",
    )
    val glassModifier = if (backdrop != null) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { Capsule() },
            effects = {
                vibrancy()
                blur(4.dp.toPx())
                lens(8.dp.toPx(), 16.dp.toPx(), depthEffect = true)
            },
            highlight = { Highlight.Default.copy(alpha = if (dark) 0.45f else 0.90f) },
            shadow = { Shadow(radius = 8.dp, color = palette.shadowColor) },
            onDrawSurface = {
                if (tint != Color.Unspecified) drawRect(tint.copy(alpha = 0.84f))
                else drawRect(palette.glassCrystalSurface)
            },
        )
    } else {
        Modifier
            .background(
                if (tint != Color.Unspecified) tint else palette.searchBackground,
                CircleShape,
            )
    }
    Box(
        modifier = modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.42f)
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
 * Compact sleek inline search capsule (36dp height) modeled after top-tier mobile apps.
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
            .background(palette.searchBackground, CircleShape)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (searching) {
            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 1.8.dp)
        } else if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onClear)
                    .semantics { contentDescription = clearContentDescription },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Close, clearContentDescription, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Icon(Icons.Outlined.Search, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Lightweight, ultra-clean text filter tabs with sleek underline pill indicator
 * (replaces bloated double-layered segmented bars on main screens).
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
        modifier = modifier.fillMaxWidth().height(34.dp),
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
                targetValue = if (selected) 18.dp else 0.dp,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
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
                    fontSize = if (selected) 15.sp else 13.5.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                )
                Spacer(Modifier.height(3.dp))
                Box(
                    Modifier
                        .width(indicatorWidth)
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        trailing?.invoke(this)
    }
}

/**
 * Clean compact segmented pill control for Settings & Bottom Sheets (36dp height).
 */
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
            .background(palette.searchBackground, CircleShape)
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / items.size
        val density = LocalDensity.current
        val targetOffsetPx = with(density) { (segmentWidth * selectedIndex.coerceIn(0, items.lastIndex)).toPx() }
        val animatedOffsetPx by animateFloatAsState(
            targetValue = targetOffsetPx,
            animationSpec = spring(dampingRatio = 0.74f, stiffness = Spring.StiffnessMedium),
            label = "segOffset",
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetPx.roundToInt(), 0) }
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(
                    if (dark) Color(0xFF283347) else Color.White,
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
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Compact vibrant squircle icon badge (brings clear visual hierarchy and crisp polish).
 */
@Composable
fun SquircleIconBadge(
    icon: ImageVector,
    startColor: Color,
    endColor: Color = startColor,
    size: Dp = 34.dp,
    iconSize: Dp = 18.dp,
    cornerRadius: Dp = 10.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedRectangle(cornerRadius))
            .background(
                Brush.linearGradient(listOf(startColor, endColor)),
                RoundedRectangle(cornerRadius),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize), tint = Color.White)
    }
}

data class LiquidDockTab(
    val label: String,
    val icon: ImageVector,
)

/**
 * Signature 5-slot True Crystal Apple Liquid Glass Floating Bottom Dock:
 *   [ Tab 0 ]  [ Tab 1 ]   ( + Crystal Pill )   [ Tab 2 ]  [ Tab 3 ]
 *
 * Uses Kyant0/AndroidLiquidGlass with TRUE crystal transparency (low surface alpha + light blur +
 * pronounced optical lens refraction + chromatic aberration) so scrolling cards and colorful icons
 * visibly bend and glow straight through the floating dock!
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
                blur(5.dp.toPx())
                lens(
                    refractionHeight = 16.dp.toPx(),
                    refractionAmount = 36.dp.toPx(),
                    depthEffect = true,
                    chromaticAberration = true,
                )
            },
            highlight = { Highlight.Default.copy(alpha = if (dark) 0.52f else 0.96f) },
            innerShadow = { InnerShadow(radius = 6.dp, alpha = if (dark) 0.16f else 0.06f) },
            shadow = { Shadow(radius = 24.dp, color = palette.shadowColor) },
            onDrawSurface = { drawRect(palette.glassDockSurface) },
        )
    } else {
        Modifier
            .background(palette.cardBackground.copy(alpha = 0.92f), Capsule())
            .border(0.8.dp, palette.glassSpecBorder, Capsule())
    }

    val centerInteraction = remember { MutableInteractionSource() }
    val centerPressed by centerInteraction.collectIsPressedAsState()
    val centerScale by animateFloatAsState(
        targetValue = if (centerActionEnabled && centerPressed) 0.91f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 550f),
        label = "centerPlusScale",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .then(barModifier)
            .padding(horizontal = 5.dp, vertical = 4.dp),
    ) {
        val slotWidth = maxWidth / 5
        val density = LocalDensity.current
        // Map 4 logical tabs (0, 1, 2, 3) to 5 visual slots (0, 1, [center=2], 3, 4)
        val visualSlot = if (selectedIndex < 2) selectedIndex else selectedIndex + 1
        val targetOffsetPx = with(density) { (slotWidth * visualSlot).toPx() }
        val animatedOffsetPx by animateFloatAsState(
            targetValue = targetOffsetPx,
            animationSpec = spring(dampingRatio = 0.72f, stiffness = 460f),
            label = "dockIndicatorOffset",
        )

        // Base 5-column row captured into dockInnerBackdrop
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

        // Gliding Crystal Lens Active Indicator Pill (clean glass lens around active tab)
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
                            refractionHeight = 4.dp.toPx(),
                            refractionAmount = 8.dp.toPx(),
                            depthEffect = false,
                            chromaticAberration = false,
                        )
                    },
                    highlight = { Highlight.Default.copy(alpha = if (dark) 0.45f else 0.88f) },
                    innerShadow = { InnerShadow(radius = 4.dp, alpha = if (dark) 0.14f else 0.05f) },
                    onDrawSurface = { drawRect(palette.glassActivePillTint) },
                ),
        )

        // Center Floating Crystal Glass (+) Pill Button (signature focal accent!)
        val centerTint = palette.accentBlue
        val centerGlassModifier = if (backdrop != null) {
            Modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(4.dp.toPx())
                    lens(11.dp.toPx(), 24.dp.toPx(), depthEffect = true, chromaticAberration = true)
                },
                highlight = { Highlight.Default.copy(alpha = 0.75f) },
                shadow = { Shadow(radius = 12.dp, color = centerTint.copy(alpha = 0.32f)) },
                onDrawSurface = {
                    drawRect(
                        Brush.linearGradient(
                            listOf(
                                centerTint.copy(alpha = 0.84f),
                                palette.accentCyan.copy(alpha = 0.82f),
                            ),
                        ),
                    )
                },
            )
        } else {
            Modifier.background(
                Brush.linearGradient(listOf(centerTint, palette.accentCyan)),
                Capsule(),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(54.dp)
                .height(38.dp)
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
                modifier = Modifier.size(22.dp),
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
    val tint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
        label = "dockTabTint",
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
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(20.dp), tint = tint)
        Text(
            text = tab.label,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}
