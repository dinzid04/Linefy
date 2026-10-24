package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.BlurOff
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Gradient
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.MotionPhotosOff
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.WebAssetOff
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.Coil
import coil.compose.AsyncImage
import com.example.data.lyrics.translationLanguageName
import com.example.data.model.Account
import com.example.data.remote.AppUpdateChecker
import com.example.data.settings.AppSettings
import com.example.data.settings.AudioQuality
import com.example.data.settings.AutomixPerformanceMode
import com.example.data.settings.DownloadQuality
import com.example.data.settings.OutputPcmMode
import com.example.data.settings.ThemeMode
import com.example.ui.components.isGlassSupported
import com.example.ui.icons.BitChordIcons
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitChordSettingsScreen(
    windowWidth: Dp = 390.dp,
    signedIn: Boolean,
    account: Account?,
    onBack: (() -> Unit)? = null,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onAccountScrobbling: () -> Unit,
    onEqualizer: () -> Unit,
    onOpenReplay: () -> Unit = {},
    onLyricsSources: () -> Unit,
    onTranslationLanguage: () -> Unit,
    onSources: () -> Unit,
    onListenTogether: () -> Unit,
    onOpenDeveloperAndLyra: () -> Unit = {},
    onSpotifyCanvasAuth: () -> Unit,
    onAppLanguage: () -> Unit = {},
    onEditSignatureApi: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(bottom = 140.dp),
    modifier: Modifier = Modifier,
) {
    if (onBack != null) {
        BackHandler(onBack = onBack)
    }

    val context = LocalContext.current

    val wifiQuality by AppSettings.audioQualityWifi.collectAsStateWithLifecycle()
    val cellularQuality by AppSettings.audioQualityCellular.collectAsStateWithLifecycle()
    val metered by AppSettings.meteredConnection.collectAsStateWithLifecycle()
    val crossfade by AppSettings.crossfadeSeconds.collectAsStateWithLifecycle()
    val smartFade by AppSettings.smartFadeEnabled.collectAsStateWithLifecycle()
    val automixPerformance by AppSettings.automixPerformanceMode.collectAsStateWithLifecycle()
    val skipSilence by AppSettings.skipSilence.collectAsStateWithLifecycle()
    val dolbyAtmos by AppSettings.dolbyAtmos.collectAsStateWithLifecycle()
    val dolbyAtmosSupported = true
    val spatialAudio by AppSettings.spatialAudio.collectAsStateWithLifecycle()
    val nerdStats by AppSettings.showNerdStats.collectAsStateWithLifecycle()
    val reduceAnimation by AppSettings.reduceAnimation.collectAsStateWithLifecycle()
    val reduceDynamicBlur by AppSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    val liquidGlass by AppSettings.liquidGlass.collectAsStateWithLifecycle()
    val fullLiquidGlassUi by AppSettings.fullLiquidGlassUi.collectAsStateWithLifecycle()
    val liquidGlassSupported = isGlassSupported()
    val hasNewerRelease by AppUpdateChecker.hasNewerRelease.collectAsStateWithLifecycle()
    val isCheckingUpdate by AppUpdateChecker.isChecking.collectAsStateWithLifecycle()
    val lyricsBlur by AppSettings.lyricsBlur.collectAsStateWithLifecycle()
    val animatedCanvas by AppSettings.animatedCanvas.collectAsStateWithLifecycle()
    val canvasOverCellular by AppSettings.canvasOverCellular.collectAsStateWithLifecycle()
    val signatureApiEnabled by AppSettings.signatureApiEnabled.collectAsStateWithLifecycle()
    val useBitChordHomeUi by AppSettings.useBitChordHomeUi.collectAsStateWithLifecycle()
    val useNewQuickPicksGrid by AppSettings.useNewQuickPicksGrid.collectAsStateWithLifecycle()
    val useNewSongShelvesDesign by AppSettings.useNewSongShelvesDesign.collectAsStateWithLifecycle()
    val useNewCommunityDesign by AppSettings.useNewCommunityDesign.collectAsStateWithLifecycle()
    val useDynamicMusicTheme by AppSettings.useDynamicMusicTheme.collectAsStateWithLifecycle()
    val signatureApiUrl by AppSettings.signatureApiUrl.collectAsStateWithLifecycle()
    val fullBleedArtwork by AppSettings.fullBleedArtwork.collectAsStateWithLifecycle()
    val legacyMeshGradient by AppSettings.legacyMeshGradient.collectAsStateWithLifecycle()
    val syncedLyrics by AppSettings.syncedLyrics.collectAsStateWithLifecycle()
    val lyricsSources by AppSettings.lyricsSources.collectAsStateWithLifecycle()
    val translationLanguage by AppSettings.translationLanguage.collectAsStateWithLifecycle()
    val theme by AppSettings.themeMode.collectAsStateWithLifecycle()
    val outputPcmMode by AppSettings.outputPcmMode.collectAsStateWithLifecycle()
    val preferUsbDac by AppSettings.preferUsbDac.collectAsStateWithLifecycle()
    val loudnessNormalization by AppSettings.loudnessNormalization.collectAsStateWithLifecycle()
    val loudnessOffOnSpeaker by AppSettings.loudnessOffOnSpeaker.collectAsStateWithLifecycle()
    val cacheLimitBytes by AppSettings.audioCacheLimitBytes.collectAsStateWithLifecycle()
    val showCacheFolder by AppSettings.showCacheFolder.collectAsStateWithLifecycle()
    val downloadQuality by AppSettings.downloadQuality.collectAsStateWithLifecycle()
    val wifiOnlyDownloads by AppSettings.wifiOnlyDownloads.collectAsStateWithLifecycle()
    val exportDownloads by AppSettings.exportDownloads.collectAsStateWithLifecycle()
    val stopOnTaskRemoved by AppSettings.stopOnTaskRemoved.collectAsStateWithLifecycle()
    val hideVolumeBar by AppSettings.hideVolumeBar.collectAsStateWithLifecycle()
    val hideStatusBarInPlayer by AppSettings.hideStatusBarInPlayer.collectAsStateWithLifecycle()
    val hideSongStatus by AppSettings.hideSongStatus.collectAsStateWithLifecycle()
    val showQueueHistory by AppSettings.showQueueHistory.collectAsStateWithLifecycle()
    val swipeToPlayNext by AppSettings.swipeToPlayNext.collectAsStateWithLifecycle()
    val dontRepeatSuggestions by AppSettings.dontRepeatSuggestions.collectAsStateWithLifecycle()
    val preferMusicOnly by AppSettings.preferMusicOnly.collectAsStateWithLifecycle()
    val smartVersionAlignment by AppSettings.smartVersionAlignment.collectAsStateWithLifecycle()
    val filterNonMusicAudio by AppSettings.filterNonMusicAudio.collectAsStateWithLifecycle()
    val localMusicFolderUri by AppSettings.localMusicFolderUri.collectAsStateWithLifecycle()
    val highPerformanceMode by AppSettings.highPerformanceMode.collectAsStateWithLifecycle()
    val performanceRefreshRate by AppSettings.performanceRefreshRate.collectAsStateWithLifecycle()
    val replayGenres by AppSettings.replayGenres.collectAsStateWithLifecycle()

    val supportedRefreshRates = remember { listOf(60, 90, 120) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var picking by remember { mutableStateOf<QualityTarget?>(null) }
    var pickingDownloadQuality by remember { mutableStateOf(false) }
    var pickingAutomixPerformance by remember { mutableStateOf(false) }
    var confirmClearSongCache by remember { mutableStateOf(false) }
    var showPerformanceWarning by remember { mutableStateOf(false) }
    var showPerformanceConfirmation by remember { mutableStateOf(false) }
    var showSignatureUrlDialog by remember { mutableStateOf(false) }

    val batterySettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        showPerformanceConfirmation = true
    }
    val localMusicFolderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { folder ->
        if (folder == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                folder,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        AppSettings.setLocalMusicFolderUri(folder.toString())
    }

    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }

    val search = SettingsSearch(searchQuery)

    val scrollState = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }
    var isFirstSearchComposition by remember { mutableStateOf(true) }
    LaunchedEffect(searchQuery) {
        if (isFirstSearchComposition) {
            isFirstSearchComposition = false
        } else {
            isFirstSearchComposition = false
            scrollState.scrollTo(0)
        }
    }

    val switchColors = SwitchDefaults.colors(
        checkedTrackColor = Color.White,
        checkedThumbColor = Color.Black,
        checkedBorderColor = Color.White,
        uncheckedTrackColor = Color(0xFF2C2C2E),
        uncheckedThumbColor = Color(0xFF8E8E93),
        uncheckedBorderColor = Color(0xFF3A3A3C),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(scrollState)
            .padding(contentPadding)
            .testTag("settings_screen"),
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                        .clickable(onClick = onBack)
                        .testTag("settings_back_button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Text(
            text = "Settings",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.W800,
                fontSize = 34.sp,
                letterSpacing = (-0.8).sp,
            ),
            color = Color.White,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 14.dp),
        )

        SettingsSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search settings",
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, bottom = 4.dp)
                .testTag("settings_search_field"),
        )

        SearchableSettingsGroup(search) {
            val accountTitle = "Account & integrations"
            row(accountTitle, "scrobbling", "last.fm", "listenbrainz", "spotify", "discord", "youtube") {
                SettingsRow(
                    icon = Icons.Rounded.Person,
                    title = accountTitle,
                    subtitle = account?.email?.takeIf { it.isNotBlank() }
                        ?: if (signedIn) "Signed in" else "Not signed in",
                    onClick = onAccountScrobbling,
                )
            }
            val listenTogetherTitle = "Listen together"
            row(listenTogetherTitle, "jam", "party", "sync", "friends") {
                SettingsRow(
                    icon = Icons.Rounded.Groups,
                    title = listenTogetherTitle,
                    subtitle = "Start or join a live listening party with friends",
                    onClick = onListenTogether,
                )
            }
            val developerAndLyraTitle = "Informasi Developer & LYRA"
            row(developerAndLyraTitle, "developer", "lyra", "lyrafy", "dinz", "filosofi", "tentang") {
                SettingsRow(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = developerAndLyraTitle,
                            tint = Color(0xFFE2B963),
                            modifier = Modifier.size(ICON_SIZE),
                        )
                    },
                    title = developerAndLyraTitle,
                    subtitle = "Profil pengembang, filosofi & makna di balik Lyrafy",
                    onClick = onOpenDeveloperAndLyra,
                )
            }
        }

        SearchableSettingsGroup(search, header = "AUDIO QUALITY") {
            val sourceTitle = "Sources"
            val sourceSubtitle = "Addons, JioSaavn, WebDAV, SMB"
            row(sourceTitle, sourceSubtitle, "addon", "lossless") {
                SettingsRow(
                    icon = Icons.Rounded.Extension,
                    title = sourceTitle,
                    subtitle = sourceSubtitle,
                    onClick = onSources,
                )
            }
            val onWifiTitle = "On Wi-Fi"
            row(onWifiTitle, "wi-fi", "streaming quality") {
                SettingsRow(
                    icon = Icons.Rounded.Wifi,
                    title = onWifiTitle,
                    badge = "IN USE".takeIf { metered != true },
                    value = wifiQuality.label,
                    onClick = { picking = QualityTarget.WIFI },
                )
            }
            val onMobileDataTitle = "On mobile data"
            row(onMobileDataTitle, "cellular", "streaming quality") {
                SettingsRow(
                    icon = Icons.Rounded.SignalCellularAlt,
                    title = onMobileDataTitle,
                    badge = "IN USE".takeIf { metered == true },
                    value = cellularQuality.label,
                    onClick = { picking = QualityTarget.CELLULAR },
                )
            }
            val dolbyAtmosTitle = "Dolby Atmos"
            row(dolbyAtmosTitle, "surround", "e-ac-3") {
                SettingsRow(
                    leadingContent = { DolbyAtmosGlyph(modifier = Modifier.size(ICON_SIZE)) },
                    title = dolbyAtmosTitle,
                    subtitle = "Play Dolby Atmos spatial tracks when a source offers them.",
                    enabled = dolbyAtmosSupported,
                    trailing = {
                        Switch(
                            checked = dolbyAtmos && dolbyAtmosSupported,
                            onCheckedChange = AppSettings::setDolbyAtmos,
                            enabled = dolbyAtmosSupported,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setDolbyAtmos(!dolbyAtmos) },
                )
            }
        }

        SearchableSettingsGroup(search, header = "DOWNLOADS") {
            val downloadQualityTitle = "Download quality"
            row(downloadQualityTitle, "offline", "lossless") {
                SettingsRow(
                    icon = Icons.Rounded.Download,
                    title = downloadQualityTitle,
                    subtitle = "${downloadQuality.perTrack} per track",
                    value = downloadQuality.label,
                    onClick = { pickingDownloadQuality = true },
                )
            }
            val downloadWifiOnlyTitle = "Download over Wi-Fi only"
            row(downloadWifiOnlyTitle, "wi-fi", "cellular", divided = false) {
                SettingsSubRow(
                    title = downloadWifiOnlyTitle,
                    checked = wifiOnlyDownloads,
                    onCheckedChange = AppSettings::setWifiOnlyDownloads,
                    badge = "BLOCKING".takeIf { wifiOnlyDownloads && metered == true },
                )
            }
            val exportDownloadsTitle = "Export compatible downloads"
            row(exportDownloadsTitle, "music folder", divided = false) {
                SettingsSubRow(
                    title = exportDownloadsTitle,
                    checked = exportDownloads,
                    onCheckedChange = AppSettings::setExportDownloads,
                    subtitle = "Music/BitChord".takeIf { exportDownloads },
                )
            }
        }

        SearchableSettingsGroup(search, header = "PLAYBACK") {
            val preferMusicOnlyTitle = "Prefer music-only version"
            row(preferMusicOnlyTitle, "video", "audio", "music video") {
                SettingsRow(
                    icon = Icons.Rounded.SmartDisplay,
                    title = preferMusicOnlyTitle,
                    subtitle = "Switch music videos to their official album audio cut",
                    trailing = {
                        Switch(
                            checked = preferMusicOnly,
                            onCheckedChange = AppSettings::setPreferMusicOnly,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setPreferMusicOnly(!preferMusicOnly) },
                )
            }
            val outputPrecisionTitle = "Output precision"
            row(outputPrecisionTitle, "pcm", "bit depth", "sample rate", "dac") {
                SettingsRow(
                    icon = Icons.Rounded.GraphicEq,
                    title = outputPrecisionTitle,
                    subtitle = "AudioTrack · Speaker · 48.0 kHz · ${outputPcmMode.label}",
                )
                SegmentedControl(
                    options = OutputPcmMode.entries.map(OutputPcmMode::label),
                    selectedIndex = OutputPcmMode.entries.indexOf(outputPcmMode),
                    onSelect = { AppSettings.setOutputPcmMode(OutputPcmMode.entries[it]) },
                    modifier = Modifier.padding(start = TEXT_INSET, end = ROW_INSET, bottom = 14.dp),
                )
            }
            val preferUsbDacTitle = "Prefer USB DAC"
            row(preferUsbDacTitle, "headphone", "output") {
                SettingsSubRow(
                    title = preferUsbDacTitle,
                    checked = preferUsbDac,
                    onCheckedChange = AppSettings::setPreferUsbDac,
                )
            }
            val loudnessTitle = "Loudness normalization"
            row(loudnessTitle, "loudness", "volume", "normalize", "replaygain", "lufs") {
                SettingsRow(
                    icon = Icons.Rounded.VolumeUp,
                    title = loudnessTitle,
                    subtitle = "Play songs at a consistent volume level",
                    trailing = {
                        Switch(
                            checked = loudnessNormalization,
                            onCheckedChange = AppSettings::setLoudnessNormalization,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setLoudnessNormalization(!loudnessNormalization) },
                )
            }
            val loudnessSpeakerTitle = "Disable on Phone Speaker"
            row(loudnessSpeakerTitle, "loudness", "speaker", "normalize") {
                SettingsSubRow(
                    title = loudnessSpeakerTitle,
                    checked = loudnessOffOnSpeaker,
                    onCheckedChange = AppSettings::setLoudnessOffOnSpeaker,
                )
            }
            if (!smartFade) {
                val crossfadeTitle = "Crossfade"
                row(crossfadeTitle, "fade", "gapless") {
                    SliderRow(
                        icon = Icons.Rounded.Waves,
                        title = crossfadeTitle,
                        subtitle = "Smooth transition between songs",
                        value = if (crossfade == 0) "Off" else "${crossfade}s",
                        sliderValue = crossfade.toFloat(),
                        onSliderValue = { AppSettings.setCrossfadeSeconds(it.roundToInt()) },
                        valueRange = 0f..12f,
                        steps = 11,
                    )
                }
            }
            val automixTitle = "Automix"
            row(automixTitle, "crossfade", "smart fade", "mix") {
                SettingsRow(
                    icon = Icons.Rounded.AutoAwesome,
                    title = automixTitle,
                    badge = "BETA",
                    subtitle = if (smartFade) {
                        "Matching tempo, beats and vocal phrasing between tracks"
                    } else {
                        "Intelligent DJ transitions timed to beats and song structure"
                    },
                    trailing = {
                        Switch(
                            checked = smartFade,
                            onCheckedChange = AppSettings::setSmartFadeEnabled,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setSmartFadeEnabled(!smartFade) },
                )
            }
            val automixPerformanceTitle = "Automix performance"
            row(automixPerformanceTitle, "cpu", "battery") {
                SettingsRow(
                    icon = Icons.Rounded.Tune,
                    title = automixPerformanceTitle,
                    subtitle = "CPU budget for background beat and vocal analysis",
                    value = automixPerformance.label,
                    onClick = { pickingAutomixPerformance = true },
                )
            }
            val skipSilenceTitle = "Skip silence"
            row(skipSilenceTitle, "silence") {
                SettingsRow(
                    icon = Icons.AutoMirrored.Rounded.VolumeOff,
                    title = skipSilenceTitle,
                    subtitle = "Automatically skip silent passages in tracks",
                    trailing = {
                        Switch(
                            checked = skipSilence,
                            onCheckedChange = AppSettings::setSkipSilence,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setSkipSilence(!skipSilence) },
                )
            }
            val spatialAudioTitle = "Spatial audio"
            row(spatialAudioTitle, "surround", "3d") {
                SettingsRow(
                    icon = Icons.Rounded.SurroundSound,
                    title = spatialAudioTitle,
                    subtitle = "Widen stereo soundstage with cross-feed processing",
                    trailing = {
                        Switch(
                            checked = spatialAudio,
                            onCheckedChange = AppSettings::setSpatialAudio,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setSpatialAudio(!spatialAudio) },
                )
            }
            val equalizerTitle = "Equalizer"
            row(equalizerTitle, "eq", "bass", "treble") {
                SettingsRow(
                    icon = Icons.Rounded.Tune,
                    title = equalizerTitle,
                    subtitle = "Dynamic tone pad, 7-band EQ and stereo balance",
                    onClick = onEqualizer,
                )
            }
        }

        SearchableSettingsGroup(search, header = "APPEARANCE") {
            val themeTitle = "Theme"
            row(themeTitle, "dark mode", "light mode") {
                SettingsRow(icon = Icons.Rounded.Brightness4, title = themeTitle)
                SegmentedControl(
                    options = ThemeMode.entries.map { it.label },
                    selectedIndex = ThemeMode.entries.indexOf(theme),
                    onSelect = { AppSettings.setThemeMode(ThemeMode.entries[it]) },
                    modifier = Modifier.padding(start = ROW_INSET, end = ROW_INSET, bottom = 14.dp),
                )
            }
            val reduceAnimationTitle = "Reduce animation"
            row(reduceAnimationTitle, "motion") {
                SettingsRow(
                    icon = Icons.Rounded.MotionPhotosOff,
                    title = reduceAnimationTitle,
                    subtitle = "Freeze mesh gradient motion and transitions",
                    trailing = {
                        Switch(
                            checked = reduceAnimation,
                            onCheckedChange = AppSettings::setReduceAnimation,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setReduceAnimation(!reduceAnimation) },
                )
            }
            val reduceDynamicBlurTitle = "Reduce dynamic blur"
            row(reduceDynamicBlurTitle, "blur", "performance") {
                SettingsRow(
                    icon = Icons.Rounded.BlurOff,
                    title = reduceDynamicBlurTitle,
                    subtitle = "Replace live frosted blur with solid dark surfaces",
                    trailing = {
                        Switch(
                            checked = reduceDynamicBlur,
                            onCheckedChange = AppSettings::setReduceDynamicBlur,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setReduceDynamicBlur(!reduceDynamicBlur) },
                )
            }
            val liquidGlassTitle = "Liquid Glass"
            row(liquidGlassTitle, "glass", "blur", "navbar", "refraction") {
                SettingsRow(
                    icon = Icons.Rounded.AutoAwesome,
                    title = liquidGlassTitle,
                    subtitle = if (liquidGlassSupported) {
                        "Real refracting glass on the floating tab bar. Android 13+ only; uses more GPU."
                    } else {
                        "Requires Android 12 or newer."
                    },
                    enabled = liquidGlassSupported,
                    trailing = {
                        Switch(
                            checked = liquidGlass,
                            onCheckedChange = AppSettings::setLiquidGlass,
                            enabled = liquidGlassSupported,
                            colors = switchColors,
                            modifier = Modifier.testTag("liquid_glass_toggle"),
                        )
                    },
                    onClick = { AppSettings.setLiquidGlass(!liquidGlass) },
                )
            }
            val fullLiquidGlassModeTitle = "Full Liquid Glass Mode"
            row(fullLiquidGlassModeTitle, "glass", "glassmorphism", "liquid", "kaca", "ui") {
                SettingsRow(
                    icon = Icons.Rounded.AutoAwesome,
                    title = fullLiquidGlassModeTitle,
                    subtitle = "Terapkan efek kaca transparan (glassmorphism) pada seluruh kartu & komponen UI aplikasi",
                    trailing = {
                        Switch(
                            checked = fullLiquidGlassUi,
                            onCheckedChange = AppSettings::setFullLiquidGlassUi,
                            colors = switchColors,
                            modifier = Modifier.testTag("full_liquid_glass_mode_toggle"),
                        )
                    },
                    onClick = { AppSettings.setFullLiquidGlassUi(!fullLiquidGlassUi) },
                )
            }
            val fullScreenCoverArtTitle = "Full-screen cover art"
            row(fullScreenCoverArtTitle, "artwork", "player", "square", "kotak") {
                SettingsRow(
                    icon = Icons.Rounded.Fullscreen,
                    title = fullScreenCoverArtTitle,
                    subtitle = if (fullBleedArtwork) {
                        "Menampilkan sampul lagu berbentuk kotak persegi (1:1) di tengah layar pemutar"
                    } else {
                        "Menampilkan sampul lagu penuh dari ujung ke ujung (Full-bleed default)"
                    },
                    trailing = {
                        Switch(
                            checked = fullBleedArtwork,
                            onCheckedChange = AppSettings::setFullBleedArtwork,
                            colors = switchColors,
                            modifier = Modifier.testTag("full_screen_cover_art_toggle"),
                        )
                    },
                    onClick = { AppSettings.setFullBleedArtwork(!fullBleedArtwork) },
                )
            }
            val legacyMeshGradientTitle = "Legacy mesh gradient"
            row(legacyMeshGradientTitle, "background", "player") {
                SettingsRow(
                    icon = Icons.Rounded.Gradient,
                    title = legacyMeshGradientTitle,
                    subtitle = "Use drifting four-colour blobs behind the full player",
                    trailing = {
                        Switch(
                            checked = legacyMeshGradient,
                            onCheckedChange = AppSettings::setLegacyMeshGradient,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setLegacyMeshGradient(!legacyMeshGradient) },
                )
            }
            val animatedCoverArtTitle = "Animated cover art"
            row(animatedCoverArtTitle, "canvas", "video", "spotify") {
                SettingsRow(
                    icon = Icons.Rounded.Animation,
                    title = animatedCoverArtTitle,
                    subtitle = "Play looping motion artwork and Spotify Canvas on the player",
                    trailing = {
                        Switch(
                            checked = animatedCanvas,
                            onCheckedChange = AppSettings::setAnimatedCanvas,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setAnimatedCanvas(!animatedCanvas) },
                )
            }
            if (animatedCanvas) {
                val coverCellularTitle = "Play animated cover over cellular"
                row(coverCellularTitle, "canvas", "cellular", divided = false) {
                    SettingsSubRow(
                        title = coverCellularTitle,
                        checked = canvasOverCellular,
                        onCheckedChange = AppSettings::setCanvasOverCellular,
                    )
                }
                val spotifyCanvasTitle = "Set up Spotify Canvas"
                row(spotifyCanvasTitle, "spotify", "canvas", divided = false) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onSpotifyCanvasAuth)
                            .padding(start = ROW_INSET, end = ROW_INSET, top = 4.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = spotifyCanvasTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Chevron()
                    }
                }
            }
            val artistSignaturesTitle = "Artist signatures"
            row(artistSignaturesTitle, "signature", "artist", "logo") {
                SettingsRow(
                    icon = Icons.Rounded.Draw,
                    title = artistSignaturesTitle,
                    subtitle = "Show artist title logos on the player",
                    trailing = {
                        Switch(
                            checked = signatureApiEnabled,
                            onCheckedChange = AppSettings::setSignatureApiEnabled,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setSignatureApiEnabled(!signatureApiEnabled) },
                )
            }
            if (signatureApiEnabled) {
                val signatureUrlTitle = "Artist signature API URL"
                row(signatureUrlTitle, "signature", "api", "url", divided = false) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSignatureUrlDialog = true
                                onEditSignatureApi()
                            }
                            .padding(start = ROW_INSET, end = ROW_INSET, top = 4.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = signatureApiUrl.ifBlank { "Configure signature API URL" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Chevron()
                    }
                }
            }
            val useBitChordHomeUiTitle = "Gunakan UI Beranda BitChord"
            row(useBitChordHomeUiTitle, "bitchord", "home", "ui", "beranda") {
                SettingsRow(
                    icon = Icons.Rounded.AutoAwesome,
                    title = useBitChordHomeUiTitle,
                    subtitle = "Mengganti seluruh tampilan layar Home dengan HomeScreen.kt asli dari source code BitChord",
                    trailing = {
                        Switch(
                            checked = useBitChordHomeUi,
                            onCheckedChange = AppSettings::setUseBitChordHomeUi,
                            colors = switchColors,
                            modifier = Modifier.testTag("use_bitchord_home_ui_toggle"),
                        )
                    },
                    onClick = { AppSettings.setUseBitChordHomeUi(!useBitChordHomeUi) },
                )
            }
            val useNewQuickPicksGridTitle = "Desain Baru Pintasan Cepat"
            row(useNewQuickPicksGridTitle, "pintasan", "cepat", "grid", "desain") {
                SettingsRow(
                    icon = Icons.Rounded.AutoAwesome,
                    title = useNewQuickPicksGridTitle,
                    subtitle = "Mengaktifkan desain grid 3x3 pager pada Panggilan/Pintasan Cepat",
                    trailing = {
                        Switch(
                            checked = useNewQuickPicksGrid,
                            onCheckedChange = AppSettings::setUseNewQuickPicksGrid,
                            colors = switchColors,
                            modifier = Modifier.testTag("use_new_quick_picks_grid_toggle"),
                        )
                    },
                    onClick = { AppSettings.setUseNewQuickPicksGrid(!useNewQuickPicksGrid) },
                )
            }
            val useNewSongShelvesDesignTitle = "Desain Baru Daftar Lagu Beranda"
            row(useNewSongShelvesDesignTitle, "beranda", "pilihan cepat", "trending", "cover", "daftar lagu") {
                SettingsRow(
                    icon = Icons.Rounded.PlaylistPlay,
                    title = useNewSongShelvesDesignTitle,
                    subtitle = "Mengaktifkan desain baru bergaris pembatas pada Pilihan Cepat, Lagu Trending, Cover & Remix",
                    trailing = {
                        Switch(
                            checked = useNewSongShelvesDesign,
                            onCheckedChange = AppSettings::setUseNewSongShelvesDesign,
                            colors = switchColors,
                            modifier = Modifier.testTag("use_new_song_shelves_design_toggle"),
                        )
                    },
                    onClick = { AppSettings.setUseNewSongShelvesDesign(!useNewSongShelvesDesign) },
                )
            }
            val useNewCommunityDesignTitle = "Desain Baru Dari Komunitas"
            row(useNewCommunityDesignTitle, "komunitas", "community", "playlist", "desain") {
                SettingsRow(
                    icon = Icons.Rounded.Groups,
                    title = useNewCommunityDesignTitle,
                    subtitle = "Mengaktifkan desain kartu besar bergaris pada seksi Dari Komunitas",
                    trailing = {
                        Switch(
                            checked = useNewCommunityDesign,
                            onCheckedChange = AppSettings::setUseNewCommunityDesign,
                            colors = switchColors,
                            modifier = Modifier.testTag("use_new_community_design_toggle"),
                        )
                    },
                    onClick = { AppSettings.setUseNewCommunityDesign(!useNewCommunityDesign) },
                )
            }
            val useDynamicMusicThemeTitle = "Warna Dinamis Sesuai Musik"
            row(useDynamicMusicThemeTitle, "warna", "dinamis", "sampul", "musik", "tema") {
                SettingsRow(
                    icon = Icons.Rounded.Gradient,
                    title = useDynamicMusicThemeTitle,
                    subtitle = "Menyesuaikan gradien warna latar dan bilah aplikasi dengan sampul lagu yang diputar",
                    trailing = {
                        Switch(
                            checked = useDynamicMusicTheme,
                            onCheckedChange = AppSettings::setUseDynamicMusicTheme,
                            colors = switchColors,
                            modifier = Modifier.testTag("use_dynamic_music_theme_toggle"),
                        )
                    },
                    onClick = { AppSettings.setUseDynamicMusicTheme(!useDynamicMusicTheme) },
                )
            }
            val syncedLyricsTitle = "Synced lyrics"
            row(syncedLyricsTitle, "lyrics", "translation", "lrclib", "musixmatch") {
                SettingsRow(
                    icon = Icons.AutoMirrored.Rounded.Notes,
                    title = syncedLyricsTitle,
                    subtitle = "Time-synced word and line lyrics on the player",
                    trailing = {
                        Switch(
                            checked = syncedLyrics,
                            onCheckedChange = AppSettings::setSyncedLyrics,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setSyncedLyrics(!syncedLyrics) },
                )
            }
            if (syncedLyrics) {
                val lyricsBlurTitle = "Blur unfocused lyrics"
                row(lyricsBlurTitle, "lyrics", "blur") {
                    SettingsRow(
                        icon = Icons.Rounded.BlurOn,
                        title = lyricsBlurTitle,
                        subtitle = "Soften upcoming and passed lines so the active line stands out",
                        trailing = {
                            Switch(
                                checked = lyricsBlur,
                                onCheckedChange = AppSettings::setLyricsBlur,
                                colors = switchColors,
                            )
                        },
                        onClick = { AppSettings.setLyricsBlur(!lyricsBlur) },
                    )
                }
                val lyricsSourcesTitle = "Lyrics sources"
                row(lyricsSourcesTitle, "lyrics", "lrclib", "musixmatch") {
                    SettingsRow(
                        icon = Icons.Rounded.Language,
                        title = lyricsSourcesTitle,
                        subtitle = lyricsSources
                            .sortedBy { it.ordinal }
                            .joinToString(", ") { it.label }
                            .ifEmpty { "No lyrics sources enabled" },
                        trailing = { Chevron() },
                        onClick = onLyricsSources,
                    )
                }
                val translationLanguageTitle = "Translation language"
                row(translationLanguageTitle, "lyrics", "translate") {
                    SettingsRow(
                        icon = Icons.Rounded.Translate,
                        title = translationLanguageTitle,
                        subtitle = if (translationLanguage.isBlank()) {
                            "Same as app language"
                        } else {
                            translationLanguageName(translationLanguage)
                        },
                        trailing = { Chevron() },
                        onClick = onTranslationLanguage,
                    )
                }
            }
        }

        SearchableSettingsGroup(search, header = "PERFORMANCE") {
            val highPerformanceModeTitle = "High performance mode"
            row(highPerformanceModeTitle, "frame rate", "refresh rate", "hz", "battery", "smooth") {
                SettingsRow(
                    icon = BitChordIcons.Performance,
                    title = highPerformanceModeTitle,
                    subtitle = if (highPerformanceMode) {
                        "Sustained ${performanceRefreshRate}Hz refresh rate active"
                    } else {
                        "Keep UI locked at high refresh rate"
                    },
                    trailing = {
                        Switch(
                            checked = highPerformanceMode,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPerformanceWarning = true
                                } else {
                                    AppSettings.setHighPerformanceMode(false)
                                }
                            },
                            colors = switchColors,
                        )
                    },
                    onClick = {
                        if (highPerformanceMode) {
                            AppSettings.setHighPerformanceMode(false)
                        } else {
                            showPerformanceWarning = true
                        }
                    },
                )
            }
            if (highPerformanceMode) {
                val refreshRateTitle = "Refresh rate"
                row(refreshRateTitle, "hz", "frame rate") {
                    SettingsRow(
                        icon = BitChordIcons.FrameRate,
                        title = refreshRateTitle,
                    )
                    SegmentedControl(
                        options = supportedRefreshRates.map { "$it Hz" },
                        selectedIndex = supportedRefreshRates.indexOf(performanceRefreshRate).coerceAtLeast(0),
                        onSelect = { index ->
                            AppSettings.setPerformanceRefreshRate(supportedRefreshRates[index])
                        },
                        modifier = Modifier.padding(
                            start = TEXT_INSET,
                            end = ROW_INSET,
                            bottom = 14.dp,
                        ),
                    )
                }
            }
        }

        SearchableSettingsGroup(search, header = "MUSIC LIBRARY") {
            val localMusicFolderTitle = "Local music folder"
            row(localMusicFolderTitle, "folder", "offline", "files") {
                SettingsRow(
                    icon = Icons.Rounded.Folder,
                    title = localMusicFolderTitle,
                    subtitle = localMusicFolderUri.takeIf { it.isNotBlank() } ?: "All audio folders",
                    onClick = { localMusicFolderPicker.launch(null) },
                )
            }
            if (localMusicFolderUri.isNotBlank()) {
                val useAllAudioFoldersTitle = "Use all audio folders"
                row(useAllAudioFoldersTitle, "folder") {
                    SettingsRow(
                        icon = Icons.Rounded.LibraryMusic,
                        title = useAllAudioFoldersTitle,
                        subtitle = "Reset folder filter and scan all device music",
                        onClick = { AppSettings.setLocalMusicFolderUri("") },
                    )
                }
            }
            val filterNonMusicAudioTitle = "Filter non-music audio"
            row(filterNonMusicAudioTitle, "podcast", "recording") {
                SettingsRow(
                    icon = Icons.Rounded.FilterAlt,
                    title = filterNonMusicAudioTitle,
                    subtitle = "Hide voice recordings, ringtones and short clips",
                    trailing = {
                        Switch(
                            checked = filterNonMusicAudio,
                            onCheckedChange = AppSettings::setFilterNonMusicAudio,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setFilterNonMusicAudio(!filterNonMusicAudio) },
                )
            }
        }

        val cacheUnlimited = cacheLimitBytes == AppSettings.UNLIMITED_CACHE_LIMIT_BYTES
        val cacheLimitMb = if (cacheUnlimited) CACHE_UNLIMITED_STOP_MB else (cacheLimitBytes / (1024 * 1024)).toInt()
        val cacheLimitLabel = if (cacheUnlimited) "Unlimited" else formatCacheSize(cacheLimitMb)
        SearchableSettingsGroup(search, header = "STORAGE") {
            val showCacheFolderTitle = "Show cached songs folder"
            row(showCacheFolderTitle, "cache", "cached songs", "library", "folder") {
                SettingsRow(
                    icon = Icons.Rounded.Cached,
                    title = showCacheFolderTitle,
                    subtitle = "Show cached streaming tracks in Library",
                    trailing = {
                        Switch(
                            checked = showCacheFolder,
                            onCheckedChange = AppSettings::setShowCacheFolder,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setShowCacheFolder(!showCacheFolder) },
                )
            }
            val songCacheLimitTitle = "Song cache limit"
            row(songCacheLimitTitle, "cache", "space", "unlimited") {
                SliderRow(
                    icon = Icons.Rounded.Storage,
                    title = songCacheLimitTitle,
                    subtitle = when {
                        cacheUnlimited -> "No size ceiling; bounded only by free storage"
                        cacheLimitMb > CACHE_WARNING_MB -> "Up to $cacheLimitLabel reserved for cached tracks"
                        else -> "Disk space reserved for recently streamed songs"
                    },
                    value = cacheLimitLabel,
                    sliderValue = cacheLimitMb.toFloat(),
                    onSliderValue = {
                        val mb = it.roundToInt()
                        if (mb >= CACHE_UNLIMITED_STOP_MB) {
                            AppSettings.setAudioCacheLimitBytes(AppSettings.UNLIMITED_CACHE_LIMIT_BYTES)
                        } else {
                            AppSettings.setAudioCacheLimitBytes(mb.toLong() * 1024L * 1024L)
                        }
                    },
                    valueRange = (AppSettings.DEFAULT_CACHE_LIMIT_BYTES / (1024 * 1024)).toFloat()..
                        CACHE_UNLIMITED_STOP_MB.toFloat(),
                    steps = 19,
                )
            }
            val clearSongCacheTitle = "Clear song cache"
            row(clearSongCacheTitle, "cache", "free space") {
                SettingsRow(
                    icon = Icons.Rounded.DeleteSweep,
                    title = clearSongCacheTitle,
                    subtitle = "Remove cached audio streams to free up storage",
                    onClick = { confirmClearSongCache = true },
                )
            }
            val clearImageCacheTitle = "Clear image cache"
            row(clearImageCacheTitle, "cache", "artwork", "free space") {
                SettingsRow(
                    icon = Icons.Rounded.DeleteSweep,
                    title = clearImageCacheTitle,
                    subtitle = "Remove cached cover artwork from memory and disk",
                    onClick = {
                        val loader = Coil.imageLoader(context)
                        loader.memoryCache?.clear()
                        loader.diskCache?.clear()
                        Toast.makeText(context, "Image cache cleared", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }

        SearchableSettingsGroup(search, header = "YOUR DATA") {
            val replayTitle = "Replay"
            row(replayTitle, "stats", "history", "wrapped") {
                SettingsRow(
                    icon = Icons.Rounded.BarChart,
                    title = replayTitle,
                    subtitle = "Your top songs, artists, albums and listening milestones",
                    onClick = onOpenReplay,
                )
            }
            val workOutGenresTitle = "Work out genres"
            row(workOutGenresTitle, "genre", "replay") {
                SettingsRow(
                    icon = Icons.Rounded.LocalOffer,
                    title = workOutGenresTitle,
                    subtitle = if (replayGenres) {
                        "Look up artist genres for Replay breakdown"
                    } else {
                        "Genre chart disabled in Replay"
                    },
                    trailing = {
                        Switch(
                            checked = replayGenres,
                            onCheckedChange = AppSettings::setReplayGenres,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setReplayGenres(!replayGenres) },
                )
            }
            val exportDataTitle = "Export data"
            row(exportDataTitle, "backup") {
                SettingsRow(
                    icon = Icons.Rounded.FileUpload,
                    title = exportDataTitle,
                    subtitle = "Save listening history and settings to a JSON file",
                    onClick = {
                        Toast.makeText(context, "Settings snapshot exported", Toast.LENGTH_SHORT).show()
                    },
                )
            }
            val importDataTitle = "Import data"
            row(importDataTitle, "backup", "restore") {
                SettingsRow(
                    icon = Icons.Rounded.FileDownload,
                    title = importDataTitle,
                    subtitle = "Restore listening history and settings from backup",
                    onClick = {
                        Toast.makeText(context, "Select a BitChord backup JSON file", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }

        SearchableSettingsGroup(search, header = "MISCELLANEOUS") {
            val playNextOnSwipeTitle = "Play next on swipe"
            row(playNextOnSwipeTitle, "swipe", "queue") {
                SettingsRow(
                    icon = Icons.Rounded.PlaylistPlay,
                    title = playNextOnSwipeTitle,
                    subtitle = if (swipeToPlayNext) {
                        "Swiping a song row plays it next"
                    } else {
                        "Swiping a song row adds it to the end of the queue"
                    },
                    trailing = {
                        Switch(
                            checked = swipeToPlayNext,
                            onCheckedChange = AppSettings::setSwipeToPlayNext,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setSwipeToPlayNext(!swipeToPlayNext) },
                )
            }
            val dontRepeatSongsTitle = "Don't repeat songs"
            row(dontRepeatSongsTitle, "radio", "suggestions") {
                SettingsRow(
                    icon = Icons.Rounded.History,
                    title = dontRepeatSongsTitle,
                    subtitle = "Avoid repeating already played tracks in AutoPlay",
                    trailing = {
                        Switch(
                            checked = dontRepeatSuggestions,
                            onCheckedChange = AppSettings::setDontRepeatSuggestions,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setDontRepeatSuggestions(!dontRepeatSuggestions) },
                )
            }
            val stopMusicOnCloseTitle = "Stop music on close"
            row(stopMusicOnCloseTitle, "notification", "background") {
                SettingsRow(
                    icon = Icons.Rounded.MusicOff,
                    title = stopMusicOnCloseTitle,
                    subtitle = "Stop playback when swiping the app away from recents",
                    trailing = {
                        Switch(
                            checked = stopOnTaskRemoved,
                            onCheckedChange = AppSettings::setStopOnTaskRemoved,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setStopOnTaskRemoved(!stopOnTaskRemoved) },
                )
            }
            val hideVolumeBarTitle = "Hide volume bar"
            row(hideVolumeBarTitle, "volume", "player") {
                SettingsRow(
                    icon = Icons.Rounded.VolumeOff,
                    title = hideVolumeBarTitle,
                    subtitle = "Hide the bottom volume slider on the full player",
                    trailing = {
                        Switch(
                            checked = hideVolumeBar,
                            onCheckedChange = AppSettings::setHideVolumeBar,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setHideVolumeBar(!hideVolumeBar) },
                )
            }
            val hideStatusBarTitle = "Hide status bar in player"
            row(hideStatusBarTitle, "status bar", "immersive", "fullscreen", "player") {
                SettingsRow(
                    icon = Icons.Rounded.WebAssetOff,
                    title = hideStatusBarTitle,
                    subtitle = "Hide system clock and battery icons while the player is open",
                    trailing = {
                        Switch(
                            checked = hideStatusBarInPlayer,
                            onCheckedChange = AppSettings::setHideStatusBarInPlayer,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setHideStatusBarInPlayer(!hideStatusBarInPlayer) },
                )
            }
            val showQueueHistoryTitle = "Show queue history"
            row(showQueueHistoryTitle, "player", "queue", "history", "previously played") {
                SettingsRow(
                    icon = Icons.Rounded.History,
                    title = showQueueHistoryTitle,
                    subtitle = "Include previously played songs above the queue",
                    trailing = {
                        Switch(
                            checked = showQueueHistory,
                            onCheckedChange = AppSettings::setShowQueueHistory,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setShowQueueHistory(!showQueueHistory) },
                )
            }
            val hideSongStatusTitle = "Hide song status"
            row(hideSongStatusTitle, "player", "playing from", "played by") {
                SettingsRow(
                    icon = Icons.Rounded.VisibilityOff,
                    title = hideSongStatusTitle,
                    subtitle = "Hide the 'Playing from' caption at the top of the player",
                    trailing = {
                        Switch(
                            checked = hideSongStatus,
                            onCheckedChange = AppSettings::setHideSongStatus,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setHideSongStatus(!hideSongStatus) },
                )
            }
        }

        SearchableSettingsGroup(search, header = "LANGUAGE") {
            val appLanguageTitle = "App language"
            row(appLanguageTitle, "locale", "translate") {
                SettingsRow(
                    icon = Icons.Rounded.Language,
                    title = appLanguageTitle,
                    subtitle = Locale.getDefault().displayLanguage.replaceFirstChar { it.uppercase() },
                    onClick = onAppLanguage,
                )
            }
        }

        SearchableSettingsGroup(search, header = "ADVANCED OPTIONS") {
            val smartAlignmentTitle = "Smart version alignment"
            row(smartAlignmentTitle, "alignment", "sync", "waveform", "video", "audio", "skit", "intro") {
                SettingsRow(
                    icon = Icons.Rounded.GraphicEq,
                    title = smartAlignmentTitle,
                    subtitle = "Align audio waveform when switching between song and video cuts",
                    trailing = {
                        Switch(
                            checked = smartVersionAlignment,
                            onCheckedChange = AppSettings::setSmartVersionAlignment,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setSmartVersionAlignment(!smartVersionAlignment) },
                )
            }
            val showNerdStatsTitle = "Show stats for nerds"
            row(showNerdStatsTitle, "debug", "bitrate", "codec") {
                SettingsRow(
                    icon = Icons.Rounded.BarChart,
                    title = showNerdStatsTitle,
                    subtitle = "Show codec, bitrate and sample rate badge on the player",
                    trailing = {
                        Switch(
                            checked = nerdStats,
                            onCheckedChange = AppSettings::setShowNerdStats,
                            colors = switchColors,
                        )
                    },
                    onClick = { AppSettings.setShowNerdStats(!nerdStats) },
                )
            }
            val checkUpdatesTitle = "Software Update"
            row(checkUpdatesTitle, "update", "version", "github", "linefy", "pembaruan") {
                SettingsRow(
                    icon = Icons.Rounded.Download,
                    title = checkUpdatesTitle,
                    badge = "NEW".takeIf { hasNewerRelease },
                    subtitle = when {
                        isCheckingUpdate -> "Memeriksa pembaruan dari GitHub dinzid04/Linefy..."
                        hasNewerRelease -> "Versi baru tersedia! Ketuk untuk melihat changelog & mengunduh"
                        else -> "Cek pembaruan rilis & README terbaru dari GitHub (v$version)"
                    },
                    onClick = {
                        AppUpdateChecker.checkForUpdates(manualTrigger = true)
                    },
                )
            }
        }

        if (!search.anyMatch) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No settings matching \"$searchQuery\"",
                    color = Color(0xFF8E8E93),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (searchQuery.isBlank()) {
            Text(
                text = buildAnnotatedString {
                    append("Lyrafy $version  ")
                    val linkStyles = TextLinkStyles(
                        style = SpanStyle(
                            color = Color(0xFFE2B963),
                            textDecoration = TextDecoration.Underline,
                        ),
                    )
                    withLink(LinkAnnotation.Url("https://github.com/dinzid04/Linefy.git", linkStyles)) {
                        append("GitHub")
                    }
                    append("  ")
                    withLink(LinkAnnotation.Url("https://github.com/dinzid04", linkStyles)) {
                        append("Developer")
                    }
                    append("  ")
                    withLink(LinkAnnotation.Url("https://x.com/lyrafy", linkStyles)) {
                        append("X")
                    }
                    append("\n~Lyrafy (Lyra - Fy) • Music for a brighter you")
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8E8E93),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
            )
        }
    }

    picking?.let { target ->
        ModalBottomSheet(
            onDismissRequest = { picking = null },
            containerColor = Color.Black,
        ) {
            QualitySheet(
                target = target,
                selected = when (target) {
                    QualityTarget.WIFI -> wifiQuality
                    QualityTarget.CELLULAR -> cellularQuality
                },
                onSelect = { quality ->
                    when (target) {
                        QualityTarget.WIFI -> AppSettings.setAudioQualityWifi(quality)
                        QualityTarget.CELLULAR -> AppSettings.setAudioQualityCellular(quality)
                    }
                    picking = null
                },
            )
        }
    }

    if (pickingDownloadQuality) {
        ModalBottomSheet(
            onDismissRequest = { pickingDownloadQuality = false },
            containerColor = Color.Black,
        ) {
            DownloadQualitySheet(
                selected = downloadQuality,
                onSelect = { quality ->
                    AppSettings.setDownloadQuality(quality)
                    pickingDownloadQuality = false
                },
            )
        }
    }

    if (pickingAutomixPerformance) {
        ModalBottomSheet(
            onDismissRequest = { pickingAutomixPerformance = false },
            containerColor = Color.Black,
        ) {
            AutomixPerformanceSheet(
                selected = automixPerformance,
                onSelect = { mode ->
                    AppSettings.setAutomixPerformanceMode(mode)
                    pickingAutomixPerformance = false
                },
            )
        }
    }

    if (confirmClearSongCache) {
        AlertDialog(
            onDismissRequest = { confirmClearSongCache = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Clear song cache?", color = Color.White) },
            text = { Text("Cached songs will be removed from device storage.", color = Color(0xFF8E8E93)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmClearSongCache = false
                    Toast.makeText(context, "Song cache cleared", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Clear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearSongCache = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            },
        )
    }

    if (showPerformanceWarning) {
        AlertDialog(
            onDismissRequest = { showPerformanceWarning = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Before enabling High Performance Mode", color = Color.White) },
            text = {
                Text(
                    "High performance mode locks the display to its highest refresh rate. Set Battery usage to Unrestricted for the smoothest results.",
                    color = Color(0xFF8E8E93),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPerformanceWarning = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        val settingsIntent = intent.takeIf {
                            it.resolveActivity(context.packageManager) != null
                        } ?: Intent(Settings.ACTION_SETTINGS)
                        batterySettingsLauncher.launch(settingsIntent)
                    },
                ) {
                    Text("Open battery settings", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPerformanceWarning = false
                    AppSettings.setHighPerformanceMode(true)
                }) {
                    Text("Enable now", color = Color.White)
                }
            },
        )
    }

    if (showPerformanceConfirmation) {
        AlertDialog(
            onDismissRequest = { showPerformanceConfirmation = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Enable High Performance Mode?", color = Color.White) },
            text = { Text("Keep UI locked to high refresh rate while BitChord is open.", color = Color(0xFF8E8E93)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPerformanceConfirmation = false
                        AppSettings.setHighPerformanceMode(true)
                    },
                ) {
                    Text("Enable", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPerformanceConfirmation = false }) {
                    Text("Not yet", color = Color(0xFF8E8E93))
                }
            },
        )
    }

    if (showSignatureUrlDialog) {
        var urlInput by remember { mutableStateOf(signatureApiUrl) }
        AlertDialog(
            onDismissRequest = { showSignatureUrlDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Artist signature API URL", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    AppSettings.setSignatureApiUrl(urlInput)
                    showSignatureUrlDialog = false
                }) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignatureUrlDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            },
        )
    }
}

@Composable
private fun SettingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF1C1C1E))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = Color(0xFF8E8E93),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color(0xFF8E8E93),
                    fontSize = 16.sp,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3A3C))
                    .clickable { onQueryChange("") },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Clear",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
}

private enum class QualityTarget(val icon: ImageVector, val title: String) {
    WIFI(Icons.Rounded.Wifi, "Wi-Fi"),
    CELLULAR(Icons.Rounded.SignalCellularAlt, "mobile data"),
}

internal fun openEqualizer(context: Context, sessionId: Int) {
    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
    }
    runCatching { context.startActivity(intent) }.onFailure {
        Toast.makeText(context, "No system equalizer found on this device", Toast.LENGTH_SHORT).show()
    }
}

private const val CACHE_WARNING_MB = 2048
private const val CACHE_UNLIMITED_STOP_MB = 10 * 1024 + 512

private fun formatCacheSize(mb: Int): String {
    if (mb < 1024) return "$mb MB"
    val gb = mb / 1024f
    return if (gb == gb.toInt().toFloat()) "${gb.toInt()} GB" else "%.1f GB".format(Locale.ROOT, gb)
}

@Composable
internal fun AccountCard(
    signedIn: Boolean,
    account: Account?,
    onSignIn: () -> Unit,
    onClick: (() -> Unit)? = null,
    grouped: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (grouped) {
                    Modifier
                } else {
                    Modifier
                        .padding(horizontal = GROUP_INSET)
                        .clip(GroupShape)
                        .background(Color(0xFF1C1C1E))
                },
            )
            .then(
                when {
                    signedIn && onClick != null -> Modifier.clickable(onClick = onClick)
                    !signedIn -> Modifier.clickable(onClick = onSignIn)
                    else -> Modifier
                },
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (account?.thumbnailUrl != null) {
            AsyncImage(
                model = account.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(0.5.dp, Color.White.copy(alpha = 0.14f), CircleShape),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = null,
                    tint = Color(0xFF8E8E93),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = account?.name ?: if (signedIn) "Signed in" else "Not signed in",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.W700,
                    fontSize = 20.sp,
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = account?.email?.takeIf { it.isNotBlank() }
                    ?: if (signedIn) "YouTube Music account" else "Tap to sign in with Google",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8E8E93),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!signedIn) {
            Spacer(Modifier.width(8.dp))
            Chevron()
        }
    }
}

@Composable
private fun QualitySheet(
    target: QualityTarget,
    selected: AudioQuality,
    onSelect: (AudioQuality) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = target.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "Audio quality",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Text(
                    text = "Streaming on ${target.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8E8E93),
                )
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF2C2C2E))

        AudioQuality.entries.reversed().forEach { quality ->
            val chosen = quality == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(quality)
                    }
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = quality.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                    Text(
                        text = "${quality.detail} · ${quality.hourly}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E8E93),
                    )
                }
                if (chosen) {
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AutomixPerformanceSheet(
    selected: AutomixPerformanceMode,
    onSelect: (AutomixPerformanceMode) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "Automix performance",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Text(
                    text = "Controls background CPU usage for beat and vocal analysis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8E8E93),
                )
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF2C2C2E))
        AutomixPerformanceMode.entries.forEach { mode ->
            val chosen = mode == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(mode)
                    }
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                    Text(
                        text = mode.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E8E93),
                    )
                }
                if (chosen) {
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadQualitySheet(
    selected: DownloadQuality,
    onSelect: (DownloadQuality) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Download,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "Download quality",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Text(
                    text = "Quality kept for offline playback",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8E8E93),
                )
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF2C2C2E))

        DownloadQuality.entries.reversed().forEach { quality ->
            val chosen = quality == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(quality)
                    }
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = quality.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                    Text(
                        text = "${quality.detail} · ${quality.perTrack}/track",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E8E93),
                    )
                }
                if (chosen) {
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

// ---- Building blocks --------------------------------------------------------

internal val GroupShape = RoundedCornerShape(14.dp)
internal val GROUP_INSET = 16.dp
internal val ROW_INSET = 16.dp
internal val ICON_SIZE = 22.dp
internal val ICON_GAP = 14.dp
internal val TEXT_INSET = ROW_INSET + ICON_SIZE + ICON_GAP

private class SettingsSearch(query: String) {
    private val needle = query.trim()

    var anyMatch = false
        private set

    fun matches(vararg keywords: String?): Boolean {
        val hit = needle.isEmpty() ||
            keywords.any { it?.contains(needle, ignoreCase = true) == true }
        if (hit) anyMatch = true
        return hit
    }
}

private class SettingsGroupScope(
    private val search: SettingsSearch,
    private val header: String?,
) {
    class Entry(val divided: Boolean, val content: @Composable () -> Unit)

    val entries = mutableListOf<Entry>()

    fun row(
        vararg keywords: String?,
        divided: Boolean = true,
        content: @Composable () -> Unit,
    ) {
        if (search.matches(header, *keywords)) entries += Entry(divided, content)
    }
}

@Composable
private fun SearchableSettingsGroup(
    search: SettingsSearch,
    header: String? = null,
    footer: String? = null,
    topSpacing: Dp = 26.dp,
    content: @Composable SettingsGroupScope.() -> Unit,
) {
    val scope = SettingsGroupScope(search, header)
    scope.content()
    if (scope.entries.isEmpty()) return
    SettingsGroup(header = header, footer = footer, topSpacing = topSpacing) {
        scope.entries.forEachIndexed { index, entry ->
            when {
                index > 0 && entry.divided -> RowDivider()
                index == 0 && !entry.divided -> Spacer(Modifier.height(10.dp))
            }
            entry.content()
        }
    }
}

@Composable
internal fun SettingsGroup(
    header: String? = null,
    footer: String? = null,
    topSpacing: Dp = 26.dp,
    content: @Composable () -> Unit,
) {
    if (header != null) {
        Text(
            text = header.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.W600,
                fontSize = 11.sp,
            ),
            color = Color(0xFF8E8E93),
            modifier = Modifier.padding(
                start = GROUP_INSET + 4.dp,
                end = GROUP_INSET,
                top = 26.dp,
                bottom = 8.dp,
            ),
        )
    } else {
        Spacer(Modifier.height(topSpacing))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GROUP_INSET)
            .clip(GroupShape)
            .background(Color(0xFF1C1C1E)),
    ) {
        content()
    }
    if (footer != null) {
        Text(
            text = footer,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = Color(0xFF8E8E93),
            modifier = Modifier.padding(
                start = GROUP_INSET + 4.dp,
                end = GROUP_INSET + 4.dp,
                top = 8.dp,
            ),
        )
    }
}

@Composable
internal fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = TEXT_INSET),
        thickness = 0.5.dp,
        color = Color(0xFF2C2C2E),
    )
}

@Composable
internal fun SettingsRow(
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    subtitleContent: (@Composable () -> Unit)? = null,
    value: String? = null,
    badge: String? = null,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .alpha(if (enabled) 1f else 0.45f)
            .heightIn(min = 52.dp)
            .padding(horizontal = ROW_INSET, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            leadingContent != null -> leadingContent()
            iconPainter != null -> Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(ICON_SIZE),
            )
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(ICON_SIZE),
            )
        }
        Spacer(Modifier.width(ICON_GAP))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (badge != null) {
                    Spacer(Modifier.width(8.dp))
                    Badge(badge)
                }
            }
            if (subtitleContent != null) {
                subtitleContent()
            } else if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = Color(0xFF8E8E93),
                    maxLines = 5,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        if (trailing != null) {
            trailing()
        } else if (value != null || onClick != null) {
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.5.sp),
                    color = Color(0xFF8E8E93),
                    maxLines = 1,
                )
                Spacer(Modifier.width(4.dp))
            }
            Chevron()
        }
    }
}

@Composable
internal fun SettingsSubRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    badge: String? = null,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(start = ROW_INSET, end = ROW_INSET, top = 0.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (badge != null) {
                    Spacer(Modifier.width(8.dp))
                    Badge(badge)
                }
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = Color(0xFF8E8E93),
                    maxLines = 5,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Color.White,
                checkedThumbColor = Color.Black,
                checkedBorderColor = Color.White,
                uncheckedTrackColor = Color(0xFF2C2C2E),
                uncheckedThumbColor = Color(0xFF8E8E93),
                uncheckedBorderColor = Color(0xFF3A3A3C),
            ),
        )
    }
}

@Composable
internal fun Badge(text: String) {
    Text(
        text = text.uppercase(Locale.ROOT),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.W700,
            fontSize = 10.5.sp,
        ),
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
internal fun Chevron() {
    Icon(
        Icons.Rounded.ChevronRight,
        contentDescription = null,
        tint = Color(0xFF8E8E93).copy(alpha = 0.7f),
        modifier = Modifier.size(20.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SliderRow(
    icon: ImageVector,
    title: String,
    value: String,
    sliderValue: Float,
    onSliderValue: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    subtitle: String? = null,
) {
    val colors = SliderDefaults.colors(
        thumbColor = Color.White,
        activeTrackColor = Color.White,
        inactiveTrackColor = Color(0xFF2C2C2E),
    )
    Column(Modifier.padding(start = ROW_INSET, end = ROW_INSET, top = 12.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(ICON_SIZE),
            )
            Spacer(Modifier.width(ICON_GAP))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = Color.White,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                        color = Color(0xFF8E8E93),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.5.sp),
                color = Color(0xFF8E8E93),
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = onSliderValue,
            valueRange = valueRange,
            steps = steps,
            colors = colors,
            track = { state ->
                SliderDefaults.Track(
                    sliderState = state,
                    colors = colors,
                    drawStopIndicator = null,
                    drawTick = { _, _ -> },
                )
            },
            modifier = Modifier.padding(start = ICON_SIZE + ICON_GAP),
        )
    }
}

@Composable
internal fun DestructiveRow(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.W500,
            ),
            color = Color.White,
        )
    }
}

@Composable
internal fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2C2C2E))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { index, label ->
            val chosen = index == selectedIndex
            val pill by animateColorAsState(
                targetValue = if (chosen) Color.White else Color.Transparent,
                animationSpec = tween(160),
                label = "segmentPill",
            )
            val labelColor by animateColorAsState(
                targetValue = if (chosen) Color.Black else Color(0xFF8E8E93),
                animationSpec = tween(160),
                label = "segmentLabel",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(pill)
                    .clickable(enabled = enabled) {
                        if (!chosen) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelect(index)
                        }
                    }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 12.5.sp,
                    ),
                    color = labelColor,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
internal fun DolbyAtmosGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val leftD = Path().apply {
            moveTo(w * 0.12f, h * 0.22f)
            lineTo(w * 0.28f, h * 0.22f)
            quadraticTo(w * 0.48f, h * 0.22f, w * 0.48f, h * 0.50f)
            quadraticTo(w * 0.48f, h * 0.78f, w * 0.28f, h * 0.78f)
            lineTo(w * 0.12f, h * 0.78f)
            close()
        }
        val rightD = Path().apply {
            moveTo(w * 0.88f, h * 0.22f)
            lineTo(w * 0.72f, h * 0.22f)
            quadraticTo(w * 0.52f, h * 0.22f, w * 0.52f, h * 0.50f)
            quadraticTo(w * 0.52f, h * 0.78f, w * 0.72f, h * 0.78f)
            lineTo(w * 0.88f, h * 0.78f)
            close()
        }
        drawPath(leftD, color = Color.White)
        drawPath(rightD, color = Color.White)
    }
}
