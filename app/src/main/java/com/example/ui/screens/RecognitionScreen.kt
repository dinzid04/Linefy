package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.local.AppDatabase
import com.example.data.local.RecognitionHistoryEntity
import com.example.data.settings.AppSettings
import com.example.recognition.MusicRecognitionService
import com.example.shazamkit.models.RecognitionUiState
import com.example.shazamkit.models.ShazamRecognitionResult
import com.example.ui.components.LyrafyLogoLoadingIndicator
import com.example.ui.components.liquidGlass
import com.example.ui.components.lyraGlassCard
import com.example.ui.theme.rememberDominantAccentColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MICROPHONE_SVG_PATH_1 =
    "M0 0 C16.43 12.3 26.33 27.23 29.3 47.72 C29.52 50.49 29.52 52.94 29.3 55.72 C27.36 57.65 23.73 56.93 21.09 56.96 C19.6 57 18.11 57.03 16.62 57.06 C14.27 57.11 11.92 57.16 9.57 57.2 C7.3 57.24 5.04 57.29 2.77 57.34 C2.07 57.35 1.37 57.36 0.64 57.37 C-3.01 57.46 -4.58 57.64 -7.7 59.72 C-7.95 62.3 -7.95 62.3 -7.7 64.72 C-4.72 66.21 -1.64 65.92 1.64 65.96 C3.13 66 4.62 66.03 6.11 66.06 C8.46 66.11 10.81 66.16 13.16 66.2 C15.42 66.24 17.69 66.29 19.95 66.34 C20.66 66.35 21.36 66.36 22.09 66.37 C27.07 66.49 27.07 66.49 29.3 68.72 C29.54 70.67 29.54 70.67 29.53 73.04 C29.53 73.88 29.52 74.73 29.52 75.6 C29.51 76.48 29.5 77.37 29.48 78.28 C29.48 79.61 29.48 79.61 29.48 80.97 C29.44 87.58 29.44 87.58 28.3 88.72 C25.63 88.81 22.99 88.83 20.32 88.82 C19.53 88.81 18.73 88.81 17.91 88.81 C15.35 88.81 12.79 128.79 10.23 88.78 C8.5 88.78 6.77 88.77 5.04 88.77 C0.8 88.76 -3.45 88.74 -7.7 88.72 C-7.75 91.05 -7.74 93.39 -7.7 95.72 C-5.7 97.72 -1.73 96.95 1.02 97 C1.79 97.02 2.56 97.04 3.36 97.06 C5.84 97.11 8.32 97.17 10.8 97.22 C14.06 97.29 17.32 97.36 20.58 97.44 C21.34 97.45 22.09 97.46 22.87 97.48 C28.18 97.61 28.18 97.61 29.3 98.72 C29.55 101.86 29.48 105.01 29.48 108.16 C29.5 109.04 29.51 109.93 29.52 110.84 C29.52 111.68 29.53 112.53 29.53 113.4 C29.53 114.18 29.54 114.96 29.54 115.76 C29.3 117.72 29.3 117.72 27.3 119.72 C24.73 119.95 22.27 120.07 19.7 120.1 C18.95 120.11 18.2 120.13 17.43 120.15 C15.03 120.2 12.63 120.24 10.23 120.28 C7.83 120.33 5.43 120.37 3.03 120.42 C1.53 120.46 0.04 120.48 -1.46 120.5 C-4.87 120.27 -4.87 120.27 -7.7 121.72 C-7.87 124.22 -7.87 124.22 -7.7 126.72 C-5.74 128.69 -1.93 127.93 0.76 127.96 C2.3 128 3.84 128.03 5.37 128.06 C7.81 128.11 10.24 128.16 12.67 128.2 C15.02 128.24 17.36 128.29 19.7 128.34 C20.43 128.35 21.16 128.36 21.91 128.37 C27.07 128.49 27.07 128.49 29.3 130.72 C29.57 133.09 29.57 133.09 29.6 136.07 C29.61 137.16 29.62 138.25 29.63 139.38 C29.51 162.19 26.08 181.96 9.3 198.72 C-3.75 210.41 -19.28 216.35 -36.78 215.97 C-54.39 214.44 -70 207.05 -81.7 193.72 C-82.24 193.12 -82.77 192.51 -83.32 191.89 C-94.47 178.17 -97.23 161.73 -97.02 144.53 C-97.02 143.22 -97.02 141.92 -97.03 140.57 C-96.97 130.99 -96.97 130.99 -94.7 128.72 C-92.06 128.47 -89.5 128.35 -86.85 128.3 C-85.69 128.28 -85.69 128.28 -84.51 128.25 C-82.03 128.19 -79.55 128.14 -77.08 128.09 C-75.4 128.06 -73.72 128.02 -72.05 127.98 C-67.93 127.88 -63.82 127.8 -59.7 127.72 C-60.36 125.41 -61.02 123.1 -61.7 120.72 C-62.73 120.69 -63.75 120.67 -64.81 120.64 C-68.6 120.55 -72.39 120.45 -76.17 120.35 C-77.82 120.3 -79.46 120.26 -81.1 120.22 C-83.46 120.16 -85.81 120.1 -88.17 120.04 C-88.91 120.02 -89.65 120 -90.41 119.98 C-95.59 119.83 -95.59 119.83 -96.7 118.72 C-96.8 117.24 -96.83 115.76 -96.84 114.28 C-96.84 113.39 -96.84 112.49 -96.84 111.57 C-96.83 110.16 -96.83 110.16 -96.83 108.72 C-96.83 107.78 -96.84 106.84 -96.84 105.87 C-96.84 104.97 -96.84 104.08 -96.84 103.16 C-96.83 101.92 -96.83 101.92 -96.83 100.65 C-96.7 98.72 -96.7 98.72 -95.7 97.72 C-93.2 97.57 -90.71 97.48 -88.2 97.44 C-87.45 97.42 -86.7 97.4 -85.93 97.38 C-82.7 97.3 -79.48 97.24 -76.25 97.17 C-73.9 97.11 -71.55 97.06 -69.2 97 C-68.1 96.98 -68.1 96.98 -66.97 96.96 C-63.55 97.17 -63.55 97.17 -60.7 95.72 C-60.66 93.72 -60.66 91.72 -60.7 89.72 C-61.69 89.7 -62.67 89.68 -63.68 89.66 C-67.34 89.58 -70.99 89.49 -74.64 89.4 C-76.22 89.36 -77.8 89.33 -79.38 89.3 C-81.65 89.25 -83.92 89.19 -86.19 89.13 C-86.9 89.12 -87.6 89.11 -88.33 89.1 C-91.98 88.99 -93.59 88.8 -96.7 86.72 C-97.06 84.76 -97.06 84.76 -97.03 82.4 C-97.02 81.13 -97.02 81.13 -97.02 79.84 C-97 78.95 -96.97 78.07 -96.95 77.16 C-96.95 76.27 -96.94 75.38 -96.94 74.46 C-96.86 67.87 -96.86 67.87 -95.7 66.72 C-93.28 66.57 -90.88 66.48 -88.46 66.44 C-87.73 66.42 -87 66.4 -86.26 66.38 C-83.93 66.32 -81.6 66.27 -79.27 66.22 C-76.2 66.15 -73.14 66.08 -70.08 66 C-69.37 65.99 -68.67 65.97 -67.94 65.96 C-64.19 65.91 -64.19 65.91 -60.7 64.72 C-60.05 61.69 -60.05 61.69 -59.7 58.72 C-63.47 57.54 -67.13 57.53 -71.06 57.44 C-71.76 57.42 -72.46 57.4 -73.19 57.38 C-76.18 57.3 -79.18 57.24 -82.18 57.17 C-84.37 57.11 -86.56 57.06 -88.75 57 C-90.06 56.97 -91.37 56.94 -92.72 56.91 C-95.7 56.72 -95.7 56.72 -96.7 55.72 C-98.19 39.01 -91.09 23.42 -81.14 10.47 C-61.13 -12.38 -25.37 -16.68 0 0 Z"

private const val MICROPHONE_SVG_PATH_2 =
    "M0 0 C1.22 -0 1.22 -0 2.47 -0.01 C4.2 -0.02 5.93 -0.02 7.65 -0.02 C10.3 -0.02 12.95 -0.04 15.6 -0.06 C17.27 -0.06 18.95 -0.06 20.62 -0.06 C21.42 -0.07 22.22 -0.08 23.04 -0.08 C28.65 -0.06 28.65 -0.06 30.88 2.17 C31.27 3.98 31.27 3.98 31.48 6.18 C33.61 24.89 39.48 41.18 53.88 54.17 C54.47 54.76 55.05 55.35 55.66 55.96 C70.12 69.91 88.26 74.83 107.96 74.71 C116.98 74.44 124.6 72.69 132.88 69.17 C133.96 68.71 135.05 68.25 136.17 67.78 C142.38 64.91 147.54 61.36 152.72 56.9 C154.88 55.17 154.88 55.17 157.04 54.18 C159.45 52.86 159.76 51.67 160.88 49.17 C161.91 47.59 162.95 46.01 164 44.45 C173.04 30.92 176.98 17.29 178.88 1.17 C180.67 -0.62 183.07 0.04 185.46 0.03 C186.23 0.03 187.01 0.03 187.82 0.04 C188.61 0.03 189.41 0.03 190.23 0.03 C191.91 0.03 193.6 0.03 195.28 0.04 C197.87 0.04 200.46 0.04 203.04 0.03 C204.68 0.03 206.31 0.03 207.94 0.04 C209.11 0.03 209.11 0.03 210.3 0.03 C215.76 0.05 215.76 0.05 216.88 1.17 C216.97 2.73 216.99 4.3 216.98 5.86 C216.97 6.8 216.97 7.75 216.97 8.72 C216.96 9.71 216.95 10.71 216.94 11.73 C216.94 12.73 216.93 13.72 216.93 14.75 C216.92 17.22 216.9 19.7 216.88 22.17 C213.12 24.23 210.44 24.38 206.13 24.29 C204.96 24.27 203.79 24.26 202.58 24.24 C201.69 24.22 200.8 24.19 199.88 24.17 C199.71 24.92 199.54 25.67 199.36 26.44 C193.5 51.22 177.75 71.6 156.69 85.86 C147.58 91.43 135.6 97.17 124.88 97.17 C124.88 110.37 124.88 123.57 124.88 137.17 C129.83 137.5 134.78 137.83 139.88 138.17 C146.71 141.67 151.91 145.82 154.62 153.18 C155.74 157.19 156.18 161.02 155.88 165.17 C153.94 167.11 151 166.29 148.42 166.3 C147.02 166.3 147.02 166.3 145.6 166.3 C144.14 166.3 144.14 166.3 142.65 166.3 C139.41 166.31 136.18 166.3 132.94 166.3 C130.7 166.3 128.47 166.3 126.23 166.3 C121.53 166.31 116.83 166.3 112.13 166.3 C106.1 166.29 100.06 166.3 94.02 166.3 C89.4 166.31 84.78 166.3 80.15 166.3 C77.93 166.3 75.7 166.3 73.47 166.3 C70.37 166.31 67.26 166.3 64.16 166.3 C63.23 166.3 62.3 166.3 61.34 166.3 C54.99 166.28 54.99 166.28 53.88 165.17 C53.1 158.07 54.94 153.09 58.88 147.17 C66.25 138.81 73.74 138.1 84.88 137.17 C84.55 124.3 84.22 111.43 83.88 98.17 C78.6 96.52 73.32 94.87 67.88 93.17 C46.33 83.47 27.18 67.84 16.88 46.17 C16.45 45.3 16.02 44.43 15.58 43.53 C12.74 37.55 10.58 31.56 8.88 25.17 C7.91 25.12 6.95 25.07 5.96 25.02 C4.69 24.94 3.43 24.87 2.13 24.79 C0.88 24.72 -0.38 24.65 -1.67 24.58 C-4.81 24.2 -6.42 23.64 -9.12 22.17 C-9.15 18.69 -9.17 15.21 -9.18 11.73 C-9.19 10.74 -9.2 9.74 -9.21 8.72 C-9.21 7.78 -9.22 6.83 -9.22 5.86 C-9.22 4.98 -9.23 4.11 -9.23 3.21 C-8.97 -1.63 -3.73 0.01 0 0 Z"

/**
 * Renders the exact SVG vector icon from `microphone.txt` (viewport 736 x 736, cropped tightly around the microphone bounds).
 */
@Composable
fun LyrafyMicrophoneIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFFBFBFB)
) {
    val path1 = remember { PathParser().parsePathString(MICROPHONE_SVG_PATH_1).toPath() }
    val path2 = remember { PathParser().parsePathString(MICROPHONE_SVG_PATH_2).toPath() }

    Canvas(modifier = modifier) {
        // Tight bounding box of the two translated microphone paths inside 736x736 is roughly X:[255..482], Y:[204..538]
        // Center is (368.5f, 371.0f), span is ~340f
        val targetSpan = 350f
        val scaleFactor = (size.minDimension / targetSpan)
        withTransform({
            translate(left = size.width / 2f, top = size.height / 2f)
            scale(scaleX = scaleFactor, scaleY = scaleFactor, pivot = androidx.compose.ui.geometry.Offset.Zero)
            translate(left = -368.5f, top = -371.0f)
        }) {
            withTransform({
                translate(left = 403.703125f, top = 216.28125f)
            }) {
                drawPath(path = path1, color = tint)
            }
            withTransform({
                translate(left = 265.12109375f, top = 370.83203125f)
            }) {
                drawPath(path = path2, color = tint)
            }
        }
    }
}

/**
 * Floating "Kenali Musik" button (56.dp circle) shown on Home & Search tabs when enabled.
 * Automatically adapts to Liquid Glass vs Original mode and auto-hides on scroll down.
 */
@Composable
fun FloatingMusicRecognitionButton(
    visible: Boolean,
    liquidGlassEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dynamicAccentColor: Color = Color(0xFFE2B963)
) {
    val useDynamicTheme by AppSettings.useDynamicMusicTheme.collectAsState()
    val fullLiquidGlass by AppSettings.fullLiquidGlassUi.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) +
            scaleIn(initialScale = 0.72f, animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)) +
            slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(240)),
        exit = fadeOut(tween(180)) +
            scaleOut(targetScale = 0.72f, animationSpec = tween(180)) +
            slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(200)),
        modifier = modifier
    ) {
        val baseModifier = Modifier
            .size(56.dp)
            .testTag("floating_music_recognition_button")

        val styledModifier = if (liquidGlassEnabled || fullLiquidGlass) {
            val tint = if (useDynamicTheme) {
                dynamicAccentColor.copy(alpha = 0.28f)
            } else {
                Color(0xFF1A1B20).copy(alpha = 0.44f)
            }
            baseModifier
                .liquidGlass(
                    shape = CircleShape,
                    surfaceTint = tint
                )
        } else {
            val bgColor = if (useDynamicTheme) {
                Color(0xFF1A1B20)
            } else {
                Color(0xFF17181C)
            }
            baseModifier
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.65f),
                    spotColor = Color.Black.copy(alpha = 0.65f)
                )
                .clip(CircleShape)
                .background(bgColor, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
        }

        Box(
            modifier = styledModifier.clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 28.dp, color = Color.White),
                onClick = onClick
            ),
            contentAlignment = Alignment.Center
        ) {
            LyrafyMicrophoneIcon(
                modifier = Modifier.size(26.dp),
                tint = Color(0xFFFBFBFB)
            )
        }
    }
}

/**
 * Full-screen "Kenali Musik" screen matching `1002167764.png`, `1002167765.png`, `1002167766.png`, and `1002167654.jpg`.
 */
@Composable
fun RecognitionScreen(
    onBack: () -> Unit,
    onPlayInLyra: (title: String, artist: String) -> Unit,
    dynamicAccentColor: Color = Color(0xFFE2B963)
) {
    val context = LocalContext.current
    val uiState by MusicRecognitionService.uiState.collectAsState()
    val useDynamicTheme by AppSettings.useDynamicMusicTheme.collectAsState()
    val fullLiquidGlass by AppSettings.fullLiquidGlassUi.collectAsState()

    var showHistoryScreen by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            MusicRecognitionService.startRecognition(context)
        }
    }

    fun triggerListening() {
        val hasMicPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasMicPerm) {
            MusicRecognitionService.startRecognition(context)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Automatically start listening when screen opens if currently Idle
    LaunchedEffect(Unit) {
        if (uiState is RecognitionUiState.Idle) {
            triggerListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (MusicRecognitionService.uiState.value is RecognitionUiState.Listening) {
                MusicRecognitionService.cancelRecognition(context)
            }
        }
    }

    BackHandler {
        if (showHistoryScreen) {
            showHistoryScreen = false
        } else {
            MusicRecognitionService.cancelRecognition(context)
            onBack()
        }
    }

    if (showHistoryScreen) {
        RecognitionHistoryScreen(
            onBack = { showHistoryScreen = false },
            onSelectResult = { historyItem ->
                showHistoryScreen = false
                onPlayInLyra(historyItem.title, historyItem.artist)
            }
        )
        return
    }

    val backgroundBrush = if (useDynamicTheme) {
        Brush.verticalGradient(
            colors = listOf(
                dynamicAccentColor.copy(alpha = 0.24f),
                Color(0xFF0A0B0E),
                Color(0xFF060608)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0D0E12),
                Color(0xFF07080A),
                Color(0xFF050507)
            )
        )
    }

    val accentColor = if (useDynamicTheme) {
        dynamicAccentColor
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar: Back arrow on left, "Kenali musik" title, History icon on right (hidden when Result Found if matches 1002167654.jpg or kept clean)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    MusicRecognitionService.cancelRecognition(context)
                    onBack()
                },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("recognition_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }

            Text(
                text = "Kenali musik",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
            )

            IconButton(
                onClick = { showHistoryScreen = true },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("recognition_history_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = "Riwayat Pengenalan",
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Main Animated Body for the 4 States
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                (fadeIn(tween(340, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.90f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) +
                    slideInVertically(
                        initialOffsetY = { it / 6 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )).togetherWith(
                    fadeOut(tween(220)) + scaleOut(targetScale = 0.94f, animationSpec = tween(220))
                )
            },
            label = "RecognitionStateTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp)
        ) { state ->
            when (state) {
                is RecognitionUiState.Idle -> {
                    RecognitionIdleView(
                        onTapRecognize = { triggerListening() },
                        fullLiquidGlass = fullLiquidGlass
                    )
                }

                is RecognitionUiState.Listening -> {
                    RecognitionListeningView(
                        amplitude = state.amplitude,
                        canFinishEarly = state.canFinishEarly,
                        onFinishEarly = { MusicRecognitionService.finishListeningEarly() },
                        onCancel = { MusicRecognitionService.resetToIdle() },
                        fullLiquidGlass = fullLiquidGlass,
                        accentColor = accentColor
                    )
                }

                is RecognitionUiState.Processing -> {
                    RecognitionProcessingView(
                        onCancel = { MusicRecognitionService.resetToIdle() },
                        fullLiquidGlass = fullLiquidGlass
                    )
                }

                is RecognitionUiState.Success -> {
                    RecognitionResultCardView(
                        result = state.result,
                        useDynamicTheme = useDynamicTheme,
                        fullLiquidGlass = fullLiquidGlass,
                        onPlayInLyra = {
                            val title = state.result.title
                            val artist = state.result.artist
                            MusicRecognitionService.resetToIdle()
                            onPlayInLyra(title, artist)
                        },
                        onListenAgain = {
                            triggerListening()
                        },
                        onClose = {
                            MusicRecognitionService.resetToIdle()
                            onBack()
                        }
                    )
                }

                is RecognitionUiState.NoMatch -> {
                    RecognitionNoMatchView(
                        message = state.message,
                        onRetry = { triggerListening() }
                    )
                }

                is RecognitionUiState.Error -> {
                    RecognitionNoMatchView(
                        message = state.message,
                        onRetry = { triggerListening() }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecognitionIdleView(
    onTapRecognize: () -> Unit,
    fullLiquidGlass: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // Subtle outer ring
                Box(
                    modifier = Modifier
                        .size(214.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                )

                // Main interactive circle button
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .shadow(20.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            if (fullLiquidGlass) Color(0xFF1D1E24).copy(alpha = 0.72f)
                            else Color(0xFF191A1F),
                            CircleShape
                        )
                        .border(1.2.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                        .clickable(onClick = onTapRecognize)
                        .testTag("recognition_tap_circle"),
                    contentAlignment = Alignment.Center
                ) {
                    LyrafyMicrophoneIcon(
                        modifier = Modifier.size(66.dp),
                        tint = Color(0xFFFBFBFB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "KENALI MUSIK",
                color = Color(0xFF9EA0A8),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ketuk untuk mengenali",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecognitionListeningView(
    amplitude: Float,
    canFinishEarly: Boolean,
    onFinishEarly: () -> Unit,
    onCancel: () -> Unit,
    fullLiquidGlass: Boolean,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ListeningRipples")
    val ripple1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )
    val ripple2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 730, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )
    val ripple3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 1460, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple3"
    )

    val smoothedAmp by animateFloatAsState(
        targetValue = amplitude.coerceIn(0.08f, 1f),
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "smoothedAmp"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(300.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerRadius = 84.dp.toPx()
                    val maxRadius = (size.minDimension / 2f) * (0.92f + smoothedAmp * 0.14f)

                    val ripples = listOf(ripple1, ripple2, ripple3)
                    for (progress in ripples) {
                        val currentRadius = centerRadius + (maxRadius - centerRadius) * progress
                        val alpha = (1f - progress) * (0.34f + smoothedAmp * 0.25f)
                        drawCircle(
                            color = accentColor.copy(alpha = alpha.coerceIn(0f, 0.55f)),
                            radius = currentRadius,
                            style = Stroke(width = 1.6.dp.toPx())
                        )
                        drawCircle(
                            color = accentColor.copy(alpha = (alpha * 0.18f).coerceIn(0f, 0.15f)),
                            radius = currentRadius
                        )
                    }
                }

                // Pulsing center circle with microphone
                val centerScale = 1f + (smoothedAmp * 0.08f)
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .graphicsLayer {
                            scaleX = centerScale
                            scaleY = centerScale
                        }
                        .shadow(24.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            if (fullLiquidGlass) Color(0xFF202229).copy(alpha = 0.78f)
                            else Color(0xFF1B1C22),
                            CircleShape
                        )
                        .border(1.4.dp, Color.White.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    LyrafyMicrophoneIcon(
                        modifier = Modifier.size(66.dp),
                        tint = Color(0xFFFBFBFB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "MENDENGARKAN...",
                color = Color(0xFFA6A8B0),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mendengarkan...",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Capsule button "Cukup" (appears when ready or always tappable after 2.8s)
            AnimatedVisibility(
                visible = canFinishEarly,
                enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.88f),
                exit = fadeOut(tween(150))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        onClick = onFinishEarly,
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF23252C),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("recognition_cukup_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cukup",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Capsule button "Batal"
            Surface(
                onClick = onCancel,
                shape = RoundedCornerShape(50),
                color = Color(0xFF17181D),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.14f)
                ),
                modifier = Modifier
                    .height(42.dp)
                    .testTag("recognition_cancel_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Batal",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun RecognitionProcessingView(
    onCancel: () -> Unit,
    fullLiquidGlass: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(206.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFE2B963).copy(alpha = 0.22f), CircleShape)
                )

                // Center Circle with Golden Harp Logo animation (`svg_3.txt`)
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .shadow(24.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            if (fullLiquidGlass) Color(0xFF1D1E24).copy(alpha = 0.82f)
                            else Color(0xFF18191E),
                            CircleShape
                        )
                        .border(1.4.dp, Color(0xFFE2B963).copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    LyrafyLogoLoadingIndicator(
                        size = 86.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "MEMPROSES...",
                color = Color(0xFFE2B963),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Memproses...",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                onClick = onCancel,
                shape = RoundedCornerShape(50),
                color = Color(0xFF17181D),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.14f)
                ),
                modifier = Modifier.height(42.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Batal",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * State 4: Result Found UI matching `1002167654.jpg` 1:1.
 * Left-aligned cover art, "KENALI MUSIK" header, bold song title, artist name, album name,
 * and 3 full-width vertical capsule buttons:
 * 1. "Putar di Lyra"
 * 2. "Dengarkan ulang"
 * 3. "Tutup"
 */
@Composable
private fun RecognitionResultCardView(
    result: ShazamRecognitionResult,
    useDynamicTheme: Boolean,
    fullLiquidGlass: Boolean,
    onPlayInLyra: () -> Unit,
    onListenAgain: () -> Unit,
    onClose: () -> Unit
) {
    val resultAccent = rememberDominantAccentColor(
        imageUrl = result.coverArtHqUrl ?: result.coverArtUrl,
        defaultColor = Color(0xFFE2B963)
    )
    val primaryButtonBg = if (useDynamicTheme) {
        resultAccent
    } else {
        Color(0xFFF3F4F6)
    }
    val primaryButtonContent = if (useDynamicTheme) {
        Color.White
    } else {
        Color(0xFF0E0F12)
    }
    val secondaryButtonBg = if (fullLiquidGlass) {
        Color(0xFF22242B).copy(alpha = 0.72f)
    } else {
        Color(0xFF1C1D22)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        // Rounded Square Cover Art (left-aligned as in 1002167654.jpg)
        val coverModel = result.coverArtHqUrl ?: result.coverArtUrl
        Box(
            modifier = Modifier
                .size(236.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1B20))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!coverModel.isNullOrBlank()) {
                AsyncImage(
                    model = coverModel,
                    contentDescription = result.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LyrafyMicrophoneIcon(
                    modifier = Modifier.size(72.dp),
                    tint = Color.White.copy(alpha = 0.35f)
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "KENALI MUSIK",
            color = Color(0xFF9EA0A8),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = result.title,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = result.artist,
            color = Color(0xFFD5D7DE),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        if (!result.album.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.album,
                color = Color(0xFF8E9099),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 1. "Putar di Lyra" full-width capsule button
        Button(
            onClick = onPlayInLyra,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryButtonBg,
                contentColor = primaryButtonContent
            ),
            contentPadding = PaddingValues(vertical = 15.dp, horizontal = 22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("play_in_lyra_button")
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Putar di Lyra",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. "Dengarkan ulang" full-width capsule button
        Surface(
            onClick = onListenAgain,
            shape = RoundedCornerShape(50),
            color = secondaryButtonBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("listen_again_button")
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                LyrafyMicrophoneIcon(
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Dengarkan ulang",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. "Tutup" full-width outline capsule button
        OutlinedButton(
            onClick = onClose,
            shape = RoundedCornerShape(50),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            contentPadding = PaddingValues(vertical = 14.dp, horizontal = 22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("close_recognition_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Tutup",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RecognitionNoMatchView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF191A20))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                    .clickable(onClick = onRetry),
                contentAlignment = Alignment.Center
            ) {
                LyrafyMicrophoneIcon(
                    modifier = Modifier.size(56.dp),
                    tint = Color.White.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "TIDAK DIKENALI",
                color = Color(0xFF9EA0A8),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Coba Lagi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RecognitionHistoryScreen(
    onBack: () -> Unit,
    onSelectResult: (RecognitionHistoryEntity) -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getInstance(context).recentTrackDao() }
    val history by dao.observeRecognitionHistory().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()) }

    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0D))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
            Text(
                text = "Riwayat Kenali Musik",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
            )
            if (history.isNotEmpty()) {
                IconButton(
                    onClick = {
                        scope.launch { dao.clearRecognitionHistory() }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Hapus Semua",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LyrafyMicrophoneIcon(
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.35f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum ada riwayat lagu yang dikenali",
                        color = Color(0xFF8E9099),
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(history, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF15161B))
                            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
                            .clickable { onSelectResult(item) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.coverArtUrl ?: item.coverArtHqUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF22232A))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.artist,
                                color = Color(0xFFB5B7C0),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateFormatter.format(Date(item.recognizedAt)),
                                color = Color(0xFF777982),
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                scope.launch { dao.deleteRecognitionHistoryById(item.id) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Hapus",
                                tint = Color(0xFF8E9099),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
