package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.auth.EncryptedPrefs
import com.example.data.lyrics.LyricsSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
}

enum class AudioQuality(
    val label: String,
    val detail: String,
    val hourly: String,
) {
    LOW("Low", "AAC · ~48 kbps", "~22 MB/hr"),
    NORMAL("Normal", "AAC / Opus · ~128 kbps", "~58 MB/hr"),
    HIGH("High", "AAC / Opus · ~256 kbps", "~115 MB/hr"),
    LOSSLESS("Lossless", "24-bit / 192 kHz FLAC when available", "~350 MB/hr"),
}

enum class DownloadQuality(
    val label: String,
    val detail: String,
    val perTrack: String,
) {
    LOW("Low", "HE-AAC · 64 kbps", "~1.5 MB"),
    NORMAL("Normal", "AAC / Opus · 128 kbps", "~3.2 MB"),
    HIGH("High", "AAC / Opus · 256 kbps", "~6.5 MB"),
    LOSSLESS("Lossless", "16-bit / 24-bit FLAC", "~25 MB"),
}

enum class AutomixPerformanceMode(
    val label: String,
    val subtitle: String,
) {
    POWER_SAVER("Power saver", "Light beat detection with minimal battery impact"),
    BALANCED("Balanced", "Full beat & phrase alignment on background threads"),
    MAX_PRECISION("Max precision", "Deep vocal stem & harmonic transition analysis"),
}

enum class AutomixMode(
    val label: String,
    val subtitle: String,
    val windowSec: Int,
    val stemBlend: Float,
) {
    OFF("Off", "Tracks play back-to-back without DJ mixing", 0, 0f),
    SUBTLE("Subtle", "Short 6s tempo-matched transition", 6, 0.35f),
    BALANCED("Balanced", "10s harmonic + beat-aligned DJ transition", 10, 0.65f),
    CLUB("Club", "14s full stem blend & phrase-matched crossfade", 14, 1.0f),
}

enum class OutputPcmMode(val label: String) {
    PCM_16("16-bit PCM"),
    FLOAT_32("32-bit float"),
}

typealias OutputPrecision = OutputPcmMode

enum class EqualizerMode {
    DYNAMIC,
    MANUAL,
}

enum class EqualizerPreset(
    val label: String,
    val gains: List<Float>,
) {
    FLAT("Flat", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)),
    BASS_BOOST("Bass", listOf(5.5f, 4.0f, 1.5f, 0f, 0f, 0.5f, 1.0f)),
    VOCAL("Vocal", listOf(-2.0f, -1.0f, 1.5f, 4.0f, 3.5f, 1.5f, 0f)),
    ACOUSTIC("Acoustic", listOf(2.5f, 2.0f, 0.5f, 1.0f, 2.5f, 3.0f, 2.0f)),
    ELECTRONIC("Electronic", listOf(4.5f, 3.5f, 0f, -1.5f, 1.5f, 3.5f, 4.5f)),
    ROCK("Rock", listOf(4.0f, 2.5f, -1.0f, -0.5f, 2.0f, 3.5f, 4.0f)),
    CLASSICAL("Classical", listOf(3.0f, 2.0f, -1.0f, 0f, 1.0f, 2.5f, 3.5f)),
    CUSTOM("Custom", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)),
}

enum class AudioSourceId(
    val id: String,
    val title: String,
    val subtitle: String,
) {
    TIDAL("tidal", "TIDAL HiFi / Max", "Up to 24-bit/192 kHz FLAC lossless"),
    QOBUZ("qobuz", "Qobuz Studio", "Hi-Res 24-bit FLAC stream"),
    DEEZER("deezer", "Deezer HiFi", "16-bit/44.1 kHz CD FLAC"),
    YOUTUBE("youtube", "YouTube Music", "Opus 251 / AAC 140 stream"),
}

object AppSettings {
    private const val PREFS_FILE = "bitchord_settings_enc"
    private const val LEGACY_PREFS_FILE = "bitchord_settings"
    private lateinit var prefs: SharedPreferences

    const val DEFAULT_CACHE_LIMIT_BYTES: Long = 512L * 1024L * 1024L
    const val UNLIMITED_CACHE_LIMIT_BYTES: Long = -1L

    val scrobblingAvailable: Boolean = true

    val isLiquidGlassSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _reduceAnimation = MutableStateFlow(false)
    val reduceAnimation: StateFlow<Boolean> = _reduceAnimation.asStateFlow()

    private val _reduceDynamicBlur = MutableStateFlow(false)
    val reduceDynamicBlur: StateFlow<Boolean> = _reduceDynamicBlur.asStateFlow()
    val reduceBlur: StateFlow<Boolean> = _reduceDynamicBlur.asStateFlow()

    private val _liquidGlass = MutableStateFlow(false)
    val liquidGlass: StateFlow<Boolean> = _liquidGlass.asStateFlow()

    private val _fullLiquidGlassUi = MutableStateFlow(false)
    val fullLiquidGlassUi: StateFlow<Boolean> = _fullLiquidGlassUi.asStateFlow()

    private val _fullBleedArtwork = MutableStateFlow(false)
    val fullBleedArtwork: StateFlow<Boolean> = _fullBleedArtwork.asStateFlow()
    val fullScreenCoverArt: StateFlow<Boolean> = _fullBleedArtwork.asStateFlow()

    private val _legacyMeshGradient = MutableStateFlow(false)
    val legacyMeshGradient: StateFlow<Boolean> = _legacyMeshGradient.asStateFlow()

    private val _animatedCanvas = MutableStateFlow(true)
    val animatedCanvas: StateFlow<Boolean> = _animatedCanvas.asStateFlow()
    val animatedCoverArt: StateFlow<Boolean> = _animatedCanvas.asStateFlow()

    private val _canvasOverCellular = MutableStateFlow(false)
    val canvasOverCellular: StateFlow<Boolean> = _canvasOverCellular.asStateFlow()
    val animatedCoverCellular: StateFlow<Boolean> = _canvasOverCellular.asStateFlow()

    private val _useBitChordHomeUi = MutableStateFlow(false)
    val useBitChordHomeUi: StateFlow<Boolean> = _useBitChordHomeUi.asStateFlow()

    private val _useNewQuickPicksGrid = MutableStateFlow(false)
    val useNewQuickPicksGrid: StateFlow<Boolean> = _useNewQuickPicksGrid.asStateFlow()
    val quickPicksRedesignEnabled: StateFlow<Boolean> = _useNewQuickPicksGrid.asStateFlow()

    private val _useNewSongShelvesDesign = MutableStateFlow(false)
    val useNewSongShelvesDesign: StateFlow<Boolean> = _useNewSongShelvesDesign.asStateFlow()
    val homeRedesignSectionsEnabled: StateFlow<Boolean> = _useNewSongShelvesDesign.asStateFlow()

    private val _useNewCommunityDesign = MutableStateFlow(false)
    val useNewCommunityDesign: StateFlow<Boolean> = _useNewCommunityDesign.asStateFlow()
    val communitySectionRedesignEnabled: StateFlow<Boolean> = _useNewCommunityDesign.asStateFlow()

    private val _useDynamicMusicTheme = MutableStateFlow(false)
    val useDynamicMusicTheme: StateFlow<Boolean> = _useDynamicMusicTheme.asStateFlow()
    val dynamicAlbumColorTheme: StateFlow<Boolean> = _useDynamicMusicTheme.asStateFlow()

    private val _showMusicRecognitionButton = MutableStateFlow(true)
    val showMusicRecognitionButton: StateFlow<Boolean> = _showMusicRecognitionButton.asStateFlow()

    private val _signatureApiEnabled = MutableStateFlow(false)
    val signatureApiEnabled: StateFlow<Boolean> = _signatureApiEnabled.asStateFlow()

    private val _signatureApiUrl = MutableStateFlow("")
    val signatureApiUrl: StateFlow<String> = _signatureApiUrl.asStateFlow()

    private val _syncedLyrics = MutableStateFlow(true)
    val syncedLyrics: StateFlow<Boolean> = _syncedLyrics.asStateFlow()

    private val _lyricsBlur = MutableStateFlow(true)
    val lyricsBlur: StateFlow<Boolean> = _lyricsBlur.asStateFlow()
    val blurUnfocusedLyrics: StateFlow<Boolean> = _lyricsBlur.asStateFlow()

    private val _lyricsSources = MutableStateFlow(LyricsSource.defaultEnabled)
    val lyricsSources: StateFlow<Set<LyricsSource>> = _lyricsSources.asStateFlow()

    private val _lyricsSourceOrder = MutableStateFlow(LyricsSource.offered)
    val lyricsSourceOrder: StateFlow<List<LyricsSource>> = _lyricsSourceOrder.asStateFlow()

    private val _prioritizeSyllableSync = MutableStateFlow(true)
    val prioritizeSyllableSync: StateFlow<Boolean> = _prioritizeSyllableSync.asStateFlow()

    private val _paxSenixApiKey = MutableStateFlow("")
    val paxSenixApiKey: StateFlow<String> = _paxSenixApiKey.asStateFlow()

    private val _lyricsOffsetMs = MutableStateFlow(0)
    val lyricsOffsetMs: StateFlow<Int> = _lyricsOffsetMs.asStateFlow()

    private val _translationLanguage = MutableStateFlow("id")
    val translationLanguage: StateFlow<String> = _translationLanguage.asStateFlow()

    private val _audioQualityWifi = MutableStateFlow(AudioQuality.LOSSLESS)
    val audioQualityWifi: StateFlow<AudioQuality> = _audioQualityWifi.asStateFlow()
    val wifiQuality: StateFlow<AudioQuality> = _audioQualityWifi.asStateFlow()

    private val _audioQualityCellular = MutableStateFlow(AudioQuality.LOSSLESS)
    val audioQualityCellular: StateFlow<AudioQuality> = _audioQualityCellular.asStateFlow()
    val mobileQuality: StateFlow<AudioQuality> = _audioQualityCellular.asStateFlow()

    private val _meteredConnection = MutableStateFlow<Boolean?>(false)
    val meteredConnection: StateFlow<Boolean?> = _meteredConnection.asStateFlow()

    private val _downloadQuality = MutableStateFlow(DownloadQuality.LOSSLESS)
    val downloadQuality: StateFlow<DownloadQuality> = _downloadQuality.asStateFlow()

    private val _wifiOnlyDownloads = MutableStateFlow(false)
    val wifiOnlyDownloads: StateFlow<Boolean> = _wifiOnlyDownloads.asStateFlow()
    val downloadWifiOnly: StateFlow<Boolean> = _wifiOnlyDownloads.asStateFlow()

    private val _exportDownloads = MutableStateFlow(true)
    val exportDownloads: StateFlow<Boolean> = _exportDownloads.asStateFlow()
    val exportCompatibleDownloads: StateFlow<Boolean> = _exportDownloads.asStateFlow()

    private val _dolbyAtmos = MutableStateFlow(false)
    val dolbyAtmos: StateFlow<Boolean> = _dolbyAtmos.asStateFlow()

    private val _preferMusicOnly = MutableStateFlow(true)
    val preferMusicOnly: StateFlow<Boolean> = _preferMusicOnly.asStateFlow()

    private val _outputPcmMode = MutableStateFlow(OutputPcmMode.FLOAT_32)
    val outputPcmMode: StateFlow<OutputPcmMode> = _outputPcmMode.asStateFlow()
    val outputPrecision: StateFlow<OutputPcmMode> = _outputPcmMode.asStateFlow()

    private val _preferUsbDac = MutableStateFlow(false)
    val preferUsbDac: StateFlow<Boolean> = _preferUsbDac.asStateFlow()

    private val _loudnessNormalization = MutableStateFlow(false)
    val loudnessNormalization: StateFlow<Boolean> = _loudnessNormalization.asStateFlow()

    private val _loudnessOffOnSpeaker = MutableStateFlow(true)
    val loudnessOffOnSpeaker: StateFlow<Boolean> = _loudnessOffOnSpeaker.asStateFlow()
    val disableNormOnSpeaker: StateFlow<Boolean> = _loudnessOffOnSpeaker.asStateFlow()

    private val _crossfadeSeconds = MutableStateFlow(0)
    val crossfadeSeconds: StateFlow<Int> = _crossfadeSeconds.asStateFlow()
    val crossfadeSec: StateFlow<Int> = _crossfadeSeconds.asStateFlow()

    private val _smartFadeEnabled = MutableStateFlow(false)
    val smartFadeEnabled: StateFlow<Boolean> = _smartFadeEnabled.asStateFlow()
    val automix: StateFlow<Boolean> = _smartFadeEnabled.asStateFlow()

    private val _automixPerformanceMode = MutableStateFlow(AutomixPerformanceMode.BALANCED)
    val automixPerformanceMode: StateFlow<AutomixPerformanceMode> = _automixPerformanceMode.asStateFlow()

    private val _automixMode = MutableStateFlow(AutomixMode.BALANCED)
    val automixMode: StateFlow<AutomixMode> = _automixMode.asStateFlow()

    private val _skipSilence = MutableStateFlow(false)
    val skipSilence: StateFlow<Boolean> = _skipSilence.asStateFlow()

    private val _spatialAudio = MutableStateFlow(false)
    val spatialAudio: StateFlow<Boolean> = _spatialAudio.asStateFlow()

    private val _showNerdStats = MutableStateFlow(false)
    val showNerdStats: StateFlow<Boolean> = _showNerdStats.asStateFlow()

    private val _highPerformanceMode = MutableStateFlow(true)
    val highPerformanceMode: StateFlow<Boolean> = _highPerformanceMode.asStateFlow()
    val highPerformance: StateFlow<Boolean> = _highPerformanceMode.asStateFlow()

    private val _performanceRefreshRate = MutableStateFlow(120)
    val performanceRefreshRate: StateFlow<Int> = _performanceRefreshRate.asStateFlow()
    val targetRefreshRate: StateFlow<Int> = _performanceRefreshRate.asStateFlow()

    private val _localMusicFolderUri = MutableStateFlow("")
    val localMusicFolderUri: StateFlow<String> = _localMusicFolderUri.asStateFlow()

    private val _filterNonMusicAudio = MutableStateFlow(true)
    val filterNonMusicAudio: StateFlow<Boolean> = _filterNonMusicAudio.asStateFlow()

    private val _showCacheFolder = MutableStateFlow(false)
    val showCacheFolder: StateFlow<Boolean> = _showCacheFolder.asStateFlow()
    val showCachedSongsFolder: StateFlow<Boolean> = _showCacheFolder.asStateFlow()

    private val _audioCacheLimitBytes = MutableStateFlow(2048L * 1024L * 1024L)
    val audioCacheLimitBytes: StateFlow<Long> = _audioCacheLimitBytes.asStateFlow()

    private val _stopOnTaskRemoved = MutableStateFlow(false)
    val stopOnTaskRemoved: StateFlow<Boolean> = _stopOnTaskRemoved.asStateFlow()

    private val _hideVolumeBar = MutableStateFlow(false)
    val hideVolumeBar: StateFlow<Boolean> = _hideVolumeBar.asStateFlow()

    private val _hideStatusBarInPlayer = MutableStateFlow(false)
    val hideStatusBarInPlayer: StateFlow<Boolean> = _hideStatusBarInPlayer.asStateFlow()

    private val _hideSongStatus = MutableStateFlow(false)
    val hideSongStatus: StateFlow<Boolean> = _hideSongStatus.asStateFlow()

    private val _showQueueHistory = MutableStateFlow(true)
    val showQueueHistory: StateFlow<Boolean> = _showQueueHistory.asStateFlow()

    private val _swipeToPlayNext = MutableStateFlow(true)
    val swipeToPlayNext: StateFlow<Boolean> = _swipeToPlayNext.asStateFlow()

    private val _dontRepeatSuggestions = MutableStateFlow(true)
    val dontRepeatSuggestions: StateFlow<Boolean> = _dontRepeatSuggestions.asStateFlow()

    private val _smartVersionAlignment = MutableStateFlow(true)
    val smartVersionAlignment: StateFlow<Boolean> = _smartVersionAlignment.asStateFlow()

    private val _replayGenres = MutableStateFlow(true)
    val replayGenres: StateFlow<Boolean> = _replayGenres.asStateFlow()

    private val _contentCountry = MutableStateFlow(
        Locale.getDefault().country.takeIf { it.length == 2 } ?: "ID"
    )
    val contentCountry: StateFlow<String> = _contentCountry.asStateFlow()

    private val _contentLanguage = MutableStateFlow(
        Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "id"
    )
    val contentLanguage: StateFlow<String> = _contentLanguage.asStateFlow()

    // Scrobbling, Discord & Spotify
    private val _lastfmEnabled = MutableStateFlow(false)
    val lastfmEnabled: StateFlow<Boolean> = _lastfmEnabled.asStateFlow()

    private val _lastfmUsername = MutableStateFlow("")
    val lastfmUsername: StateFlow<String> = _lastfmUsername.asStateFlow()

    private val _lastfmSessionKey = MutableStateFlow("")
    val lastfmSessionKey: StateFlow<String> = _lastfmSessionKey.asStateFlow()

    private val _lastfmScrobbleEnabled = MutableStateFlow(true)
    val lastfmScrobbleEnabled: StateFlow<Boolean> = _lastfmScrobbleEnabled.asStateFlow()

    private val _lastfmNowPlaying = MutableStateFlow(true)
    val lastfmNowPlaying: StateFlow<Boolean> = _lastfmNowPlaying.asStateFlow()

    private val _lastfmPrimaryArtistOnly = MutableStateFlow(false)
    val lastfmPrimaryArtistOnly: StateFlow<Boolean> = _lastfmPrimaryArtistOnly.asStateFlow()

    private val _scrobbleMinDuration = MutableStateFlow(30)
    val scrobbleMinDuration: StateFlow<Int> = _scrobbleMinDuration.asStateFlow()

    private val _scrobbleDelayPercent = MutableStateFlow(0.5f)
    val scrobbleDelayPercent: StateFlow<Float> = _scrobbleDelayPercent.asStateFlow()

    private val _scrobbleDelaySeconds = MutableStateFlow(180)
    val scrobbleDelaySeconds: StateFlow<Int> = _scrobbleDelaySeconds.asStateFlow()

    private val _listenBrainzEnabled = MutableStateFlow(false)
    val listenBrainzEnabled: StateFlow<Boolean> = _listenBrainzEnabled.asStateFlow()

    private val _listenBrainzToken = MutableStateFlow("")
    val listenBrainzToken: StateFlow<String> = _listenBrainzToken.asStateFlow()

    private val _listenBrainzPrimaryArtistOnly = MutableStateFlow(false)
    val listenBrainzPrimaryArtistOnly: StateFlow<Boolean> = _listenBrainzPrimaryArtistOnly.asStateFlow()

    private val _discordToken = MutableStateFlow("")
    val discordToken: StateFlow<String> = _discordToken.asStateFlow()

    private val _discordUsername = MutableStateFlow("")
    val discordUsername: StateFlow<String> = _discordUsername.asStateFlow()

    private val _discordName = MutableStateFlow("")
    val discordName: StateFlow<String> = _discordName.asStateFlow()

    private val _discordAvatar = MutableStateFlow("")
    val discordAvatar: StateFlow<String> = _discordAvatar.asStateFlow()

    private val _discordRpcEnabled = MutableStateFlow(true)
    val discordRpcEnabled: StateFlow<Boolean> = _discordRpcEnabled.asStateFlow()

    private val _discordUseDetails = MutableStateFlow(false)
    val discordUseDetails: StateFlow<Boolean> = _discordUseDetails.asStateFlow()

    private val _discordShowAudioQuality = MutableStateFlow(true)
    val discordShowAudioQuality: StateFlow<Boolean> = _discordShowAudioQuality.asStateFlow()

    private val _discordAdvancedMode = MutableStateFlow(false)
    val discordAdvancedMode: StateFlow<Boolean> = _discordAdvancedMode.asStateFlow()

    private val _spotifySpdcToken = MutableStateFlow("")
    val spotifySpdcToken: StateFlow<String> = _spotifySpdcToken.asStateFlow()

    // Equalizer
    private val _equalizerEnabled = MutableStateFlow(false)
    val equalizerEnabled: StateFlow<Boolean> = _equalizerEnabled.asStateFlow()

    private val _equalizerMode = MutableStateFlow(EqualizerMode.DYNAMIC)
    val equalizerMode: StateFlow<EqualizerMode> = _equalizerMode.asStateFlow()

    private val _equalizerToneX = MutableStateFlow(0)
    val equalizerToneX: StateFlow<Int> = _equalizerToneX.asStateFlow()

    private val _equalizerToneY = MutableStateFlow(0)
    val equalizerToneY: StateFlow<Int> = _equalizerToneY.asStateFlow()

    private val _equalizerFocused = MutableStateFlow(false)
    val equalizerFocused: StateFlow<Boolean> = _equalizerFocused.asStateFlow()

    private val _equalizerBalance = MutableStateFlow(0f)
    val equalizerBalance: StateFlow<Float> = _equalizerBalance.asStateFlow()

    private val _equalizerBands = MutableStateFlow(listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f))
    val equalizerBands: StateFlow<List<Float>> = _equalizerBands.asStateFlow()

    private val _equalizerPreset = MutableStateFlow(EqualizerPreset.FLAT)
    val equalizerPreset: StateFlow<EqualizerPreset> = _equalizerPreset.asStateFlow()

    private val _audioSessionId = MutableStateFlow(0)
    val audioSessionId: StateFlow<Int> = _audioSessionId.asStateFlow()

    // Sources & Remote Libraries
    private val _webdavUrl = MutableStateFlow("")
    val webdavUrl: StateFlow<String> = _webdavUrl.asStateFlow()

    private val _webdavUsername = MutableStateFlow("")
    val webdavUsername: StateFlow<String> = _webdavUsername.asStateFlow()

    private val _webdavPassword = MutableStateFlow("")
    val webdavPassword: StateFlow<String> = _webdavPassword.asStateFlow()

    private val _smbHost = MutableStateFlow("")
    val smbHost: StateFlow<String> = _smbHost.asStateFlow()

    private val _smbShare = MutableStateFlow("")
    val smbShare: StateFlow<String> = _smbShare.asStateFlow()

    private val _smbUsername = MutableStateFlow("")
    val smbUsername: StateFlow<String> = _smbUsername.asStateFlow()

    private val _smbPassword = MutableStateFlow("")
    val smbPassword: StateFlow<String> = _smbPassword.asStateFlow()

    private val _upgradeLengthSlackSeconds = MutableStateFlow(3)
    val upgradeLengthSlackSeconds: StateFlow<Int> = _upgradeLengthSlackSeconds.asStateFlow()

    val isInitialized: Boolean
        get() = ::prefs.isInitialized

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = EncryptedPrefs.open(context, PREFS_FILE, LEGACY_PREFS_FILE)
        _themeMode.value = runCatching {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.DARK.name)!!)
        }.getOrDefault(ThemeMode.DARK)
        _reduceAnimation.value = prefs.getBoolean("reduce_animation", false)
        _reduceDynamicBlur.value = prefs.getBoolean("reduce_blur", false)
        _liquidGlass.value = prefs.getBoolean("liquid_glass", false) && isLiquidGlassSupported
        _fullLiquidGlassUi.value = prefs.getBoolean("full_liquid_glass_ui", false)
        _fullBleedArtwork.value = prefs.getBoolean("full_screen_cover_art_v2", false)
        _legacyMeshGradient.value = prefs.getBoolean("legacy_mesh_gradient", false)
        _animatedCanvas.value = prefs.getBoolean("animated_cover_art", true)
        _canvasOverCellular.value = prefs.getBoolean("animated_cover_cellular", false)
        _useBitChordHomeUi.value = prefs.getBoolean("use_bitchord_home_ui", false)
        _useNewQuickPicksGrid.value = prefs.getBoolean("use_new_quick_picks_grid", false)
        _useNewSongShelvesDesign.value = prefs.getBoolean("use_new_song_shelves_design", false)
        _useNewCommunityDesign.value = prefs.getBoolean("use_new_community_design", false)
        _useDynamicMusicTheme.value = prefs.getBoolean("use_dynamic_music_theme", false)
        _showMusicRecognitionButton.value = prefs.getBoolean("show_music_recognition_button", true)
        _signatureApiEnabled.value = prefs.getBoolean("signature_api_enabled", false)
        _signatureApiUrl.value = prefs.getString("signature_api_url", "") ?: ""
        _syncedLyrics.value = prefs.getBoolean("synced_lyrics", true)
        _lyricsBlur.value = prefs.getBoolean("blur_unfocused_lyrics", true)
        _translationLanguage.value = prefs.getString("translation_language", "id") ?: "id"
        _audioQualityWifi.value = runCatching {
            AudioQuality.valueOf(prefs.getString("wifi_quality", AudioQuality.LOSSLESS.name)!!)
        }.getOrDefault(AudioQuality.LOSSLESS)
        _audioQualityCellular.value = runCatching {
            AudioQuality.valueOf(prefs.getString("mobile_quality", AudioQuality.LOSSLESS.name)!!)
        }.getOrDefault(AudioQuality.LOSSLESS)
        _downloadQuality.value = runCatching {
            DownloadQuality.valueOf(prefs.getString("download_quality", DownloadQuality.LOSSLESS.name)!!)
        }.getOrDefault(DownloadQuality.LOSSLESS)
        _wifiOnlyDownloads.value = prefs.getBoolean("download_wifi_only", false)
        _exportDownloads.value = prefs.getBoolean("export_compatible_downloads", true)
        _dolbyAtmos.value = prefs.getBoolean("dolby_atmos", false)
        _preferMusicOnly.value = prefs.getBoolean("prefer_music_only", true)
        _outputPcmMode.value = runCatching {
            OutputPcmMode.valueOf(prefs.getString("output_precision", OutputPcmMode.FLOAT_32.name)!!)
        }.getOrDefault(OutputPcmMode.FLOAT_32)
        _preferUsbDac.value = prefs.getBoolean("prefer_usb_dac", false)
        _loudnessNormalization.value = prefs.getBoolean("loudness_normalization", false)
        _loudnessOffOnSpeaker.value = prefs.getBoolean("disable_norm_on_speaker", true)
        _crossfadeSeconds.value = prefs.getInt("crossfade_sec", 0)
        _smartFadeEnabled.value = prefs.getBoolean("automix", false)
        _automixPerformanceMode.value = runCatching {
            AutomixPerformanceMode.valueOf(
                prefs.getString("automix_perf_mode", AutomixPerformanceMode.BALANCED.name)!!
            )
        }.getOrDefault(AutomixPerformanceMode.BALANCED)
        _skipSilence.value = prefs.getBoolean("skip_silence", false)
        _spatialAudio.value = prefs.getBoolean("spatial_audio", false)
        _showNerdStats.value = prefs.getBoolean("show_nerd_stats", false)
        _highPerformanceMode.value = prefs.getBoolean("high_performance", true)
        _performanceRefreshRate.value = prefs.getInt("target_refresh_rate", 120).takeIf { it > 0 } ?: 120
        _localMusicFolderUri.value = prefs.getString("local_music_folder_uri", "") ?: ""
        _filterNonMusicAudio.value = prefs.getBoolean("filter_non_music_audio", true)
        _showCacheFolder.value = prefs.getBoolean("show_cached_songs_folder", false)
        _audioCacheLimitBytes.value = prefs.getLong("audio_cache_limit_bytes", 2048L * 1024L * 1024L)
        _stopOnTaskRemoved.value = prefs.getBoolean("stop_on_task_removed", false)
        _hideVolumeBar.value = prefs.getBoolean("hide_volume_bar", false)
        _hideStatusBarInPlayer.value = prefs.getBoolean("hide_status_bar_in_player", false)
        _hideSongStatus.value = prefs.getBoolean("hide_song_status", false)
        _showQueueHistory.value = prefs.getBoolean("show_queue_history", true)
        _swipeToPlayNext.value = prefs.getBoolean("swipe_to_play_next", true)
        _dontRepeatSuggestions.value = prefs.getBoolean("dont_repeat_suggestions", true)
        _smartVersionAlignment.value = prefs.getBoolean("smart_version_alignment", true)
        _replayGenres.value = prefs.getBoolean("replay_genres", true)

        _lastfmEnabled.value = prefs.getBoolean("lastfm_enabled", false)
        _lastfmUsername.value = prefs.getString("lastfm_username", "") ?: ""
        _lastfmSessionKey.value = prefs.getString("lastfm_session_key", "") ?: ""
        _lastfmScrobbleEnabled.value = prefs.getBoolean("lastfm_scrobble_enabled", true)
        _lastfmNowPlaying.value = prefs.getBoolean("lastfm_now_playing", true)
        _lastfmPrimaryArtistOnly.value = prefs.getBoolean("lastfm_primary_artist_only", false)
        _scrobbleMinDuration.value = prefs.getInt("scrobble_min_duration", 30)
        _scrobbleDelayPercent.value = prefs.getFloat("scrobble_delay_percent", 0.5f)
        _scrobbleDelaySeconds.value = prefs.getInt("scrobble_delay_seconds", 180)
        _listenBrainzEnabled.value = prefs.getBoolean("listenbrainz_enabled", false)
        _listenBrainzToken.value = prefs.getString("listenbrainz_token", "") ?: ""
        _listenBrainzPrimaryArtistOnly.value = prefs.getBoolean("listenbrainz_primary_artist_only", false)
        _discordToken.value = prefs.getString("discord_token", "") ?: ""
        _discordUsername.value = prefs.getString("discord_username", "") ?: ""
        _discordName.value = prefs.getString("discord_name", "") ?: ""
        _discordAvatar.value = prefs.getString("discord_avatar", "") ?: ""
        _discordRpcEnabled.value = prefs.getBoolean("discord_rpc_enabled", true)
        _discordUseDetails.value = prefs.getBoolean("discord_use_details", false)
        _discordShowAudioQuality.value = prefs.getBoolean("discord_show_audio_quality", true)
        _discordAdvancedMode.value = prefs.getBoolean("discord_advanced_mode", false)
        _spotifySpdcToken.value = prefs.getString("spotify_spdc_token", "") ?: ""

        _equalizerEnabled.value = prefs.getBoolean("eq_enabled", false)
        _equalizerMode.value = runCatching {
            EqualizerMode.valueOf(prefs.getString("eq_mode", EqualizerMode.DYNAMIC.name)!!)
        }.getOrDefault(EqualizerMode.DYNAMIC)
        _equalizerToneX.value = prefs.getInt("eq_tone_x", 0)
        _equalizerToneY.value = prefs.getInt("eq_tone_y", 0)
        _equalizerFocused.value = prefs.getBoolean("eq_focused", false)
        _equalizerBalance.value = prefs.getFloat("eq_balance", 0f)
        _equalizerPreset.value = runCatching {
            EqualizerPreset.valueOf(prefs.getString("eq_preset", EqualizerPreset.FLAT.name)!!)
        }.getOrDefault(EqualizerPreset.FLAT)
        _equalizerBands.value = _equalizerPreset.value.gains

        _webdavUrl.value = prefs.getString("webdav_url", "") ?: ""
        _webdavUsername.value = prefs.getString("webdav_user", "") ?: ""
        _webdavPassword.value = prefs.getString("webdav_pass", "") ?: ""
        _smbHost.value = prefs.getString("smb_host", "") ?: ""
        _smbShare.value = prefs.getString("smb_share", "") ?: ""
        _smbUsername.value = prefs.getString("smb_user", "") ?: ""
        _smbPassword.value = prefs.getString("smb_pass", "") ?: ""
        _upgradeLengthSlackSeconds.value = prefs.getInt("upgrade_slack_sec", 3)
    }

    private fun save(block: SharedPreferences.Editor.() -> Unit) {
        if (!::prefs.isInitialized) return
        prefs.edit().apply(block).apply()
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        save { putString("theme_mode", mode.name) }
    }

    fun setReduceAnimation(v: Boolean) {
        _reduceAnimation.value = v
        save { putBoolean("reduce_animation", v) }
    }

    fun setReduceDynamicBlur(v: Boolean) {
        _reduceDynamicBlur.value = v
        save { putBoolean("reduce_blur", v) }
    }

    fun setReduceBlur(v: Boolean) = setReduceDynamicBlur(v)

    fun setLiquidGlass(v: Boolean) {
        val allowed = v && isLiquidGlassSupported
        _liquidGlass.value = allowed
        save { putBoolean("liquid_glass", allowed) }
    }

    fun setFullLiquidGlassUi(v: Boolean) {
        _fullLiquidGlassUi.value = v
        save { putBoolean("full_liquid_glass_ui", v) }
    }

    fun setFullBleedArtwork(v: Boolean) {
        _fullBleedArtwork.value = v
        save {
            putBoolean("full_screen_cover_art_v2", v)
            putBoolean("full_screen_cover_art", v)
        }
    }

    fun setFullScreenCoverArt(v: Boolean) = setFullBleedArtwork(v)

    fun setLegacyMeshGradient(v: Boolean) {
        _legacyMeshGradient.value = v
        save { putBoolean("legacy_mesh_gradient", v) }
    }

    fun setAnimatedCanvas(v: Boolean) {
        _animatedCanvas.value = v
        save { putBoolean("animated_cover_art", v) }
    }

    fun setAnimatedCoverArt(v: Boolean) = setAnimatedCanvas(v)

    fun setCanvasOverCellular(v: Boolean) {
        _canvasOverCellular.value = v
        save { putBoolean("animated_cover_cellular", v) }
    }

    fun setAnimatedCoverCellular(v: Boolean) = setCanvasOverCellular(v)

    fun setUseBitChordHomeUi(v: Boolean) {
        _useBitChordHomeUi.value = v
        save { putBoolean("use_bitchord_home_ui", v) }
    }

    fun setUseNewQuickPicksGrid(v: Boolean) {
        _useNewQuickPicksGrid.value = v
        save { putBoolean("use_new_quick_picks_grid", v) }
    }
    fun setQuickPicksRedesignEnabled(v: Boolean) = setUseNewQuickPicksGrid(v)

    fun setUseNewSongShelvesDesign(v: Boolean) {
        _useNewSongShelvesDesign.value = v
        save { putBoolean("use_new_song_shelves_design", v) }
    }
    fun setHomeRedesignSectionsEnabled(v: Boolean) = setUseNewSongShelvesDesign(v)

    fun setUseNewCommunityDesign(v: Boolean) {
        _useNewCommunityDesign.value = v
        save { putBoolean("use_new_community_design", v) }
    }
    fun setCommunitySectionRedesignEnabled(v: Boolean) = setUseNewCommunityDesign(v)

    fun setUseDynamicMusicTheme(v: Boolean) {
        _useDynamicMusicTheme.value = v
        save { putBoolean("use_dynamic_music_theme", v) }
    }
    fun setDynamicAlbumColorTheme(v: Boolean) = setUseDynamicMusicTheme(v)

    fun setShowMusicRecognitionButton(v: Boolean) {
        _showMusicRecognitionButton.value = v
        save { putBoolean("show_music_recognition_button", v) }
    }

    fun setSignatureApiEnabled(v: Boolean) {
        _signatureApiEnabled.value = v
        save { putBoolean("signature_api_enabled", v) }
    }

    fun setSignatureApiUrl(url: String) {
        _signatureApiUrl.value = url.trim()
        save { putString("signature_api_url", _signatureApiUrl.value) }
    }

    fun setSyncedLyrics(v: Boolean) {
        _syncedLyrics.value = v
        save { putBoolean("synced_lyrics", v) }
    }

    fun setLyricsBlur(v: Boolean) {
        _lyricsBlur.value = v
        save { putBoolean("blur_unfocused_lyrics", v) }
    }

    fun setBlurUnfocusedLyrics(v: Boolean) = setLyricsBlur(v)

    fun toggleLyricsSource(source: LyricsSource) {
        val current = _lyricsSources.value.toMutableSet()
        if (source in current) {
            if (current.size > 1) current.remove(source)
        } else {
            current.add(source)
        }
        _lyricsSources.value = current
    }

    fun setLyricsSourceOrder(order: List<LyricsSource>) {
        _lyricsSourceOrder.value = order
    }

    fun setPrioritizeSyllableSync(v: Boolean) {
        _prioritizeSyllableSync.value = v
    }

    fun setPaxSenixApiKey(key: String) {
        _paxSenixApiKey.value = key.trim()
    }

    fun setLyricsOffsetMs(ms: Int) {
        _lyricsOffsetMs.value = ms.coerceIn(-5000, 5000)
    }

    fun setTranslationLanguage(lang: String) {
        _translationLanguage.value = lang
        save { putString("translation_language", lang) }
    }

    fun setAudioQualityWifi(q: AudioQuality) {
        _audioQualityWifi.value = q
        save { putString("wifi_quality", q.name) }
    }

    fun setWifiQuality(q: AudioQuality) = setAudioQualityWifi(q)

    fun setAudioQualityCellular(q: AudioQuality) {
        _audioQualityCellular.value = q
        save { putString("mobile_quality", q.name) }
    }

    fun setMobileQuality(q: AudioQuality) = setAudioQualityCellular(q)

    fun setDownloadQuality(q: DownloadQuality) {
        _downloadQuality.value = q
        save { putString("download_quality", q.name) }
    }

    fun setWifiOnlyDownloads(v: Boolean) {
        _wifiOnlyDownloads.value = v
        save { putBoolean("download_wifi_only", v) }
    }

    fun setDownloadWifiOnly(v: Boolean) = setWifiOnlyDownloads(v)

    fun setExportDownloads(v: Boolean) {
        _exportDownloads.value = v
        save { putBoolean("export_compatible_downloads", v) }
    }

    fun setExportCompatibleDownloads(v: Boolean) = setExportDownloads(v)

    fun setDolbyAtmos(v: Boolean) {
        _dolbyAtmos.value = v
        save { putBoolean("dolby_atmos", v) }
    }

    fun setPreferMusicOnly(v: Boolean) {
        _preferMusicOnly.value = v
        save { putBoolean("prefer_music_only", v) }
    }

    fun setOutputPcmMode(mode: OutputPcmMode) {
        _outputPcmMode.value = mode
        save { putString("output_precision", mode.name) }
    }

    fun setOutputPrecision(mode: OutputPcmMode) = setOutputPcmMode(mode)

    fun setPreferUsbDac(v: Boolean) {
        _preferUsbDac.value = v
        save { putBoolean("prefer_usb_dac", v) }
    }

    fun setLoudnessNormalization(v: Boolean) {
        _loudnessNormalization.value = v
        save { putBoolean("loudness_normalization", v) }
    }

    fun setLoudnessOffOnSpeaker(v: Boolean) {
        _loudnessOffOnSpeaker.value = v
        save { putBoolean("disable_norm_on_speaker", v) }
    }

    fun setDisableNormOnSpeaker(v: Boolean) = setLoudnessOffOnSpeaker(v)

    fun setCrossfadeSeconds(sec: Int) {
        val clamped = sec.coerceIn(0, 12)
        _crossfadeSeconds.value = clamped
        save { putInt("crossfade_sec", clamped) }
    }

    fun setCrossfadeSec(sec: Int) = setCrossfadeSeconds(sec)

    fun setSmartFadeEnabled(v: Boolean) {
        _smartFadeEnabled.value = v
        save { putBoolean("automix", v) }
    }

    fun setAutomix(v: Boolean) = setSmartFadeEnabled(v)

    fun setAutomixPerformanceMode(mode: AutomixPerformanceMode) {
        _automixPerformanceMode.value = mode
        save { putString("automix_perf_mode", mode.name) }
    }

    fun setSkipSilence(v: Boolean) {
        _skipSilence.value = v
        save { putBoolean("skip_silence", v) }
    }

    fun setSpatialAudio(v: Boolean) {
        _spatialAudio.value = v
        save { putBoolean("spatial_audio", v) }
    }

    fun setShowNerdStats(v: Boolean) {
        _showNerdStats.value = v
        save { putBoolean("show_nerd_stats", v) }
    }

    fun setHighPerformanceMode(v: Boolean) {
        _highPerformanceMode.value = v
        save { putBoolean("high_performance", v) }
    }

    fun setHighPerformance(v: Boolean) = setHighPerformanceMode(v)

    fun setPerformanceRefreshRate(hz: Int) {
        _performanceRefreshRate.value = hz
        save { putInt("target_refresh_rate", hz) }
    }

    fun setTargetRefreshRate(hz: Int) = setPerformanceRefreshRate(hz)

    fun setLocalMusicFolderUri(uri: String?) {
        val clean = uri?.trim().orEmpty()
        _localMusicFolderUri.value = clean
        save {
            if (clean.isEmpty()) remove("local_music_folder_uri")
            else putString("local_music_folder_uri", clean)
        }
    }

    fun setFilterNonMusicAudio(v: Boolean) {
        _filterNonMusicAudio.value = v
        save { putBoolean("filter_non_music_audio", v) }
    }

    fun setShowCacheFolder(v: Boolean) {
        _showCacheFolder.value = v
        save { putBoolean("show_cached_songs_folder", v) }
    }

    fun setShowCachedSongsFolder(v: Boolean) = setShowCacheFolder(v)

    fun setAudioCacheLimitBytes(bytes: Long) {
        _audioCacheLimitBytes.value = bytes
        save { putLong("audio_cache_limit_bytes", bytes) }
    }

    fun setStopOnTaskRemoved(v: Boolean) {
        _stopOnTaskRemoved.value = v
        save { putBoolean("stop_on_task_removed", v) }
    }

    fun setHideVolumeBar(v: Boolean) {
        _hideVolumeBar.value = v
        save { putBoolean("hide_volume_bar", v) }
    }

    fun setHideStatusBarInPlayer(v: Boolean) {
        _hideStatusBarInPlayer.value = v
        save { putBoolean("hide_status_bar_in_player", v) }
    }

    fun setHideSongStatus(v: Boolean) {
        _hideSongStatus.value = v
        save { putBoolean("hide_song_status", v) }
    }

    fun setShowQueueHistory(v: Boolean) {
        _showQueueHistory.value = v
        save { putBoolean("show_queue_history", v) }
    }

    fun setSwipeToPlayNext(v: Boolean) {
        _swipeToPlayNext.value = v
        save { putBoolean("swipe_to_play_next", v) }
    }

    fun setDontRepeatSuggestions(v: Boolean) {
        _dontRepeatSuggestions.value = v
        save { putBoolean("dont_repeat_suggestions", v) }
    }

    fun setSmartVersionAlignment(v: Boolean) {
        _smartVersionAlignment.value = v
        save { putBoolean("smart_version_alignment", v) }
    }

    fun setReplayGenres(v: Boolean) {
        _replayGenres.value = v
        save { putBoolean("replay_genres", v) }
    }

    fun setContentCountry(country: String) {
        _contentCountry.value = country
        save { putString("content_country", country) }
    }

    fun setContentLanguage(language: String) {
        _contentLanguage.value = language
        save { putString("content_language", language) }
    }

    // Scrobbling, Discord & Spotify setters
    fun setLastfmEnabled(v: Boolean) {
        _lastfmEnabled.value = v
        save { putBoolean("lastfm_enabled", v) }
    }

    fun setLastfmUsername(username: String) {
        _lastfmUsername.value = username.trim()
        save { putString("lastfm_username", _lastfmUsername.value) }
    }

    fun setLastfmSessionKey(key: String) {
        _lastfmSessionKey.value = key.trim()
        save { putString("lastfm_session_key", _lastfmSessionKey.value) }
    }

    fun setLastfmScrobbleEnabled(v: Boolean) {
        _lastfmScrobbleEnabled.value = v
        save { putBoolean("lastfm_scrobble_enabled", v) }
    }

    fun setLastfmNowPlaying(v: Boolean) {
        _lastfmNowPlaying.value = v
        save { putBoolean("lastfm_now_playing", v) }
    }

    fun setLastfmPrimaryArtistOnly(v: Boolean) {
        _lastfmPrimaryArtistOnly.value = v
        save { putBoolean("lastfm_primary_artist_only", v) }
    }

    fun setScrobbleMinDuration(sec: Int) {
        _scrobbleMinDuration.value = sec.coerceIn(15, 120)
        save { putInt("scrobble_min_duration", _scrobbleMinDuration.value) }
    }

    fun setScrobbleDelayPercent(pct: Float) {
        _scrobbleDelayPercent.value = pct.coerceIn(0.1f, 1.0f)
        save { putFloat("scrobble_delay_percent", _scrobbleDelayPercent.value) }
    }

    fun setScrobbleDelaySeconds(sec: Int) {
        _scrobbleDelaySeconds.value = sec.coerceIn(30, 300)
        save { putInt("scrobble_delay_seconds", _scrobbleDelaySeconds.value) }
    }

    fun setListenBrainzEnabled(v: Boolean) {
        _listenBrainzEnabled.value = v
        save { putBoolean("listenbrainz_enabled", v) }
    }

    fun setListenBrainzToken(token: String) {
        _listenBrainzToken.value = token.trim()
        save { putString("listenbrainz_token", _listenBrainzToken.value) }
    }

    fun setListenBrainzPrimaryArtistOnly(v: Boolean) {
        _listenBrainzPrimaryArtistOnly.value = v
        save { putBoolean("listenbrainz_primary_artist_only", v) }
    }

    fun setDiscordToken(token: String) {
        _discordToken.value = token.trim()
        save { putString("discord_token", _discordToken.value) }
    }

    fun setDiscordAccount(username: String, name: String, avatar: String?) {
        _discordUsername.value = username.trim()
        _discordName.value = name.trim()
        _discordAvatar.value = avatar?.trim().orEmpty()
        save {
            putString("discord_username", _discordUsername.value)
            putString("discord_name", _discordName.value)
            putString("discord_avatar", _discordAvatar.value)
        }
    }

    fun clearDiscordAccount() {
        _discordToken.value = ""
        _discordUsername.value = ""
        _discordName.value = ""
        _discordAvatar.value = ""
        save {
            remove("discord_token")
            remove("discord_username")
            remove("discord_name")
            remove("discord_avatar")
        }
    }

    fun setDiscordRpcEnabled(v: Boolean) {
        _discordRpcEnabled.value = v
        save { putBoolean("discord_rpc_enabled", v) }
    }

    fun setDiscordUseDetails(v: Boolean) {
        _discordUseDetails.value = v
        save { putBoolean("discord_use_details", v) }
    }

    fun setDiscordShowAudioQuality(v: Boolean) {
        _discordShowAudioQuality.value = v
        save { putBoolean("discord_show_audio_quality", v) }
    }

    fun setDiscordAdvancedMode(v: Boolean) {
        _discordAdvancedMode.value = v
        save { putBoolean("discord_advanced_mode", v) }
    }

    fun setSpotifySpdcToken(token: String) {
        _spotifySpdcToken.value = token.trim()
        save { putString("spotify_spdc_token", _spotifySpdcToken.value) }
    }

    // Equalizer setters
    fun setEqualizerEnabled(enabled: Boolean) {
        _equalizerEnabled.value = enabled
        save { putBoolean("eq_enabled", enabled) }
    }

    fun setEqualizerMode(mode: EqualizerMode) {
        _equalizerMode.value = mode
        save { putString("eq_mode", mode.name) }
    }

    fun setEqualizerToneX(x: Int) {
        _equalizerToneX.value = x.coerceIn(-5, 5)
        save { putInt("eq_tone_x", _equalizerToneX.value) }
    }

    fun setEqualizerToneY(y: Int) {
        _equalizerToneY.value = y.coerceIn(-5, 5)
        save { putInt("eq_tone_y", _equalizerToneY.value) }
    }

    fun setEqualizerFocused(focused: Boolean) {
        _equalizerFocused.value = focused
        save { putBoolean("eq_focused", focused) }
    }

    fun setEqualizerBalance(balance: Float) {
        _equalizerBalance.value = balance.coerceIn(-1f, 1f)
        save { putFloat("eq_balance", _equalizerBalance.value) }
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        _equalizerPreset.value = preset
        if (preset != EqualizerPreset.CUSTOM) {
            _equalizerBands.value = preset.gains
        }
        save { putString("eq_preset", preset.name) }
    }

    fun setEqualizerBands(bands: List<Float>) {
        _equalizerBands.value = bands.map { it.coerceIn(-12f, 12f) }
        _equalizerPreset.value = EqualizerPreset.CUSTOM
        save { putString("eq_preset", EqualizerPreset.CUSTOM.name) }
    }

    fun setAudioSessionId(id: Int) {
        _audioSessionId.value = id
    }

    // Sources & Remote Libraries setters
    fun setWebDavUrl(url: String) {
        _webdavUrl.value = url.trim()
        save { putString("webdav_url", _webdavUrl.value) }
    }

    fun setWebDavUsername(user: String) {
        _webdavUsername.value = user.trim()
        save { putString("webdav_user", _webdavUsername.value) }
    }

    fun setWebDavPassword(pass: String) {
        _webdavPassword.value = pass
        save { putString("webdav_pass", _webdavPassword.value) }
    }

    fun setSmbHost(host: String) {
        _smbHost.value = host.trim()
        save { putString("smb_host", _smbHost.value) }
    }

    fun setSmbShare(share: String) {
        _smbShare.value = share.trim()
        save { putString("smb_share", _smbShare.value) }
    }

    fun setSmbUsername(user: String) {
        _smbUsername.value = user.trim()
        save { putString("smb_user", _smbUsername.value) }
    }

    fun setSmbPassword(pass: String) {
        _smbPassword.value = pass
        save { putString("smb_pass", _smbPassword.value) }
    }

    fun setUpgradeLengthSlackSeconds(sec: Int) {
        _upgradeLengthSlackSeconds.value = sec.coerceIn(0, 10)
        save { putInt("upgrade_slack_sec", _upgradeLengthSlackSeconds.value) }
    }
}
