package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.lyrics.LyricLine as BitChordLyricLine
import com.example.data.model.LyricsData
import com.example.data.model.MusicItem
import com.example.data.settings.AppSettings
import com.example.data.settings.PlayerUiStyle
import com.example.data.settings.TurntableLabelContent
import com.example.player.PlayerUiState
import com.example.ui.home.PlayerSubView
import com.example.ui.theme.PlusJakartaSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// ============================================================================
// CONSTANTS & GEOMETRY FOR TURNTABLE & POSTER STYLES
// ============================================================================

private const val SpinDegreesPerSecond = 26f
private const val RecordDiameterFraction = 1.406f
private const val RecordCenterXFraction = 0.162f
private const val RecordCenterYByWidthFraction = 0.703f
private const val RecordArtLabelFraction = 0.526f

private const val ArmPivotX = 0.895f
private const val ArmPivotY = 0.125f
private const val ArmLength = 0.876f
private const val ArmHeadBend = 27f
private const val ArmHeadLength = 0.145f
private const val ArmStylusAlong = 0.115f
private const val ArmArcRadius = 1.03f
private const val LabelGapDegrees = 3.5f

private const val BaseCenterX = 0.885f
private const val BaseHalfWidth = 0.10f
private const val BaseTop = 0.02f
private const val BaseBottom = 0.40f

private val VinylBlack = Color(0xFF09090A)
private val PlatterBlack = Color(0xFF0B0B0D)
private val PlatterEdge = Color(0xFF2A2830)
private val LabelFallback = Color(0xFF27303B)
private val VinylTrackGaps = floatArrayOf(0.665f, 0.745f, 0.83f, 0.905f)
private val PrintBase = Color.White.copy(alpha = 0.18f)
private val PrintInk = Color.White.copy(alpha = 0.70f)

private val TurntablePillWidth = 59.dp
private val TurntablePillHeight = 42.dp
private val TurntablePillGap = 11.dp
private val TurntableChevronSize = 43.dp
private val TurntableSideMargin = 26.dp
private val TurntablePillFill = Color.White.copy(alpha = 0.11f)
private val ActiveHeartCoralColor = Color(0xFFFF4D67)

private fun rad(degrees: Float) = Math.toRadians(degrees.toDouble()).toFloat()
private fun deg(radians: Float) = Math.toDegrees(radians.toDouble()).toFloat()

private val StylusReach = hypot(
    ArmLength + ArmStylusAlong * cos(rad(ArmHeadBend)),
    ArmStylusAlong * sin(rad(ArmHeadBend))
)
private val StylusDegrees = deg(
    atan2(
        ArmStylusAlong * sin(rad(ArmHeadBend)),
        ArmLength + ArmStylusAlong * cos(rad(ArmHeadBend))
    )
)

private fun tubeDegreesFor(fraction: Float): Float {
    val vx = RecordCenterXFraction - ArmPivotX
    val vy = RecordCenterYByWidthFraction - ArmPivotY
    val v = hypot(vx, vy)
    val r = fraction * (RecordDiameterFraction / 2f)
    val turn = acos(((StylusReach * StylusReach + v * v - r * r) / (2f * StylusReach * v)).coerceIn(-1f, 1f))
    return deg(atan2(vy, vx)) - deg(turn) - StylusDegrees
}

private val StartDegrees = tubeDegreesFor(0.94f)
private val EndDegrees = tubeDegreesFor(RecordArtLabelFraction + 0.075f)

private val AluminiumStops = arrayOf(
    0.00f to Color(0xFF55585C),
    0.10f to Color(0xFF6F7277),
    0.30f to Color(0xFF8B8E93),
    0.52f to Color(0xFFA3A6AA),
    0.66f to Color(0xFFBDC0C3),
    0.76f to Color(0xFFD6D8DB),
    0.84f to Color(0xFFC3C5C8),
    0.93f to Color(0xFF919498),
    1.00f to Color(0xFF606368),
)

private val AnodisedStops = arrayOf(
    0.00f to Color(0xFF09090A),
    0.40f to Color(0xFF161618),
    0.68f to Color(0xFF26272A),
    0.80f to Color(0xFF3A3B3F),
    0.90f to Color(0xFF242528),
    1.00f to Color(0xFF0F0F11),
)

private val FingerLiftStops = arrayOf(
    0.00f to Color(0xFF3C3E42),
    0.55f to Color(0xFF7A7D82),
    0.75f to Color(0xFFA9ACB0),
    1.00f to Color(0xFF55585C),
)

private class TonearmFinish(val faceLit: Color, val faceShade: Color, val side: Color)
private val MatteFinish = TonearmFinish(Color(0xFF2B2C2F), Color(0xFF18191B), Color(0xFF0C0C0E))
private val CapFinish = TonearmFinish(Color(0xFF303135), Color(0xFF1B1C1E), Color(0xFF0D0D0F))
private val ShellFinish = TonearmFinish(Color(0xFF34353A), Color(0xFF1F2023), Color(0xFF111113))

// ============================================================================
// 1. TURNTABLE PLAYER STAGE (1:1 WITH VIDEO Screen_Recording_164934)
// ============================================================================

@Composable
fun TurntablePlayerStage(
    track: MusicItem,
    effectiveCoverUrl: String,
    playerState: PlayerUiState,
    smoothPositionSecState: State<Double>,
    lyricsData: LyricsData?,
    bitChordLyricsLines: List<BitChordLyricLine>,
    isFavorite: Boolean,
    ambientColor: Color,
    onToggleFavorite: () -> Unit,
    onPreviousTrack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenMoreMenu: () -> Unit,
    onOpenStylesSelector: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    isPreview: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val labelContent by AppSettings.turntableLabelContent.collectAsStateWithLifecycle()
    val customName by AppSettings.turntableCustomName.collectAsStateWithLifecycle()
    val realisticMode by AppSettings.turntableRealisticMode.collectAsStateWithLifecycle()
    val rotatingEnabled by AppSettings.turntableRotatingAnimation.collectAsStateWithLifecycle()

    val discName = remember(labelContent, customName, track.title, track.cleanArtist, track.subtitle) {
        when (labelContent) {
            TurntableLabelContent.CUSTOM -> customName
            TurntableLabelContent.MUSIC_NAME -> track.title
            TurntableLabelContent.ARTIST_NAME -> track.cleanArtist
            TurntableLabelContent.ALBUM_NAME -> track.subtitle.ifBlank { track.title }
            TurntableLabelContent.MUSIC_AND_ARTIST ->
                listOf(track.title, track.cleanArtist).filter { it.isNotBlank() }.joinToString(" · ")
            TurntableLabelContent.NONE -> ""
        }
    }

    val spinAngle = remember { mutableFloatStateOf(0f) }
    val lastFrameNanos = remember { mutableLongStateOf(0L) }
    val shouldSpin = rotatingEnabled && playerState.isPlaying && !isPreview

    LaunchedEffect(shouldSpin) {
        if (!shouldSpin) {
            lastFrameNanos.longValue = 0L
            return@LaunchedEffect
        }
        while (isActive) {
            withInfiniteAnimationFrameNanos { frameTimeNanos ->
                val prev = lastFrameNanos.longValue
                lastFrameNanos.longValue = frameTimeNanos
                if (prev != 0L) {
                    val deltaSeconds = (frameTimeNanos - prev) / 1_000_000_000f
                    spinAngle.floatValue = (spinAngle.floatValue + SpinDegreesPerSecond * deltaSeconds).mod(360f)
                }
            }
        }
    }

    var isExpandMenuOpen by remember { mutableStateOf(false) }
    val measurer = rememberTextMeasurer(cacheSize = 64)
    val density = LocalDensity.current
    val context = LocalContext.current

    val deepToneTop = remember(ambientColor) { lerp(ambientColor, Color(0xFF141110), 0.45f) }
    val deepToneMid = remember(ambientColor) { lerp(ambientColor, Color(0xFF0E0C0B), 0.68f) }
    val deepToneBottom = remember(ambientColor) { lerp(ambientColor, Color(0xFF080707), 0.85f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to deepToneTop,
                    0.58f to deepToneMid,
                    1.0f to deepToneBottom
                )
            )
            .then(
                if (!isPreview) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { onOpenStylesSelector() }
                        )
                    }
                } else Modifier
            )
    ) {
        val boxWidthDp = maxWidth
        val discSideDp = boxWidthDp * RecordDiameterFraction
        val discLeftDp = boxWidthDp * RecordCenterXFraction - discSideDp / 2f
        val discTopDp = boxWidthDp * RecordCenterYByWidthFraction - discSideDp / 2f
        val discLeftPx = with(density) { discLeftDp.toPx() }
        val discTopPx = with(density) { discTopDp.toPx() }

        // 1. Tonearm Base Slab (drawn under the record)
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawArmBase(size.width)
                }
        )

        val platterModifier = Modifier
            .wrapContentSize(Alignment.TopStart, unbounded = true)
            .requiredSize(discSideDp)
            .offset { IntOffset(discLeftPx.roundToInt(), discTopPx.roundToInt()) }

        // 2. Soft drop shadow under vinyl platter
        if (realisticMode) {
            Canvas(modifier = platterModifier) {
                val r = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    brush = Brush.radialGradient(
                        0.86f to Color.Black.copy(alpha = 0.62f),
                        1f to Color.Transparent,
                        center = center + Offset(0f, r * 0.035f),
                        radius = r * 1.09f
                    ),
                    radius = r * 1.09f,
                    center = center + Offset(0f, r * 0.035f)
                )
            }
        }

        // 3. Static Vinyl Grooves & Light Sheen Surface
        Canvas(
            modifier = platterModifier.graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
        ) {
            val r = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            if (realisticMode) {
                drawVinylSurface(center, r, RecordArtLabelFraction)
            } else {
                drawCircle(color = PlatterBlack, radius = r, center = center)
                drawCircle(
                    color = PlatterEdge,
                    radius = r - 1.dp.toPx() / 2f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        // 4. Spinning Center Artwork Label + Circular Printed Ring Text
        val labelSideDp = discSideDp * RecordArtLabelFraction
        Box(
            modifier = platterModifier.graphicsLayer {
                rotationZ = spinAngle.floatValue
            }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(labelSideDp)
                    .clip(CircleShape)
                    .background(LabelFallback)
            ) {
                AsyncImage(
                    model = remember(context, effectiveCoverUrl) {
                        ImageRequest.Builder(context)
                            .data(effectiveCoverUrl)
                            .size(900, 900)
                            .allowHardware(true)
                            .crossfade(false)
                            .build()
                    },
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (discName.isNotBlank()) {
                val ringFraction = RecordArtLabelFraction + (1f - RecordArtLabelFraction) * 0.58f
                val band = (1f - RecordArtLabelFraction) / 2f * discSideDp.value
                val fontSize = (band * 0.28f).coerceIn(8f, 14f).sp
                val ringTextStyle = remember(fontSize) {
                    TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = fontSize,
                        letterSpacing = 0.22.em,
                        color = Color.White
                    )
                }
                Canvas(modifier = Modifier.matchParentSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val ringR = size.minDimension / 2f * ringFraction
                    val unit = discName.trim().uppercase() + "  ·  "
                    val glyphs = unit.map { measurer.measure(it.toString(), ringTextStyle) }
                    val unitWidth = glyphs.sumOf { it.size.width.toDouble() }.toFloat().coerceAtLeast(1f)
                    val circumference = (2 * Math.PI * ringR).toFloat()
                    val repeats = (circumference / unitWidth).toInt().coerceAtLeast(1)
                    val stretch = circumference / (unitWidth * repeats)
                    var travelled = 0f
                    repeat(repeats) {
                        for (layout in glyphs) {
                            val w = layout.size.width * stretch
                            val angle = (travelled + w / 2f) / ringR
                            rotate(Math.toDegrees(angle.toDouble()).toFloat(), center) {
                                val at = Offset(
                                    center.x - layout.size.width / 2f,
                                    center.y - ringR - layout.size.height / 2f
                                )
                                drawText(layout, color = PrintBase, topLeft = at)
                                drawText(
                                    layout,
                                    color = PrintInk,
                                    topLeft = at,
                                    blendMode = BlendMode.Overlay
                                )
                            }
                            travelled += w
                        }
                    }
                }
            }
        }

        // 5. Subtle Inner Label Rim Shading Over Spinning Label
        if (realisticMode) {
            Canvas(modifier = platterModifier) {
                val r = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                val labelR = r * RecordArtLabelFraction
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to Color.White.copy(alpha = 0.10f),
                        1f to Color.Transparent,
                        center = center + Offset(-labelR * 0.35f, -labelR * 0.45f),
                        radius = labelR * 1.1f
                    ),
                    radius = labelR,
                    center = center
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        0.82f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.45f),
                        center = center,
                        radius = labelR
                    ),
                    radius = labelR,
                    center = center
                )
            }
        }

        // 6. Interactive Tonearm + Curved Seek Time Arc
        TurntableTonearmOverlay(
            trackId = track.id,
            isPlaying = playerState.isPlaying,
            progressFraction = playerState.progressFraction,
            durationSec = playerState.durationSec,
            interactive = !isPreview,
            onSeekTo = onSeekTo,
            modifier = Modifier.fillMaxSize()
        )

        // 7. Stacked Pills + 3-Dots Button + Title + Badge (No bottom lyric card)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = TurntableSideMargin, vertical = 28.dp)
        ) {
            // Right-aligned vertical stack of 4 pill buttons
            TurntablePillStack(
                isFavorite = isFavorite,
                isPlaying = playerState.isPlaying,
                onToggleFavorite = onToggleFavorite,
                onPreviousTrack = onPreviousTrack,
                onTogglePlayPause = onTogglePlayPause,
                onNextTrack = onNextTrack,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Dots (•••) Button + Song Title + Format Badge + Artist Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(TurntableChevronSize)
                        .clip(CircleShape)
                        .background(
                            if (isExpandMenuOpen) Color.White.copy(alpha = 0.28f)
                            else TurntablePillFill
                        )
                        .clickable(enabled = !isPreview) { isExpandMenuOpen = !isExpandMenuOpen }
                        .testTag("turntable_chevron_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More actions menu",
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !isPreview) { onOpenMoreMenu() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = track.title,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 23.sp,
                            letterSpacing = (-0.5).sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val badgeLabel = remember(playerState.streamFormat) {
                            val fmt = playerState.streamFormat.uppercase()
                            when {
                                fmt.contains("FLAC") -> "FLAC"
                                fmt.contains("OPUS") -> "OPUS"
                                else -> "AAC"
                            }
                        }
                        Box(
                            modifier = Modifier
                                .height(19.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.38f), RoundedCornerShape(5.dp))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badgeLabel,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.5.sp,
                                color = Color.White.copy(alpha = 0.86f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.cleanArtist,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.5.sp,
                        color = Color.White.copy(alpha = 0.60f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 8. Expandable Floating Action Menu when 3-Dots (•••) is tapped
        AnimatedVisibility(
            visible = isExpandMenuOpen && !isPreview,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(160))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.54f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isExpandMenuOpen = false }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(start = TurntableSideMargin, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TurntableQuickMenuItem(
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        label = "Lyrics",
                        isActive = false,
                        onClick = {
                            isExpandMenuOpen = false
                            onOpenLyrics()
                        }
                    )
                    TurntableQuickMenuItem(
                        icon = Icons.Default.Tune,
                        label = "Player Styles",
                        isActive = false,
                        onClick = {
                            isExpandMenuOpen = false
                            onOpenStylesSelector()
                        }
                    )
                    TurntableQuickMenuItem(
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        label = "Queue",
                        isActive = false,
                        onClick = {
                            isExpandMenuOpen = false
                            onOpenQueue()
                        }
                    )
                    TurntableQuickMenuItem(
                        icon = Icons.Default.Shuffle,
                        label = "Shuffle",
                        isActive = playerState.isShuffleEnabled,
                        onClick = onToggleShuffle
                    )
                    TurntableQuickMenuItem(
                        icon = Icons.Default.Repeat,
                        label = "Repeat",
                        isActive = playerState.isRepeatEnabled,
                        onClick = onToggleRepeat
                    )
                    TurntableQuickMenuItem(
                        icon = Icons.Default.MoreHoriz,
                        label = "More options",
                        isActive = false,
                        onClick = {
                            isExpandMenuOpen = false
                            onOpenMoreMenu()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TurntableQuickMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(end = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Color.White.copy(alpha = 0.32f)
                    else Color.White.copy(alpha = 0.16f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
private fun TurntableBottomLyricCard(
    lyricsData: LyricsData?,
    bitChordLyricsLines: List<BitChordLyricLine>,
    smoothPositionSecState: State<Double>,
    durationSec: Int,
    onClick: () -> Unit,
) {
    val posSec = smoothPositionSecState.value.toFloat()
    val posMs = (posSec * 1000f).toLong()

    val activeBitChordLine = remember(bitChordLyricsLines, posMs) {
        if (bitChordLyricsLines.isEmpty()) null
        else {
            bitChordLyricsLines.lastOrNull { it.timeMs <= posMs + 150L }
                ?: bitChordLyricsLines.firstOrNull()
        }
    }

    val activeFallbackLine = remember(lyricsData, posSec, durationSec) {
        val lines = lyricsData?.lines.orEmpty()
        if (lines.isEmpty()) null
        else {
            val idx = ((posSec / durationSec.coerceAtLeast(1).toFloat()) * lines.size)
                .toInt()
                .coerceIn(0, lines.lastIndex)
            lines[idx]
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.09f))
            .border(0.8.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when {
            activeBitChordLine != null -> {
                val words = activeBitChordLine.words
                if (words.isNotEmpty()) {
                    val annotated = buildAnnotatedString {
                        words.forEachIndexed { i, w ->
                            val sung = posMs >= w.startMs
                            withStyle(
                                SpanStyle(
                                    color = if (sung) Color.White else Color.White.copy(alpha = 0.45f),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append(w.text)
                                if (i < words.lastIndex && !w.text.endsWith(" ")) append(" ")
                            }
                        }
                    }
                    Text(
                        text = annotated,
                        fontFamily = PlusJakartaSans,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = activeBitChordLine.text,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            !activeFallbackLine.isNullOrBlank() -> {
                Text(
                    text = activeFallbackLine,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            else -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.55f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Tap to view lyrics & queue",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}

@Composable
fun TurntablePillStack(
    isFavorite: Boolean,
    isPlaying: Boolean,
    onToggleFavorite: () -> Unit,
    onPreviousTrack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(TurntablePillGap),
        modifier = modifier
    ) {
        // 1. Heart / Like Pill
        Box(
            modifier = Modifier
                .size(TurntablePillWidth, TurntablePillHeight)
                .clip(RoundedCornerShape(50))
                .background(TurntablePillFill)
                .clickable(onClick = onToggleFavorite)
                .testTag("style_pill_favorite"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) ActiveHeartCoralColor else Color.White.copy(alpha = 0.88f),
                modifier = Modifier.size(20.dp)
            )
        }

        // 2. Previous Pill
        Box(
            modifier = Modifier
                .size(TurntablePillWidth, TurntablePillHeight)
                .clip(RoundedCornerShape(50))
                .background(TurntablePillFill)
                .clickable(onClick = onPreviousTrack)
                .testTag("style_pill_previous"),
            contentAlignment = Alignment.Center
        ) {
            StyleDoubleTriangleIcon(
                pointLeft = true,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // 3. Play / Pause Pill
        Box(
            modifier = Modifier
                .size(TurntablePillWidth, TurntablePillHeight)
                .clip(RoundedCornerShape(50))
                .background(TurntablePillFill)
                .clickable(onClick = onTogglePlayPause)
                .testTag("style_pill_play_pause"),
            contentAlignment = Alignment.Center
        ) {
            StylePlayPauseGlyph(
                isPlaying = isPlaying,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // 4. Next Pill
        Box(
            modifier = Modifier
                .size(TurntablePillWidth, TurntablePillHeight)
                .clip(RoundedCornerShape(50))
                .background(TurntablePillFill)
                .clickable(onClick = onNextTrack)
                .testTag("style_pill_next"),
            contentAlignment = Alignment.Center
        ) {
            StyleDoubleTriangleIcon(
                pointLeft = false,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun TurntableTonearmOverlay(
    trackId: String,
    isPlaying: Boolean,
    progressFraction: Float,
    durationSec: Int,
    interactive: Boolean,
    onSeekTo: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val measurer = rememberTextMeasurer(cacheSize = 16)
    val labelStyle = remember {
        TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.White
        )
    }

    val durationMs = (durationSec.coerceAtLeast(1) * 1000L)
    val angle = remember { Animatable(StartDegrees) }
    val held = remember { Animatable(0f) }
    val dragging = remember { booleanArrayOf(false) }
    val scrubMs = remember { mutableLongStateOf(-1L) }
    val peek = remember { Animatable(0f) }
    val peekGrow = remember { Animatable(0f) }

    // Peek the curved time arc when track starts or changes
    LaunchedEffect(trackId, isPlaying, interactive) {
        if (!interactive || !isPlaying) return@LaunchedEffect
        if (peek.value < 0.05f) peekGrow.snapTo(0f)
        launch { peek.animateTo(1f, tween(240)) }
        peekGrow.animateTo(1f, tween(760, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)))
        delay(2400L)
        peek.animateTo(0f, tween(420))
    }

    // Update arm angle from playback progress when not dragging
    LaunchedEffect(progressFraction, dragging[0]) {
        if (dragging[0]) return@LaunchedEffect
        val target = StartDegrees + (EndDegrees - StartDegrees) * progressFraction.coerceIn(0f, 1f)
        if (abs(target - angle.value) > 1.5f) {
            angle.animateTo(target, spring(dampingRatio = 0.9f, stiffness = 120f))
        } else if (!angle.isRunning) {
            angle.snapTo(target)
        }
    }

    val currentOnSeekTo by rememberUpdatedState(onSeekTo)
    val currentDurationMs by rememberUpdatedState(durationMs)

    Box(
        modifier = modifier.then(
            if (!interactive) Modifier
            else Modifier.pointerInput(Unit) {
                val grabPx = 28.dp.toPx()
                awaitEachGesture {
                    val w = size.width.toFloat()
                    val pivot = Offset(w * ArmPivotX, w * ArmPivotY)
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (!isTouchOnArm(down.position, pivot, angle.value, w, grabPx)) return@awaitEachGesture
                    down.consume()
                    dragging[0] = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch { angle.stop() }
                    scope.launch { held.animateTo(1f, tween(180)) }

                    val grabbedOffset = angle.value - degreesAroundPivot(down.position, pivot)
                    var lastTick = -1L
                    scrubMs.longValue = msForArmAngle(angle.value, currentDurationMs)

                    drag(down.id) { change ->
                        change.consume()
                        val nextAngle = (degreesAroundPivot(change.position, pivot) + grabbedOffset)
                            .coerceIn(StartDegrees, EndDegrees)
                        scope.launch { angle.snapTo(nextAngle) }
                        val ms = msForArmAngle(nextAngle, currentDurationMs)
                        scrubMs.longValue = ms
                        val tick = ms / 10_000L
                        if (tick != lastTick) {
                            if (lastTick >= 0) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastTick = tick
                        }
                    }

                    val finalFraction = ((angle.value - StartDegrees) / (EndDegrees - StartDegrees)).coerceIn(0f, 1f)
                    currentOnSeekTo(finalFraction)
                    dragging[0] = false
                    scope.launch {
                        delay(450L)
                        held.animateTo(0f, tween(320))
                        if (!dragging[0]) scrubMs.longValue = -1L
                    }
                }
            }
        )
    ) {
        // Tonearm Shadow Layer
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val pivotYRatio = if (size.height > 0f) ArmPivotY * size.width / size.height else 0f
                    transformOrigin = TransformOrigin(ArmPivotX, pivotYRatio)
                    rotationZ = angle.value - StartDegrees
                    val lift = held.value
                    val shift = size.width * (0.012f + 0.024f * lift)
                    translationX = shift * 0.55f
                    translationY = shift * 1.1f
                    alpha = 0.52f - 0.18f * lift
                }
                .drawBehind {
                    val pivot = Offset(size.width * ArmPivotX, size.width * ArmPivotY)
                    drawTonearmGeometry(pivot, StartDegrees, size.width, silhouette = true)
                }
        )

        // Tonearm Main Metallic Layer
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val pivotYRatio = if (size.height > 0f) ArmPivotY * size.width / size.height else 0f
                    transformOrigin = TransformOrigin(ArmPivotX, pivotYRatio)
                    rotationZ = angle.value - StartDegrees
                    val s = 1f + 0.012f * held.value
                    scaleX = s
                    scaleY = s
                }
                .drawBehind {
                    val pivot = Offset(size.width * ArmPivotX, size.width * ArmPivotY)
                    drawTonearmGeometry(pivot, StartDegrees, size.width, silhouette = false)
                }
        )

        // Curved Time Arc around Stylus Path
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val lift = maxOf(held.value, peek.value)
                    if (lift > 0.001f) {
                        val pivot = Offset(size.width * ArmPivotX, size.width * ArmPivotY)
                        drawTonearmTimeArc(
                            pivot = pivot,
                            degrees = angle.value,
                            w = size.width,
                            alpha = lift,
                            grow = if (held.value >= peek.value) 1f else peekGrow.value,
                            positionMs = scrubMs.longValue,
                            durationMs = currentDurationMs,
                            measurer = measurer,
                            labelStyle = labelStyle
                        )
                    }
                }
        )
    }
}

private fun unitVec(degrees: Float): Offset {
    val r = rad(degrees)
    return Offset(cos(r), sin(r))
}

private fun degreesAroundPivot(p: Offset, pivot: Offset): Float =
    deg(atan2(p.y - pivot.y, p.x - pivot.x))

private fun msForArmAngle(degrees: Float, durationMs: Long): Long {
    val f = ((degrees - StartDegrees) / (EndDegrees - StartDegrees)).coerceIn(0f, 1f)
    return (durationMs * f).toLong()
}

private fun isTouchOnArm(p: Offset, pivot: Offset, degrees: Float, w: Float, slop: Float): Boolean {
    val u = unitVec(degrees)
    val joint = pivot + u * (w * ArmLength)
    val head = joint + unitVec(degrees + ArmHeadBend) * (w * ArmHeadLength * 0.5f)
    if (hypot(p.x - head.x, p.y - head.y) < w * ArmHeadLength * 0.5f + slop * 0.6f) return true
    val rel = p - pivot
    val along = rel.x * u.x + rel.y * u.y
    if (along < w * 0.12f || along > w * ArmLength) return false
    return abs(rel.y * u.x - rel.x * u.y) < slop
}

private fun DrawScope.drawTonearmTimeArc(
    pivot: Offset,
    degrees: Float,
    w: Float,
    alpha: Float,
    grow: Float,
    positionMs: Long,
    durationMs: Long,
    measurer: TextMeasurer,
    labelStyle: TextStyle,
) {
    val radius = w * ArmArcRadius
    val stroke = 5.dp.toPx()
    val topLeft = pivot - Offset(radius, radius)
    val arcSize = Size(radius * 2f, radius * 2f)
    val start = StartDegrees + StylusDegrees
    val reach = (EndDegrees - StartDegrees) * grow.coerceIn(0f, 1f)

    drawArc(
        color = Color.White.copy(alpha = 0.32f * alpha),
        startAngle = start,
        sweepAngle = reach.coerceAtLeast(0.01f),
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(stroke, cap = StrokeCap.Round)
    )
    drawArc(
        color = Color.White.copy(alpha = alpha),
        startAngle = start,
        sweepAngle = (degrees - StartDegrees).coerceIn(0.01f, reach.coerceAtLeast(0.01f)),
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(stroke, cap = StrokeCap.Round)
    )

    if (durationMs <= 0L) return
    val shownMs = if (positionMs >= 0L) positionMs else msForArmAngle(degrees, durationMs)
    drawRotatedArcTimeLabel(
        text = formatStyleDuration(shownMs),
        pivot = pivot,
        degrees = start - LabelGapDegrees,
        radius = radius,
        alpha = alpha,
        measurer = measurer,
        style = labelStyle
    )
    val endAlpha = alpha * ((grow - 0.7f) / 0.3f).coerceIn(0f, 1f)
    if (endAlpha > 0.001f) {
        val remainingMs = (durationMs - shownMs).coerceAtLeast(0L)
        drawRotatedArcTimeLabel(
            text = "-${formatStyleDuration(remainingMs)}",
            pivot = pivot,
            degrees = EndDegrees + StylusDegrees + LabelGapDegrees,
            radius = radius,
            alpha = endAlpha,
            measurer = measurer,
            style = labelStyle
        )
    }
}

private fun DrawScope.drawRotatedArcTimeLabel(
    text: String,
    pivot: Offset,
    degrees: Float,
    radius: Float,
    alpha: Float,
    measurer: TextMeasurer,
    style: TextStyle,
) {
    val layout = measurer.measure(text, style.copy(color = style.color.copy(alpha = alpha)))
    val at = pivot + unitVec(degrees) * radius
    rotate(degrees - 90f, at) {
        drawText(layout, topLeft = Offset(at.x - layout.size.width / 2f, at.y - layout.size.height / 2f))
    }
}

// ============================================================================
// VINYL & TONEARM CANVAS RENDERING PRIMITIVES
// ============================================================================

private fun vinylHash(i: Int): Float {
    val s = sin(i * 12.9898f + 78.233f) * 43758.547f
    return s - floor(s)
}

private fun vinylSheenBrush(center: Offset, peak: Float, power: Double, floorVal: Float = 0f): Brush {
    val steps = 180
    val stops = Array(steps + 1) { i ->
        val f = i / steps.toFloat()
        val c = abs(cos(f * 2.0 * Math.PI)).pow(power).toFloat()
        f to Color.White.copy(alpha = floorVal + (peak - floorVal) * c)
    }
    return Brush.sweepGradient(*stops, center = center)
}

private fun vinylGrooveBands(center: Offset, radius: Float, from: Float, to: Float, bandPx: Float): Brush {
    val n = ((to - from) * radius / bandPx).toInt().coerceIn(12, 200)
    val stops = ArrayList<Pair<Float, Color>>(n + 3)
    stops += 0f to Color.Transparent
    for (i in 0..n) {
        val fine = vinylHash(i)
        val slow = 0.5f + 0.5f * sin(i * 0.23f + 1.7f * sin(i * 0.043f))
        val dark = (0.04f + 0.18f * fine * fine + 0.14f * slow).coerceAtMost(0.4f)
        stops += (from + (to - from) * i / n) to Color.Black.copy(alpha = dark)
    }
    stops += 1f to Color.Transparent
    return Brush.radialGradient(*stops.toTypedArray(), center = center, radius = radius)
}

private fun vinylFineGrooves(center: Offset, periodPx: Float): Brush = Brush.radialGradient(
    0f to Color.Black.copy(alpha = 0f),
    0.5f to Color.Black.copy(alpha = 0.14f),
    1f to Color.Black.copy(alpha = 0f),
    center = center,
    radius = periodPx,
    tileMode = TileMode.Repeated
)

private fun DrawScope.drawVinylSurface(center: Offset, radius: Float, label: Float) {
    val r = radius
    val runOut = (label + 0.055f).coerceAtMost(0.93f)
    val rimIn = 0.972f
    drawCircle(VinylBlack, r, center)

    val bandRadius = (runOut + rimIn) / 2f * r
    val band = Stroke(width = (rimIn - runOut) * r)
    drawCircle(vinylSheenBrush(center, peak = 0.20f, power = 5.0, floorVal = 0.012f), bandRadius, center, style = band)
    drawCircle(vinylGrooveBands(center, r, runOut, rimIn, bandPx = 2.4.dp.toPx()), bandRadius, center, style = band)
    drawCircle(vinylFineGrooves(center, max(3f, 1.2.dp.toPx())), bandRadius, center, style = band)

    val gap = max(1.5f, r * 0.0045f)
    for (g in VinylTrackGaps) {
        if (g < runOut + 0.02f || g > rimIn - 0.01f) continue
        drawCircle(Color.Black.copy(alpha = 0.42f), g * r, center, style = Stroke(gap))
        drawCircle(vinylSheenBrush(center, peak = 0.16f, power = 140.0), g * r, center, style = Stroke(gap * 0.55f))
    }
    drawCircle(
        vinylSheenBrush(center, peak = 0.06f, power = 90.0),
        (label + runOut) / 2f * r,
        center,
        style = Stroke((runOut - label) * r)
    )
    drawCircle(Color.Black.copy(alpha = 0.45f), runOut * r, center, style = Stroke(max(1f, r * 0.003f)))

    drawCircle(
        vinylSheenBrush(center, peak = 0.07f, power = 5.0),
        (rimIn + 1f) / 2f * r,
        center,
        style = Stroke((1f - rimIn) * r)
    )
    drawCircle(
        vinylSheenBrush(center, peak = 0.32f, power = 12.0, floorVal = 0.04f),
        r * 0.991f,
        center,
        style = Stroke(max(1f, r * 0.0035f))
    )
    drawCircle(Color.Black.copy(alpha = 0.5f), r - 0.75f, center, style = Stroke(1.5f))
}

private fun DrawScope.drawArmBase(u: Float) {
    val left = (BaseCenterX - BaseHalfWidth) * u
    val top = BaseTop * u
    val width = BaseHalfWidth * 2f * u
    val height = (BaseBottom - BaseTop) * u
    val round = BaseHalfWidth * u
    for (i in 1..5) {
        val grow = u * 0.007f * i
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.075f),
            topLeft = Offset(left - grow + u * 0.008f, top - grow + u * 0.018f),
            size = Size(width + grow * 2f, height + grow * 2f),
            cornerRadius = CornerRadius(round + grow)
        )
    }
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(Color(0xFF1A1B1E), Color(0xFF0F1012), Color(0xFF0A0B0C)),
            start = Offset(left, top),
            end = Offset(left + width, top + height)
        ),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(round)
    )
    val hair = max(1f, u * 0.0016f)
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.11f), Color.White.copy(alpha = 0.025f), Color.Transparent),
            start = Offset(left, top),
            end = Offset(left + width, top + height * 0.6f)
        ),
        topLeft = Offset(left + hair / 2f, top + hair / 2f),
        size = Size(width - hair, height - hair),
        cornerRadius = CornerRadius(round),
        style = Stroke(hair)
    )
}

private fun DrawScope.drawTonearmGeometry(pivot: Offset, degrees: Float, u: Float, silhouette: Boolean) {
    val hair = max(1f, u * 0.0014f)
    withTransform({
        translate(pivot.x, pivot.y)
        rotate(degrees, Offset.Zero)
    }) {
        val tubeHalf = 0.0145f * u
        val tubeFrom = 0.09f * u
        val tubeTo = (ArmLength - 0.05f) * u
        drawArmCylinder(tubeFrom, tubeTo, tubeHalf, AluminiumStops, silhouette)
        if (!silhouette) {
            val ao = 0.022f * u
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.Black.copy(alpha = 0.55f),
                    1f to Color.Transparent,
                    startX = 0.1f * u,
                    endX = 0.1f * u + ao
                ),
                topLeft = Offset(0.1f * u, -tubeHalf),
                size = Size(ao, tubeHalf * 2f)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.5f),
                    startX = tubeTo - ao,
                    endX = tubeTo
                ),
                topLeft = Offset(tubeTo - ao, -tubeHalf),
                size = Size(ao, tubeHalf * 2f)
            )
        }

        drawArmCylinder((ArmLength - 0.055f) * u, (ArmLength + 0.004f) * u, 0.0175f * u, AnodisedStops, silhouette, round = 0.003f * u)
        drawArmCylinder(0.034f * u, 0.1f * u, 0.0188f * u, AnodisedStops, silhouette, round = 0.003f * u)
        if (!silhouette) {
            for (x in floatArrayOf((ArmLength - 0.055f) * u, 0.1f * u)) {
                drawLine(Color.White.copy(alpha = 0.16f), Offset(x, -0.016f * u), Offset(x, 0.017f * u), strokeWidth = hair)
            }
        }

        drawArmBox(-0.052f * u, -0.058f * u, 0.042f * u, 0.088f * u, corner = 0.012f * u, depth = 0.011f * u, MatteFinish, silhouette, hair)
        if (!silhouette) {
            drawLine(Color.Black.copy(alpha = 0.55f), Offset(-0.006f * u, -0.046f * u), Offset(-0.006f * u, 0.086f * u), strokeWidth = hair * 1.4f)
            drawLine(Color.White.copy(alpha = 0.07f), Offset(-0.009f * u, -0.046f * u), Offset(-0.009f * u, 0.086f * u), strokeWidth = hair)
        }
        drawArmBox(-0.1f * u, -0.026f * u, -0.04f * u, 0.058f * u, corner = 0.01f * u, depth = 0.008f * u, CapFinish, silhouette, hair)
        if (!silhouette) {
            for (k in 1..3) {
                val x = (-0.092f + k * 0.012f) * u
                drawLine(Color.Black.copy(alpha = 0.35f), Offset(x, -0.016f * u), Offset(x, 0.054f * u), strokeWidth = hair)
            }
        }

        withTransform({
            translate(ArmLength * u, 0f)
            rotate(ArmHeadBend, Offset.Zero)
        }) {
            drawLine(
                brush = if (silhouette) SolidColor(Color.Black) else Brush.linearGradient(
                    *FingerLiftStops,
                    start = Offset(0.106f * u, 0f),
                    end = Offset(0.132f * u, 0f)
                ),
                start = Offset(0.112f * u, -0.024f * u),
                end = Offset(0.127f * u, -0.067f * u),
                strokeWidth = 0.0105f * u,
                cap = StrokeCap.Round
            )
            drawArmBox(-0.014f * u, -0.032f * u, ArmHeadLength * u, 0.032f * u, corner = 0.009f * u, depth = 0.008f * u, ShellFinish, silhouette, hair)
            if (!silhouette) {
                drawLine(Color.Black.copy(alpha = 0.5f), Offset(0.014f * u, -0.022f * u), Offset(0.014f * u, 0.03f * u), strokeWidth = hair)
                drawLine(Color.Black.copy(alpha = 0.6f), Offset(0.126f * u, 0.008f * u), Offset(0.126f * u, 0.026f * u), strokeWidth = hair * 1.6f)
            }
        }
    }
}

private fun DrawScope.drawArmCylinder(
    from: Float,
    to: Float,
    half: Float,
    stops: Array<Pair<Float, Color>>,
    silhouette: Boolean,
    round: Float = 0f,
) {
    drawRoundRect(
        brush = if (silhouette) SolidColor(Color.Black) else Brush.verticalGradient(*stops, startY = -half, endY = half),
        topLeft = Offset(from, -half),
        size = Size(to - from, half * 2f),
        cornerRadius = CornerRadius(round)
    )
}

private fun DrawScope.drawArmBox(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    corner: Float,
    depth: Float,
    finish: TonearmFinish,
    silhouette: Boolean,
    hair: Float,
) {
    val radius = CornerRadius(corner)
    if (silhouette) {
        drawRoundRect(Color.Black, Offset(left, top), Size(right - left, bottom - top), radius)
        return
    }
    drawRoundRect(finish.side, Offset(left, top), Size(right - left, bottom - top), radius)
    val faceTopLeft = Offset(left, top + depth)
    val faceSize = Size(right - left - depth, bottom - top - depth)
    drawRoundRect(
        brush = Brush.linearGradient(listOf(finish.faceLit, finish.faceShade), start = Offset(left, bottom), end = Offset(right, top)),
        topLeft = faceTopLeft,
        size = faceSize,
        cornerRadius = radius
    )
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.03f), Color.Transparent),
            start = Offset(left, bottom),
            end = Offset(right - depth, top + depth)
        ),
        topLeft = faceTopLeft + Offset(hair / 2f, hair / 2f),
        size = Size(faceSize.width - hair, faceSize.height - hair),
        cornerRadius = radius,
        style = Stroke(hair)
    )
}

// ============================================================================
// 2. POSTER PLAYER STAGE (1:1 WITH VIDEO Screen_Recording_165013 & Screenshot_185654)
// ============================================================================

@Composable
fun PosterPlayerStage(
    track: MusicItem,
    effectiveCoverUrl: String,
    playerState: PlayerUiState,
    smoothPositionSecState: State<Double>,
    lyricsData: LyricsData?,
    bitChordLyricsLines: List<BitChordLyricLine>,
    isFavorite: Boolean,
    ambientColor: Color,
    onToggleFavorite: () -> Unit,
    onPreviousTrack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenMoreMenu: () -> Unit,
    onOpenStylesSelector: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    isPreview: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val visualizerEnabled by AppSettings.posterVisualizerEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalDensity.current
    var isMenuOpen by remember { mutableStateOf(false) }

    val deepTop = remember(ambientColor) { lerp(ambientColor, Color(0xFF1F2224), 0.35f) }
    val deepMid = remember(ambientColor) { lerp(ambientColor, Color(0xFF141618), 0.58f) }
    val deepBot = remember(ambientColor) { lerp(ambientColor, Color(0xFF0A0B0D), 0.82f) }

    val ringExpand = remember { Animatable(0f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to deepTop,
                    0.55f to deepMid,
                    1.0f to deepBot
                )
            )
            .then(
                if (!isPreview) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { onTogglePlayPause() },
                            onLongPress = { onOpenStylesSelector() }
                        )
                    }
                } else Modifier
            )
    ) {
        val w = maxWidth
        val h = maxHeight
        val wPx = with(density) { w.toPx() }
        val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        // Blurred ambient cover background
        AsyncImage(
            model = remember(context, effectiveCoverUrl) {
                ImageRequest.Builder(context)
                    .data(effectiveCoverUrl)
                    .size(256, 256)
                    .allowHardware(true)
                    .build()
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.5f
                    scaleY = 1.5f
                    alpha = 0.55f
                }
                .blur(radius = 60.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.16f),
                        0.55f to deepMid.copy(alpha = 0.72f),
                        1.0f to deepBot.copy(alpha = 0.92f)
                    )
                )
        )

        // 1. Great Circle Cover swung in from top-left (center = 0.13w, 0.06w; radius = 1.14w)
        val circleCenterPx = Offset(0.13f * wPx, 0.06f * wPx)
        val circleRadiusPx = 1.14f * wPx
        val circleShape = remember(circleCenterPx, circleRadiusPx) {
            GenericShape { _, _ ->
                addOval(
                    Rect(
                        circleCenterPx.x - circleRadiusPx,
                        circleCenterPx.y - circleRadiusPx,
                        circleCenterPx.x + circleRadiusPx,
                        circleCenterPx.y + circleRadiusPx
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(w * 1.22f)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.90f to Color.Black.copy(alpha = 0.28f),
                            1.00f to Color.Transparent,
                            center = circleCenterPx + Offset(0f, 10.dp.toPx()),
                            radius = circleRadiusPx * 1.06f
                        ),
                        radius = circleRadiusPx * 1.06f,
                        center = circleCenterPx + Offset(0f, 10.dp.toPx())
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(circleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .drawWithContent {
                        drawContent()
                        drawCircle(
                            brush = Brush.radialGradient(
                                0.82f to Color.Transparent,
                                1.00f to Color.Black.copy(alpha = 0.30f),
                                center = circleCenterPx,
                                radius = circleRadiusPx
                            ),
                            radius = circleRadiusPx,
                            center = circleCenterPx
                        )
                    }
            ) {
                AsyncImage(
                    model = remember(context, effectiveCoverUrl) {
                        ImageRequest.Builder(context)
                            .data(effectiveCoverUrl)
                            .size(1000, 1000)
                            .allowHardware(true)
                            .crossfade(false)
                            .build()
                    },
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 2. Expressive Synced Lyrics / Poster Typography + Bottom Credits
        val bottomPad = navBottom + 32.dp
        val topLyricsStart = (w * 1.24f).coerceAtMost(h * 0.58f) + (if (visualizerEnabled) 32.dp else 0.dp)
        val pushDownPx = with(density) { (ringExpand.value * 28.dp.toPx()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = topLyricsStart,
                    start = 28.dp,
                    end = 22.dp + TurntablePillWidth + 16.dp,
                    bottom = bottomPad
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        translationY = pushDownPx
                    }
                    .clickable(enabled = !isPreview) { onOpenLyrics() }
            ) {
                PosterExpressiveLyricsSection(
                    trackTitle = track.title,
                    bitChordLyricsLines = bitChordLyricsLines,
                    lyricsData = lyricsData,
                    smoothPositionSecState = smoothPositionSecState,
                    durationSec = playerState.durationSec,
                    isPreview = isPreview
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (track.cleanArtist.isNotBlank()) {
                Text(
                    text = track.cleanArtist,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = Color.White.copy(alpha = 0.64f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.5.sp,
                letterSpacing = (-0.4).sp,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 3. Curved Seek Arc & Optional Radial Bar Visualizer along Cover Rim
        PosterCoverRingOverlay(
            trackId = track.id,
            center = circleCenterPx,
            radius = circleRadiusPx,
            isPlaying = playerState.isPlaying,
            progressFraction = playerState.progressFraction,
            durationSec = playerState.durationSec,
            visualizerEnabled = visualizerEnabled,
            interactive = !isPreview,
            ringExpand = ringExpand,
            onSeekTo = onSeekTo,
            modifier = Modifier.fillMaxSize()
        )

        // 4. Bottom-Right Stacked Pills
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = bottomPad)
        ) {
            TurntablePillStack(
                isFavorite = isFavorite,
                isPlaying = playerState.isPlaying,
                onToggleFavorite = onToggleFavorite,
                onPreviousTrack = onPreviousTrack,
                onTogglePlayPause = onTogglePlayPause,
                onNextTrack = onNextTrack
            )
        }

        // 5. Top-Right 3-Dots (•••) Button + Floating Quick Menu
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = statusTop + 16.dp, end = 22.dp)
                .size(TurntableChevronSize)
                .clip(CircleShape)
                .background(
                    if (isMenuOpen) Color.White.copy(alpha = 0.28f)
                    else Color.White.copy(alpha = 0.14f)
                )
                .clickable(enabled = !isPreview) { isMenuOpen = !isMenuOpen }
                .testTag("poster_menu_chevron"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "Poster menu",
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(22.dp)
            )
        }

        AnimatedVisibility(
            visible = isMenuOpen && !isPreview,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(160))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.52f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isMenuOpen = false }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = statusTop + 70.dp, end = 22.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PosterQuickMenuItem(
                        label = "Lyrics",
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        isActive = false,
                        onClick = {
                            isMenuOpen = false
                            onOpenLyrics()
                        }
                    )
                    PosterQuickMenuItem(
                        label = "Shuffle",
                        icon = Icons.Default.Shuffle,
                        isActive = playerState.isShuffleEnabled,
                        onClick = onToggleShuffle
                    )
                    PosterQuickMenuItem(
                        label = "Repeat",
                        icon = Icons.Default.Repeat,
                        isActive = playerState.isRepeatEnabled,
                        onClick = onToggleRepeat
                    )
                    PosterQuickMenuItem(
                        label = "Other menu",
                        icon = Icons.Default.MoreHoriz,
                        isActive = false,
                        onClick = {
                            isMenuOpen = false
                            onOpenMoreMenu()
                        }
                    )
                    PosterQuickMenuItem(
                        label = "Edit player",
                        icon = Icons.Default.Tune,
                        isActive = false,
                        onClick = {
                            isMenuOpen = false
                            onOpenStylesSelector()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterQuickMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(start = 12.dp)
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = Color.White
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Color.White.copy(alpha = 0.32f)
                    else Color.White.copy(alpha = 0.16f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PosterExpressiveLyricsSection(
    trackTitle: String,
    bitChordLyricsLines: List<BitChordLyricLine>,
    lyricsData: LyricsData?,
    smoothPositionSecState: State<Double>,
    durationSec: Int,
    isPreview: Boolean,
) {
    val posSec = smoothPositionSecState.value.toFloat()
    val posMs = (posSec * 1000f).toLong()

    if (isPreview && bitChordLyricsLines.isEmpty() && lyricsData?.lines.isNullOrEmpty()) {
        // Preview state with 3 pulsing interlude dots matching Screenshot_20261026_185654_musicfy.jpg
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.35f))
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.35f))
            )
        }
        return
    }

    if (bitChordLyricsLines.isNotEmpty()) {
        val activeIdx = remember(bitChordLyricsLines, posMs) {
            val idx = bitChordLyricsLines.indexOfLast { it.timeMs <= posMs + 120L }
            idx
        }

        if (activeIdx < 0) {
            // Instrumental intro dots matching Screenshot_20261026_185654_musicfy.jpg
            val firstLineStart = bitChordLyricsLines.first().timeMs.coerceAtLeast(1L)
            val introFrac = (posMs.toFloat() / firstLineStart.toFloat()).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { dotIdx ->
                    val dotLit = ((introFrac * 3f) - dotIdx).coerceIn(0f, 1f)
                    val dotSize = (12f + 4f * dotLit).dp
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.30f + 0.70f * dotLit))
                    )
                }
            }
        } else {
            val currentLine = bitChordLyricsLines[activeIdx]
            val nextLine = bitChordLyricsLines.getOrNull(activeIdx + 1)

            AnimatedContent(
                targetState = activeIdx,
                transitionSpec = {
                    (slideInVertically(tween(340, easing = FastOutSlowInEasing)) { it / 2 } + fadeIn(tween(260))) togetherWith
                        (slideOutVertically(tween(280, easing = FastOutSlowInEasing)) { -it / 2 } + fadeOut(tween(200)))
                },
                label = "poster_expressive_lyric_line"
            ) { idx ->
                val line = bitChordLyricsLines.getOrNull(idx) ?: currentLine
                val upcoming = bitChordLyricsLines.getOrNull(idx + 1) ?: nextLine
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (line.words.isNotEmpty()) {
                        val annotated = buildAnnotatedString {
                            line.words.forEachIndexed { wIdx, w ->
                                val isWordSung = posMs >= w.startMs
                                val isWordActive = posMs in w.startMs..w.endMs
                                val isShortShoutWord = w.text.length in 3..5 && wIdx == 1
                                withStyle(
                                    SpanStyle(
                                        color = if (isWordSung) Color.White else Color.White.copy(alpha = 0.36f),
                                        fontSize = when {
                                            isShortShoutWord -> 40.sp
                                            isWordActive -> 36.sp
                                            else -> 33.sp
                                        },
                                        fontWeight = if (isWordSung) FontWeight.Black else FontWeight.ExtraBold
                                    )
                                ) {
                                    append(w.text)
                                    if (wIdx < line.words.lastIndex && !w.text.endsWith(" ")) append(" ")
                                }
                            }
                        }
                        Text(
                            text = annotated,
                            fontFamily = PlusJakartaSans,
                            lineHeight = 39.sp,
                            letterSpacing = (-0.8).sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        val nextTimeMs = upcoming?.timeMs ?: (line.timeMs + 4000L)
                        val lineSpan = (nextTimeMs - line.timeMs).coerceAtLeast(1200L)
                        val words = remember(line.text) { line.text.split(" ").filter { it.isNotBlank() } }
                        val wordFrac = ((posMs - line.timeMs).toFloat() / lineSpan.toFloat()).coerceIn(0f, 1f)
                        val litWords = (wordFrac * words.size).toInt()
                        val annotated = buildAnnotatedString {
                            words.forEachIndexed { wIdx, w ->
                                val sung = wIdx <= litWords
                                val shout = w.length in 3..5 && wIdx == 1 && words.size > 2
                                withStyle(
                                    SpanStyle(
                                        color = if (sung) Color.White else Color.White.copy(alpha = 0.36f),
                                        fontSize = if (shout) 40.sp else 33.sp,
                                        fontWeight = if (sung) FontWeight.Black else FontWeight.ExtraBold
                                    )
                                ) {
                                    append(w)
                                    if (wIdx < words.lastIndex) append(" ")
                                }
                            }
                        }
                        Text(
                            text = annotated,
                            fontFamily = PlusJakartaSans,
                            lineHeight = 39.sp,
                            letterSpacing = (-0.8).sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (upcoming != null && upcoming.text.isNotBlank()) {
                        Text(
                            text = upcoming.text,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            lineHeight = 29.sp,
                            letterSpacing = (-0.5).sp,
                            color = Color.White.copy(alpha = 0.22f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        return
    }

    val fallbackLines = lyricsData?.lines.orEmpty()
    if (fallbackLines.isNotEmpty()) {
        val activeIdx = ((posSec / durationSec.coerceAtLeast(1).toFloat()) * fallbackLines.size)
            .toInt()
            .coerceIn(0, fallbackLines.lastIndex)
        val currentText = fallbackLines[activeIdx]
        val nextText = fallbackLines.getOrNull(activeIdx + 1)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = currentText,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Black,
                fontSize = 33.sp,
                lineHeight = 38.sp,
                letterSpacing = (-0.8).sp,
                color = Color.White,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            if (!nextText.isNullOrBlank()) {
                Text(
                    text = nextText,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 23.sp,
                    lineHeight = 28.sp,
                    color = Color.White.copy(alpha = 0.24f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else {
        // Expressive multi-scale song title when no lyrics exist
        Text(
            text = trackTitle,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Black,
            fontSize = 36.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.9).sp,
            color = Color.White.copy(alpha = 0.90f),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PosterCoverRingOverlay(
    trackId: String,
    center: Offset,
    radius: Float,
    isPlaying: Boolean,
    progressFraction: Float,
    durationSec: Int,
    visualizerEnabled: Boolean,
    interactive: Boolean,
    ringExpand: Animatable<Float, *>,
    onSeekTo: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val measurer = rememberTextMeasurer(cacheSize = 24)
    val labelStyle = remember {
        TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.White
        )
    }

    var scrubFraction by remember { mutableFloatStateOf(-1f) }
    val durationMs = (durationSec.coerceAtLeast(1) * 1000L)
    val currentOnSeekTo by rememberUpdatedState(onSeekTo)

    // Auto-peek the curved seek bar when playback starts or track changes (matching Video 165013)
    LaunchedEffect(trackId, isPlaying, interactive) {
        if (!interactive || !isPlaying) return@LaunchedEffect
        ringExpand.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 340f))
        delay(2800L)
        if (scrubFraction < 0f) {
            ringExpand.animateTo(0f, spring(dampingRatio = 0.9f, stiffness = 170f))
        }
    }

    // Animated visualizer phase when visualizer is enabled
    val infiniteVis = rememberInfiniteTransition(label = "poster_ring_vis")
    val visPhase by infiniteVis.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "poster_vis_phase"
    )

    Spacer(
        modifier = modifier
            .then(
                if (!interactive) Modifier
                else Modifier.pointerInput(center, radius) {
                    val slop = viewConfiguration.touchSlop
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = true, pass = PointerEventPass.Main)
                        val d = hypot(down.position.x - center.x, down.position.y - center.y)
                        val dpPx = density
                        if (d < radius - 46f * dpPx || d > radius + 54f * dpPx) return@awaitEachGesture

                        scope.launch {
                            ringExpand.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 340f))
                        }

                        var travel = Offset.Zero
                        var scrubbing = false
                        var lastTick = -1L

                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            if (!scrubbing) {
                                travel += change.positionChange()
                                if (travel.getDistance() < slop) continue
                                scrubbing = true
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            change.consume()

                            val barR = radius + 22f * dpPx
                            val startAngle = posterAngleAtX(center, barR, 64f * dpPx)
                            val endAngle = posterAngleAtX(center, barR, size.width - 52f * dpPx)
                            val touchAngle = deg(
                                atan2(
                                    change.position.y - center.y,
                                    change.position.x - center.x
                                )
                            )
                            val frac = ((touchAngle - startAngle) / (endAngle - startAngle)).coerceIn(0f, 1f)
                            scrubFraction = frac
                            val tick = ((frac * durationMs).toLong()) / 10_000L
                            if (tick != lastTick) {
                                if (lastTick >= 0) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastTick = tick
                            }
                        }

                        if (scrubbing && scrubFraction >= 0f) {
                            currentOnSeekTo(scrubFraction.coerceIn(0f, 1f))
                        }
                        scope.launch {
                            delay(1400L)
                            ringExpand.animateTo(0f, spring(dampingRatio = 0.9f, stiffness = 170f))
                            scrubFraction = -1f
                        }
                    }
                }
            )
            .drawBehind {
                val e = ringExpand.value
                val dpPx = density
                val barR = radius + (-15f + 37f * e) * dpPx
                val startX = (26f + 38f * e) * dpPx
                val endX = size.width - (26f + 26f * e) * dpPx
                val startAngle = posterAngleAtX(center, barR, startX)
                val endAngle = posterAngleAtX(center, barR, endX)
                val sweep = endAngle - startAngle

                // Optional Radial Visualizer Bars along the cover rim
                if (visualizerEnabled) {
                    val visStart = posterAngleAtX(center, radius, 14f * dpPx)
                    val visEnd = posterAngleAtX(center, radius, size.width - 10f * dpPx)
                    val count = 26
                    val thickness = 5.dp.toPx()
                    val gap = (13f + 34f * e).dp.toPx()
                    val minLen = 4.dp.toPx()
                    val maxLen = 44.dp.toPx()
                    val barColor = Color.White.copy(alpha = 0.52f * (1f - 0.45f * e))

                    for (i in 0 until count) {
                        val t = i / (count - 1f)
                        val degAngle = visStart + (visEnd - visStart) * t
                        val radAngle = rad(degAngle)
                        val dir = Offset(cos(radAngle), sin(radAngle))
                        val level = if (isPlaying && interactive) {
                            (0.38f + 0.34f * sin(i * 0.75f + visPhase) * cos(i * 0.42f - visPhase * 0.7f)).coerceIn(0.08f, 1f)
                        } else {
                            (0.25f + 0.20f * sin(i * 0.9f) * sin(i * 0.37f + 1f)).coerceIn(0.06f, 0.6f)
                        }
                        val taper = 0.65f + 0.35f * (t.coerceAtMost(0.3f) / 0.3f)
                        val len = minLen + (maxLen - minLen) * level * taper
                        val inner = center + dir * (radius + gap)
                        drawLine(barColor, inner, inner + dir * len, strokeWidth = thickness, cap = StrokeCap.Round)
                    }
                }

                // Curved Seek Arc
                val stroke = (3f + 4f * e).dp.toPx()
                val topLeft = center - Offset(barR, barR)
                val arcSize = Size(barR * 2f, barR * 2f)
                val ink = 0.32f + 0.68f * e

                drawArc(
                    color = Color.White.copy(alpha = 0.38f * ink),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )

                val activeFrac = if (scrubFraction >= 0f) scrubFraction else progressFraction.coerceIn(0f, 1f)
                val playedSweep = sweep * activeFrac
                drawArc(
                    color = Color.White.copy(alpha = 0.96f * ink),
                    startAngle = startAngle,
                    sweepAngle = if (abs(playedSweep) < 0.01f) 0.01f * kotlin.math.sign(sweep) else playedSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )

                val shown = ((e - 0.3f) / 0.7f).coerceIn(0f, 1f)
                if (shown > 0.001f && durationMs > 0L) {
                    val toward = kotlin.math.sign(sweep)
                    val posMs = (activeFrac * durationMs).toLong()
                    drawPosterRingLabel(
                        text = formatStyleDuration(posMs),
                        center = center,
                        endDegrees = startAngle,
                        r = barR,
                        outward = -toward,
                        shown = shown,
                        measurer = measurer,
                        style = labelStyle
                    )
                    drawPosterRingLabel(
                        text = formatStyleDuration(durationMs),
                        center = center,
                        endDegrees = endAngle,
                        r = barR,
                        outward = toward,
                        shown = shown,
                        measurer = measurer,
                        style = labelStyle
                    )
                }
            }
    )
}

private fun posterAngleAtX(center: Offset, r: Float, x: Float): Float {
    val dx = (x - center.x).coerceIn(-r * 0.999f, r * 0.999f)
    return deg(atan2(sqrt(r * r - dx * dx), dx))
}

private fun DrawScope.drawPosterRingLabel(
    text: String,
    center: Offset,
    endDegrees: Float,
    r: Float,
    outward: Float,
    shown: Float,
    measurer: TextMeasurer,
    style: TextStyle,
) {
    val layout = measurer.measure(text, style.copy(color = style.color.copy(alpha = shown)))
    val gapPx = (layout.size.width / 2f) + (8f + 10f * (1f - shown)).dp.toPx()
    val gapDegrees = deg(gapPx / r)
    val degrees = endDegrees + outward * gapDegrees
    val radAngle = rad(degrees)
    val at = center + Offset(cos(radAngle), sin(radAngle)) * r
    val s = 0.86f + 0.14f * shown
    var turn = degrees - 90f
    while (turn > 90f) turn -= 180f
    while (turn < -90f) turn += 180f
    rotate(turn, at) {
        scale(s, s, at) {
            drawText(layout, topLeft = Offset(at.x - layout.size.width / 2f, at.y - layout.size.height / 2f))
        }
    }
}

// ============================================================================
// 3. MINIMAL (TIMELINE) PLAYER STAGE
// ============================================================================

@Composable
fun MinimalPlayerStage(
    track: MusicItem,
    effectiveCoverUrl: String,
    playerState: PlayerUiState,
    isFavorite: Boolean,
    ambientColor: Color,
    onToggleFavorite: () -> Unit,
    onPreviousTrack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenMoreMenu: () -> Unit,
    onOpenStylesSelector: () -> Unit,
    isPreview: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Desaturated two-tone palette from artwork seed matching Timeline style
    val (playedTone, remainingTone) = remember(ambientColor) {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(ambientColor.toArgb(), hsl)
        val sat = (hsl[1] * 0.42f).coerceIn(0.06f, 0.24f)
        val played = Color(ColorUtils.HSLToColor(floatArrayOf(hsl[0], sat, 0.43f)))
        val remaining = Color(ColorUtils.HSLToColor(floatArrayOf(hsl[0], sat, 0.29f)))
        played to remaining
    }

    val durationMs = (playerState.durationSec.coerceAtLeast(1) * 1000L)
    val frac = if (isPreview) 0.18f else playerState.progressFraction.coerceIn(0f, 1f)
    val elapsedMs = (frac * durationMs).toLong()
    val elapsedText = formatStyleDuration(elapsedMs)
    val totalText = formatStyleDuration(durationMs)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (!isPreview) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { onTogglePlayPause() },
                            onLongPress = { onOpenStylesSelector() }
                        )
                    }
                } else Modifier
            )
    ) {
        val hPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val topReservePx = with(density) { (statusTop + 104.dp).toPx() }
        val botReservePx = with(density) { (navBottom + 110.dp).toPx() }
        val minLineY = topReservePx + with(density) { 64.dp.toPx() }
        val maxLineY = (hPx - botReservePx - with(density) { 64.dp.toPx() }).coerceAtLeast(minLineY)
        val splitY = minLineY + (maxLineY - minLineY) * frac

        // Two-tone vertical playhead split background
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isPreview) {
                        Modifier.pointerInput(minLineY, maxLineY) {
                            detectTapGestures(
                                onLongPress = { onOpenStylesSelector() },
                                onTap = { offset ->
                                    if (offset.y in minLineY..maxLineY) {
                                        val seekFrac = ((offset.y - minLineY) / (maxLineY - minLineY)).coerceIn(0f, 1f)
                                        onSeekTo(seekFrac)
                                    }
                                }
                            )
                        }
                    } else Modifier
                )
        ) {
            val y = splitY.coerceIn(0f, size.height)
            drawRect(color = playedTone, size = Size(size.width, y))
            drawRect(color = remainingTone, topLeft = Offset(0f, y), size = Size(size.width, size.height - y))
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                    endY = y.coerceAtLeast(1f)
                ),
                size = Size(size.width, y)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.18f), Color.Transparent),
                    startY = y,
                    endY = y + size.height * 0.18f
                ),
                topLeft = Offset(0f, y),
                size = Size(size.width, size.height * 0.18f)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.14f),
                topLeft = Offset(0f, y - 1f),
                size = Size(size.width, 1.5f)
            )
        }

        // Top Header: Compact Cover Art + Song Title + Artist + More Menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusTop + 20.dp, start = 22.dp, end = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = remember(context, effectiveCoverUrl) {
                    ImageRequest.Builder(context)
                        .data(effectiveCoverUrl)
                        .size(240, 240)
                        .allowHardware(true)
                        .build()
                },
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.5.sp,
                    color = Color(0xFFF1ECEC),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.cleanArtist,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = Color(0xFFF1ECEC).copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!isPreview) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable { onOpenMoreMenu() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Riding Giant Timestamps above and below the split line
        val splitYDp = with(density) { splitY.toDp() }
        val bigTimeStyle = remember {
            TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Black,
                fontSize = 72.sp,
                lineHeight = 70.sp,
                letterSpacing = (-2.5).sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both)
            )
        }

        Text(
            text = elapsedText,
            color = Color(0xFFF1ECEC),
            style = bigTimeStyle,
            maxLines = 1,
            modifier = Modifier
                .padding(start = 24.dp)
                .offset(y = (splitYDp - 74.dp).coerceAtLeast(statusTop + 82.dp))
        )

        Text(
            text = totalText,
            color = Color(0xFFF1ECEC).copy(alpha = 0.78f),
            style = bigTimeStyle,
            maxLines = 1,
            modifier = Modifier
                .padding(start = 24.dp)
                .offset(y = splitYDp + 8.dp)
        )

        // Bottom Circular Transport Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navBottom + 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MinimalCircleButton(
                onClick = onToggleFavorite,
                enabled = !isPreview
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isFavorite) ActiveHeartCoralColor else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            MinimalCircleButton(
                onClick = onPreviousTrack,
                enabled = !isPreview
            ) {
                StyleDoubleTriangleIcon(pointLeft = true, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            MinimalCircleButton(
                onClick = onTogglePlayPause,
                size = 46.dp,
                enabled = !isPreview
            ) {
                StylePlayPauseGlyph(isPlaying = playerState.isPlaying, tint = Color.White, modifier = Modifier.size(19.dp))
            }
            MinimalCircleButton(
                onClick = onNextTrack,
                enabled = !isPreview
            ) {
                StyleDoubleTriangleIcon(pointLeft = false, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            MinimalCircleButton(
                onClick = onOpenLyrics,
                enabled = !isPreview
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "Lyrics & Queue",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MinimalCircleButton(
    onClick: () -> Unit,
    size: Dp = 38.dp,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.22f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ============================================================================
// 4. "STYLES" SELECTOR OVERLAY & CUSTOMIZATION SHEETS
//    (1:1 WITH Screenshot_20261026_185649 & Screenshot_20261026_185654)
// ============================================================================

@Composable
fun PlayerStyleSelectorOverlay(
    visible: Boolean,
    currentStyle: PlayerUiStyle,
    track: MusicItem,
    effectiveCoverUrl: String,
    playerState: PlayerUiState,
    smoothPositionSecState: State<Double>,
    lyricsData: LyricsData?,
    bitChordLyricsLines: List<BitChordLyricLine>,
    isFavorite: Boolean,
    ambientColor: Color,
    onSelectStyle: (PlayerUiStyle) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var showTurntableEditSheet by remember { mutableStateOf(false) }
    var showPosterEditSheet by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showTurntableEditSheet -> showTurntableEditSheet = false
            showPosterEditSheet -> showPosterEditSheet = false
            else -> onDismiss()
        }
    }

    val styles = remember { PlayerUiStyle.entries.toList() }
    val initialPage = remember(currentStyle) { styles.indexOf(currentStyle).coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { styles.size })
    val focusedStyle = styles.getOrElse(pagerState.currentPage) { PlayerUiStyle.DEFAULT }

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("player_styles_selector_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusTop + 10.dp, bottom = navBottom + 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header: Back button on left, "Styles" + Current Style name centered
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .height(42.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable(onClick = onDismiss)
                        .testTag("styles_selector_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .size(14.dp)
                            .offset(x = 2.dp)
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Styles",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.4).sp,
                        color = Color.White
                    )
                    AnimatedContent(
                        targetState = focusedStyle.selectorName,
                        transitionSpec = {
                            (slideInVertically(tween(280)) { it } + fadeIn(tween(220))) togetherWith
                                (slideOutVertically(tween(240)) { -it } + fadeOut(tween(160)))
                        },
                        label = "styles_selector_subtitle"
                    ) { name ->
                        Text(
                            text = name,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Horizontal Pager of Scaled Player Preview Cards (with peeking neighbor cards)
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 44.dp),
                pageSpacing = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val style = styles[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(34.dp))
                        .background(Color(0xFF141416))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(34.dp))
                        .clickable {
                            onSelectStyle(style)
                            onDismiss()
                        }
                        .testTag("style_preview_card_${style.name.lowercase()}")
                ) {
                    // Render scaled live preview inside the card
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val scaleFactor = 0.72f
                        val virtualW = maxWidth / scaleFactor
                        val virtualH = maxHeight / scaleFactor
                        Box(
                            modifier = Modifier
                                .requiredSize(virtualW, virtualH)
                                .graphicsLayer {
                                    scaleX = scaleFactor
                                    scaleY = scaleFactor
                                }
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                                        }
                                    }
                                }
                        ) {
                            when (style) {
                                PlayerUiStyle.DEFAULT -> {
                                    DefaultStylePreviewCard(
                                        track = track,
                                        effectiveCoverUrl = effectiveCoverUrl,
                                        ambientColor = ambientColor,
                                        isPlaying = playerState.isPlaying,
                                        progressFraction = playerState.progressFraction,
                                        durationSec = playerState.durationSec
                                    )
                                }
                                PlayerUiStyle.TURNTABLE -> {
                                    TurntablePlayerStage(
                                        track = track,
                                        effectiveCoverUrl = effectiveCoverUrl,
                                        playerState = playerState,
                                        smoothPositionSecState = smoothPositionSecState,
                                        lyricsData = lyricsData,
                                        bitChordLyricsLines = bitChordLyricsLines,
                                        isFavorite = isFavorite,
                                        ambientColor = ambientColor,
                                        onToggleFavorite = {},
                                        onPreviousTrack = {},
                                        onTogglePlayPause = {},
                                        onNextTrack = {},
                                        onSeekTo = {},
                                        onOpenLyrics = {},
                                        onOpenQueue = {},
                                        onOpenMoreMenu = {},
                                        onOpenStylesSelector = {},
                                        onToggleShuffle = {},
                                        onToggleRepeat = {},
                                        isPreview = true
                                    )
                                }
                                PlayerUiStyle.POSTER -> {
                                    PosterPlayerStage(
                                        track = track,
                                        effectiveCoverUrl = effectiveCoverUrl,
                                        playerState = playerState,
                                        smoothPositionSecState = smoothPositionSecState,
                                        lyricsData = lyricsData,
                                        bitChordLyricsLines = bitChordLyricsLines,
                                        isFavorite = isFavorite,
                                        ambientColor = ambientColor,
                                        onToggleFavorite = {},
                                        onPreviousTrack = {},
                                        onTogglePlayPause = {},
                                        onNextTrack = {},
                                        onSeekTo = {},
                                        onOpenLyrics = {},
                                        onOpenQueue = {},
                                        onOpenMoreMenu = {},
                                        onOpenStylesSelector = {},
                                        onToggleShuffle = {},
                                        onToggleRepeat = {},
                                        isPreview = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Page Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                styles.indices.forEach { i ->
                    val isSelected = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = if (isSelected) 1f else 0.32f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Bottom Action Buttons: "Edit" and "Set"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (focusedStyle.editable) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable {
                                if (focusedStyle == PlayerUiStyle.TURNTABLE) {
                                    showTurntableEditSheet = true
                                } else if (focusedStyle == PlayerUiStyle.POSTER) {
                                    showPosterEditSheet = true
                                }
                            }
                            .testTag("styles_selector_edit_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Edit",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable {
                            onSelectStyle(focusedStyle)
                            onDismiss()
                        }
                        .testTag("styles_selector_set_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Set",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Turntable Label Customization Sheet
        if (showTurntableEditSheet) {
            TurntableLabelCustomizationSheet(
                track = track,
                onDismiss = {
                    showTurntableEditSheet = false
                    onSelectStyle(PlayerUiStyle.TURNTABLE)
                }
            )
        }

        // Poster Visualizer Customization Sheet
        if (showPosterEditSheet) {
            PosterCustomizationSheet(
                onDismiss = {
                    showPosterEditSheet = false
                    onSelectStyle(PlayerUiStyle.POSTER)
                }
            )
        }
    }
}

@Composable
private fun DefaultStylePreviewCard(
    track: MusicItem,
    effectiveCoverUrl: String,
    ambientColor: Color,
    isPlaying: Boolean,
    progressFraction: Float,
    durationSec: Int,
) {
    val context = LocalContext.current
    val deepTop = remember(ambientColor) { lerp(ambientColor, Color(0xFF201E1B), 0.25f) }
    val deepBot = remember(ambientColor) { lerp(ambientColor, Color(0xFF0E0D0B), 0.72f) }
    val durationMs = (durationSec.coerceAtLeast(1) * 1000L)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(deepTop, deepBot)))
    ) {
        AsyncImage(
            model = remember(context, effectiveCoverUrl) {
                ImageRequest.Builder(context)
                    .data(effectiveCoverUrl)
                    .size(600, 600)
                    .allowHardware(true)
                    .build()
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.62f)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.55f to Color.Transparent,
                            1.00f to deepBot
                        )
                    )
                }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.cleanArtist,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.24f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction.coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .background(Color.White)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatStyleDuration((progressFraction * durationMs).toLong()),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.65f)
                )
                Text(
                    text = formatStyleDuration(durationMs),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StyleDoubleTriangleIcon(pointLeft = true, tint = Color.White, modifier = Modifier.size(34.dp))
                StylePlayPauseGlyph(isPlaying = isPlaying, tint = Color.White, modifier = Modifier.size(40.dp))
                StyleDoubleTriangleIcon(pointLeft = false, tint = Color.White, modifier = Modifier.size(34.dp))
            }

            Spacer(modifier = Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.10f))
            )
        }
    }
}

@Composable
fun TurntableLabelCustomizationSheet(
    track: MusicItem?,
    onDismiss: () -> Unit,
) {
    val selectedContent by AppSettings.turntableLabelContent.collectAsStateWithLifecycle()
    val customName by AppSettings.turntableCustomName.collectAsStateWithLifecycle()
    val rotatingEnabled by AppSettings.turntableRotatingAnimation.collectAsStateWithLifecycle()
    val realisticMode by AppSettings.turntableRealisticMode.collectAsStateWithLifecycle()

    var textFieldValue by remember(customName) { mutableStateOf(customName) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color(0xFF1A1A1E))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.35f))
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "What to change with",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "The disc label",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.55f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TurntableLabelContent.entries.forEach { option ->
                    val isSelected = selectedContent == option
                    val previewSubtitle = when (option) {
                        TurntableLabelContent.CUSTOM -> "Whatever you type"
                        TurntableLabelContent.MUSIC_NAME -> track?.title ?: option.subtitle
                        TurntableLabelContent.ARTIST_NAME -> track?.cleanArtist ?: option.subtitle
                        TurntableLabelContent.ALBUM_NAME -> track?.subtitle?.ifBlank { track.title } ?: option.subtitle
                        TurntableLabelContent.MUSIC_AND_ARTIST ->
                            if (track != null) "${track.title} · ${track.cleanArtist}" else option.subtitle
                        TurntableLabelContent.NONE -> option.subtitle
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) Color(0xFF2D2D33) else Color(0xFF232328))
                            .clickable { AppSettings.setTurntableLabelContent(option) }
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.label,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = previewSubtitle,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.62f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (isSelected) {
                                Text(
                                    text = "SELECTED",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.48f)
                                )
                            }
                        }

                        if (option == TurntableLabelContent.CUSTOM && isSelected) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                if (textFieldValue.isEmpty()) {
                                    Text(
                                        text = "Type the label",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.40f)
                                    )
                                }
                                BasicTextField(
                                    value = textFieldValue,
                                    onValueChange = { next ->
                                        val clean = next.take(40)
                                        textFieldValue = clean
                                        AppSettings.setTurntableCustomName(clean)
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    ),
                                    cursorBrush = SolidColor(Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Rotating animation toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF232328))
                        .clickable { AppSettings.setTurntableRotatingAnimation(!rotatingEnabled) }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rotating animation",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Spin the vinyl label while music plays",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.62f)
                        )
                    }
                    Switch(
                        checked = rotatingEnabled,
                        onCheckedChange = { AppSettings.setTurntableRotatingAnimation(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color.White
                        )
                    )
                }

                // Realistic vinyl grooves toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF232328))
                        .clickable { AppSettings.setTurntableRealisticMode(!realisticMode) }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Realistic mode",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Grooves, track gaps & light reflections on platter",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.62f)
                        )
                    }
                    Switch(
                        checked = realisticMode,
                        onCheckedChange = { AppSettings.setTurntableRealisticMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Done",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PosterCustomizationSheet(
    onDismiss: () -> Unit,
) {
    val visualizerEnabled by AppSettings.posterVisualizerEnabled.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color(0xFF1A1A1E))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.35f))
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "What to change with",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "The poster",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.55f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF232328))
                    .clickable { AppSettings.setPosterVisualizerEnabled(!visualizerEnabled) }
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Visualizer",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Bars round the cover, moving with the music",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.62f)
                    )
                }
                Switch(
                    checked = visualizerEnabled,
                    onCheckedChange = { AppSettings.setPosterVisualizerEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Done",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

// ============================================================================
// SHARED TRANSPORT VECTOR GLYPHS
// ============================================================================

@Composable
private fun StyleDoubleTriangleIcon(
    pointLeft: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val topY = h * 0.18f
        val botY = h * 0.82f
        val midY = h * 0.50f
        val cornerRadiusPx = w * 0.07f

        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.FILL
                color = tint.toArgb()
                pathEffect = android.graphics.CornerPathEffect(cornerRadiusPx)
            }
            val path = android.graphics.Path()
            if (pointLeft) {
                path.moveTo(w * 0.04f, midY)
                path.lineTo(w * 0.50f, topY)
                path.lineTo(w * 0.50f, botY)
                path.close()

                path.moveTo(w * 0.48f, midY)
                path.lineTo(w * 0.94f, topY)
                path.lineTo(w * 0.94f, botY)
                path.close()
            } else {
                path.moveTo(w * 0.52f, midY)
                path.lineTo(w * 0.06f, topY)
                path.lineTo(w * 0.06f, botY)
                path.close()

                path.moveTo(w * 0.96f, midY)
                path.lineTo(w * 0.50f, topY)
                path.lineTo(w * 0.50f, botY)
                path.close()
            }
            canvas.nativeCanvas.drawPath(path, paint)
        }
    }
}

@Composable
private fun StylePlayPauseGlyph(
    isPlaying: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (isPlaying) {
            val barW = w * 0.25f
            val barH = h * 0.78f
            val topY = (h - barH) / 2f
            val gap = w * 0.18f
            val leftX = (w - (barW * 2f + gap)) / 2f
            val rightX = leftX + barW + gap
            val r = CornerRadius(barW * 0.40f, barW * 0.40f)
            drawRoundRect(color = tint, topLeft = Offset(leftX, topY), size = Size(barW, barH), cornerRadius = r)
            drawRoundRect(color = tint, topLeft = Offset(rightX, topY), size = Size(barW, barH), cornerRadius = r)
        } else {
            val cornerRadiusPx = w * 0.09f
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    style = android.graphics.Paint.Style.FILL
                    color = tint.toArgb()
                    pathEffect = android.graphics.CornerPathEffect(cornerRadiusPx)
                }
                val path = android.graphics.Path().apply {
                    moveTo(w * 0.88f, h * 0.50f)
                    lineTo(w * 0.18f, h * 0.12f)
                    lineTo(w * 0.18f, h * 0.88f)
                    close()
                }
                canvas.nativeCanvas.drawPath(path, paint)
            }
        }
    }
}

private fun formatStyleDuration(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val mins = totalSeconds / 60L
    val secs = totalSeconds % 60L
    return "%d:%02d".format(mins, secs)
}
