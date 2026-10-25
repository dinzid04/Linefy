package com.example.ui.components

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.settings.AppSettings
import com.example.ui.components.backdrop.Backdrop
import com.example.ui.components.backdrop.backdrops.LayerBackdrop
import com.example.ui.components.backdrop.backdrops.emptyBackdrop
import com.example.ui.components.backdrop.drawBackdrop
import com.example.ui.components.backdrop.effects.blur
import com.example.ui.components.backdrop.effects.colorControls
import com.example.ui.components.backdrop.effects.lens
import com.example.ui.components.backdrop.highlight.Highlight
import com.example.ui.components.backdrop.highlight.HighlightElement
import com.example.ui.components.backdrop.internal.ShapeProvider
import com.example.ui.components.backdrop.shadow.Shadow

/** Whether the liquid glass nav bar is turned on — see [AppSettings.liquidGlass]. */
val LocalLiquidGlassEnabled = staticCompositionLocalOf { false }

/** The backdrop content (app UI) that a liquid glass surface samples from. */
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop> { emptyBackdrop() }

/**
 * Where a [liquidGlass] surface composed under it also records the glass it
 * draws — the sampled, blurred and tinted backdrop, without its rim or shadow —
 * so a lens above it can refract that surface rather than the page behind it.
 */
internal val LocalGlassExport = compositionLocalOf<LayerBackdrop?> { null }

/**
 * The backdrop blur pipeline requires [android.graphics.RenderEffect] on a
 * [android.graphics.RenderNode], available from Android 12 (API 31).
 */
fun isGlassSupported(sdkInt: Int = Build.VERSION.SDK_INT): Boolean = sdkInt >= Build.VERSION_CODES.S

internal val BAR_GUTTER = 16.dp
internal val PILL_INSET = 6.dp
internal val TAB_VERTICAL_PADDING = 9.dp
internal val TAB_ICON_LABEL_GAP = 2.dp
private val GESTURE_BAR_FLOOR = 15.dp

internal val floatingBarInsets: WindowInsets
    @Composable get() = WindowInsets.navigationBars.union(WindowInsets(bottom = GESTURE_BAR_FLOOR))

internal val GlassSpring = spring<Float>(dampingRatio = 0.72f, stiffness = 320f)
internal const val STRETCH = 0.16f
internal const val SQUASH = 0.5f

private const val VIBRANCY = 1f
private const val BLUR_RADIUS_DP = 8f
private const val LENS_HEIGHT = 0.5f
private const val LENS_AMOUNT = 0.5f
private const val LENS_MAX_DP = 48f
private const val SURFACE_OPACITY = 0.4f
private const val GLASS_RESOLUTION_SCALE = 0.33f

internal val GLASS_EDGE_WIDTH = 0.5.dp
internal val GLASS_EDGE_COLOR = Color.White.copy(alpha = 0.10f)

private const val DARK_BAR_OPACITY = 0.09f
private const val DARK_INDICATOR_OPACITY = 0.14f

@Composable
fun glassContentColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) Color.Black else Color.White

@Composable
fun glassIndicatorColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color.White.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = DARK_INDICATOR_OPACITY)
    }

@Composable
fun glassBarTint(): Color? =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) null else Color.White.copy(alpha = DARK_BAR_OPACITY)

@Composable
fun Modifier.lightweightLiquidGlass(
    shape: CornerBasedShape,
    fallbackColor: Color,
): Modifier {
    val useGlass = LocalLiquidGlassEnabled.current && isGlassSupported()
    val glassTint = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color(0xFFFAFAFA)
    } else {
        Color(0xFF121212)
    }
    val shapeProvider = ShapeProvider { shape }

    return clip(shape)
        .background(
            color = if (useGlass) glassTint.copy(alpha = SURFACE_OPACITY) else fallbackColor,
            shape = shape,
        )
        .then(
            if (useGlass) {
                HighlightElement(
                    shapeProvider = shapeProvider,
                    highlight = { Highlight.Default },
                )
            } else {
                Modifier
            },
        )
        .border(GLASS_EDGE_WIDTH, GLASS_EDGE_COLOR, shape)
}

@Composable
fun Modifier.liquidGlassRim(shape: CornerBasedShape): Modifier {
    if (!LocalLiquidGlassEnabled.current || !isGlassSupported()) return this
    val shapeProvider = remember(shape) { ShapeProvider { shape } }
    return then(
        HighlightElement(
            shapeProvider = shapeProvider,
            highlight = { Highlight.Default },
        ),
    )
}

@Composable
fun Modifier.liquidGlass(
    shape: CornerBasedShape,
    surfaceTint: Color? = null,
): Modifier {
    if (!isGlassSupported()) return this
    val reduceDynamicBlur by AppSettings.reduceBlur.collectAsStateWithLifecycle()
    if (reduceDynamicBlur) {
        return background(MaterialTheme.colorScheme.surface, shape)
            .border(GLASS_EDGE_WIDTH, GLASS_EDGE_COLOR, shape)
    }
    val backdrop = LocalAppBackdrop.current
    val exportedBackdrop = LocalGlassExport.current
    val density = LocalDensity.current
    val blurPx = with(density) { BLUR_RADIUS_DP.dp.toPx() } * GLASS_RESOLUTION_SCALE
    val lensHeightPx = with(density) { (LENS_HEIGHT * LENS_MAX_DP).dp.toPx() } * GLASS_RESOLUTION_SCALE
    val lensAmountPx = with(density) { (LENS_AMOUNT * LENS_MAX_DP).dp.toPx() } * GLASS_RESOLUTION_SCALE
    val themeTint = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color(0xFFFAFAFA)
    } else {
        Color(0xFF121212)
    }
    val surfaceTintColor = surfaceTint ?: themeTint.copy(alpha = SURFACE_OPACITY)

    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            colorControls(saturation = 1f + 0.5f * VIBRANCY)
            blur(blurPx)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                lens(
                    refractionHeight = lensHeightPx,
                    refractionAmount = lensAmountPx,
                    depthEffect = true,
                    chromaticAberration = true,
                )
            }
        },
        highlight = { Highlight.Default },
        shadow = { Shadow.Default },
        onDrawSurface = {
            drawRect(color = surfaceTintColor, size = size)
        },
        exportedBackdrop = exportedBackdrop,
        backdropScale = GLASS_RESOLUTION_SCALE,
    )
}

/**
 * Full Liquid Glass UI card modifier controlled by [AppSettings.fullLiquidGlassUi].
 * - When OFF (`false`, default): renders standard solid card background (`fallbackColor`).
 * - When ON (`true`): renders real frosted liquid glass with translucent glass fill,
 *   diagonal glass sheen, [HighlightElement] edge refraction, and a crisp `1.dp` specular white rim.
 */
@Composable
fun Modifier.lyraGlassCard(
    shape: CornerBasedShape = RoundedCornerShape(16.dp),
    fallbackColor: Color = Color(0xFF161618),
    borderAlpha: Float = 0.24f,
): Modifier {
    val fullGlassEnabled by AppSettings.fullLiquidGlassUi.collectAsStateWithLifecycle()
    if (!fullGlassEnabled) {
        return if (fallbackColor == Color.Transparent) {
            this.clip(shape)
        } else {
            this
                .clip(shape)
                .background(fallbackColor, shape)
        }
    }

    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val topSheenColor = if (isLight) {
        Color.White.copy(alpha = 0.62f)
    } else {
        Color.White.copy(alpha = 0.11f)
    }
    val bottomSheenColor = if (isLight) {
        Color.White.copy(alpha = 0.42f)
    } else {
        Color.White.copy(alpha = 0.05f)
    }
    val rimBrush = remember(isLight, borderAlpha) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = (borderAlpha * 1.25f).coerceIn(0.18f, 0.48f)),
                Color.White.copy(alpha = (borderAlpha * 0.65f).coerceIn(0.10f, 0.26f)),
                Color.White.copy(alpha = (borderAlpha * 0.35f).coerceIn(0.06f, 0.16f)),
            ),
        )
    }
    val shapeProvider = remember(shape) { ShapeProvider { shape } }

    return this
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    topSheenColor,
                    bottomSheenColor,
                ),
            ),
            shape = shape,
        )
        .then(
            if (isGlassSupported()) {
                HighlightElement(
                    shapeProvider = shapeProvider,
                    highlight = { Highlight.Default },
                )
            } else {
                Modifier
            },
        )
        .border(width = 1.dp, brush = rimBrush, shape = shape)
}

/**
 * Warm Gold-Tinted Shimmer Skeleton modifier (`#1E1E22` -> `#38352E` -> `#1E1E22`)
 * that sweeps diagonally across skeleton placeholders while content or artwork loads.
 */
fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(8.dp),
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "lyra_shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "lyra_shimmer_progress",
    )

    this
        .clip(shape)
        .drawWithCache {
            val width = size.width.coerceAtLeast(1f)
            val height = size.height.coerceAtLeast(1f)
            val startX = progress * width
            val shimmerBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E1E22),
                    Color(0xFF2B2823),
                    Color(0xFF38352E),
                    Color(0xFF2B2823),
                    Color(0xFF1E1E22),
                ),
                start = Offset(startX, 0f),
                end = Offset(startX + width * 0.85f, height),
            )
            onDrawBehind {
                drawRect(brush = shimmerBrush)
            }
        }
}
