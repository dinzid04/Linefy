package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import androidx.compose.runtime.Immutable
import com.example.data.model.AudioStreamInfo
import com.example.data.model.MusicItem
import com.example.data.settings.AppSettings
import com.example.data.settings.EqualizerMode
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

@Immutable
data class PlayerUiState(
    val currentTrack: MusicItem? = null,
    val playingFromSource: String = "Playing from Pilihan cepat",
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val progressFraction: Float = 0f,
    val currentPositionSec: Int = 0,
    val currentPositionMs: Long = 0L,
    val exactPositionSec: Double = 0.0,
    val durationSec: Int = 187,
    val volumeFraction: Float = 0.85f,
    val isShuffleEnabled: Boolean = false,
    val isRepeatEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isAutoPlayEnabled: Boolean = true,
    val streamFormat: String = "m4a (aac)",
    val queue: List<MusicItem> = emptyList(),
    val autoPlayQueue: List<MusicItem> = emptyList(),
    val currentIndex: Int = 0,
    val sleepTimerLabel: String? = null,
    val sleepTimerRemainingSec: Int = 0,
    val sleepTimerEndOfTrack: Boolean = false
) {
    val hasNext: Boolean
        get() = queue.size > 1 || (isAutoPlayEnabled && autoPlayQueue.isNotEmpty())
    val hasPrevious: Boolean
        get() = queue.size > 1
}

class AudioPlayerController(
    context: Context? = null
) {
    companion object {
        @Volatile
        private var INSTANCE: AudioPlayerController? = null

        private val _fallbackState = MutableStateFlow(PlayerUiState())

        val state: StateFlow<PlayerUiState>
            get() = INSTANCE?.playerState ?: _fallbackState

        val isPlaying: StateFlow<Boolean>
            get() = MutableStateFlow(INSTANCE?.playerState?.value?.isPlaying == true).asStateFlow()

        fun getInstance(context: Context): AudioPlayerController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioPlayerController(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val appContext: Context? = context?.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val audioManager: AudioManager? =
        appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _playerState = MutableStateFlow(
        PlayerUiState(
            volumeFraction = readDeviceVolumeFraction(audioManager)
        )
    )
    val playerState: StateFlow<PlayerUiState> = _playerState.asStateFlow()

    private val _isCurrentTrackFavorite = MutableStateFlow(false)
    val isCurrentTrackFavorite: StateFlow<Boolean> = _isCurrentTrackFavorite.asStateFlow()

    @Volatile
    private var isFullPlayerVisible: Boolean = false

    private var mediaPlayer: MediaPlayer? = null
    private var outgoingMediaPlayer: MediaPlayer? = null
    private var crossfadeJob: Job? = null
    private var crossfadeTriggeredForTrackId: String? = null
    private var audioEqualizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var progressJob: Job? = null
    private var virtualPlaybackJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var currentStreamResolver: (suspend (MusicItem) -> AudioStreamInfo?)? = null
    private var currentTrackPlayedCallback: ((MusicItem) -> Unit)? = null
    private var toggleFavoriteCallback: ((MusicItem) -> Unit)? = null

    init {
        scope.launch {
            kotlinx.coroutines.flow.combine(
                AppSettings.equalizerEnabled,
                AppSettings.equalizerMode,
                AppSettings.equalizerToneX,
                AppSettings.equalizerToneY,
                AppSettings.equalizerBands,
            ) { enabled, mode, tx, ty, bands ->
                listOf(enabled, mode, tx, ty, bands)
            }.collect {
                applyEqualizerAndDspSettings()
            }
        }
        scope.launch {
            kotlinx.coroutines.flow.combine(
                AppSettings.equalizerBalance,
                AppSettings.loudnessNormalization,
            ) { bal, loud -> bal to loud }.collect {
                applyStereoBalanceAndVolume()
                applyEqualizerAndDspSettings()
            }
        }
    }

    private fun applyStereoBalanceAndVolume() {
        val mp = mediaPlayer ?: return
        val vol = _playerState.value.volumeFraction
        val baseGain = if (audioManager != null) {
            if (vol <= 0.005f) 0f else 1f
        } else {
            vol
        }
        val bal = AppSettings.equalizerBalance.value.coerceIn(-1f, 1f)
        val leftGain = (baseGain * if (bal > 0f) (1f - bal) else 1f).coerceIn(0f, 1f)
        val rightGain = (baseGain * if (bal < 0f) (1f + bal) else 1f).coerceIn(0f, 1f)
        runCatching { mp.setVolume(leftGain, rightGain) }
    }

    private fun attachAndConfigureAudioEffects(sessionId: Int) {
        if (sessionId <= 0) return
        runCatching { AppSettings.setAudioSessionId(sessionId) }
        runCatching {
            audioEqualizer?.release()
            audioEqualizer = Equalizer(0, sessionId)
        }
        runCatching {
            loudnessEnhancer?.release()
            loudnessEnhancer = LoudnessEnhancer(sessionId)
        }
        applyEqualizerAndDspSettings()
    }

    private fun applyEqualizerAndDspSettings() {
        val eq = audioEqualizer
        if (eq != null) {
            runCatching {
                val enabled = AppSettings.equalizerEnabled.value
                eq.enabled = enabled
                if (enabled) {
                    val numBands = eq.numberOfBands.toInt()
                    val range = eq.bandLevelRange
                    val minMb = range.getOrNull(0)?.toInt() ?: -1500
                    val maxMb = range.getOrNull(1)?.toInt() ?: 1500
                    val mode = AppSettings.equalizerMode.value
                    val userBands = AppSettings.equalizerBands.value
                    val toneX = AppSettings.equalizerToneX.value
                    val toneY = AppSettings.equalizerToneY.value

                    for (b in 0 until numBands) {
                        val frac = if (numBands > 1) b.toFloat() / (numBands - 1).toFloat() else 0.5f
                        val gainDb = if (mode == EqualizerMode.MANUAL) {
                            val idx = (frac * (userBands.size - 1).coerceAtLeast(0)).roundToInt()
                                .coerceIn(0, (userBands.size - 1).coerceAtLeast(0))
                            userBands.getOrElse(idx) { 0f }
                        } else {
                            // Tone pad: toneY boosts bass/warmth on low bands, toneX boosts treble/presence on high bands
                            val bassWeight = (1f - frac * 1.4f).coerceIn(-0.4f, 1f)
                            val trebleWeight = ((frac - 0.35f) * 1.5f).coerceIn(-0.4f, 1f)
                            (toneY * 7.5f * bassWeight + toneX * 7.5f * trebleWeight).coerceIn(-12f, 12f)
                        }
                        val targetMb = (gainDb * 100f).roundToInt().coerceIn(minMb, maxMb).toShort()
                        eq.setBandLevel(b.toShort(), targetMb)
                    }
                }
            }
        }
        val le = loudnessEnhancer
        if (le != null) {
            runCatching {
                val norm = AppSettings.loudnessNormalization.value
                le.enabled = norm
                if (norm) {
                    le.setTargetGain(250) // +2.5 dB gentle loudness normalization
                } else {
                    le.setTargetGain(0)
                }
            }
        }
    }

    fun setFullPlayerVisible(visible: Boolean) {
        isFullPlayerVisible = visible
    }

    fun setFullPlayerExpanded(expanded: Boolean) {
        setFullPlayerVisible(expanded)
    }

    fun setFavoriteCallback(onToggleFavorite: (MusicItem) -> Unit) {
        toggleFavoriteCallback = onToggleFavorite
    }


    fun updateCurrentTrackFavoriteStatus(isFavorite: Boolean) {
        if (_isCurrentTrackFavorite.value != isFavorite) {
            _isCurrentTrackFavorite.value = isFavorite
            notifyPlaybackService()
        }
    }

    fun triggerToggleFavoriteFromNotification() {
        val current = _playerState.value.currentTrack ?: return
        toggleFavoriteCallback?.invoke(current)
    }

    private fun notifyPlaybackService() {
        val ctx = appContext ?: return
        val st = _playerState.value
        if (st.currentTrack != null) {
            MusicPlaybackService.startOrUpdate(ctx)
        }
    }

    private fun readDeviceVolumeFraction(am: AudioManager?): Float {
        if (am == null) return 0.85f
        return try {
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC).coerceIn(0, max)
            (cur.toFloat() / max.toFloat()).coerceIn(0f, 1f)
        } catch (_: Exception) {
            0.85f
        }
    }

    /**
     * Re-reads the current hardware STREAM_MUSIC volume from the device and updates UI state.
     */
    fun syncVolumeFromDevice() {
        val am = audioManager ?: return
        val deviceFraction = readDeviceVolumeFraction(am)
        _playerState.update { it.copy(volumeFraction = deviceFraction) }
    }

    /**
     * Adjusts device media volume up or down by 1 hardware step (with system synchronization)
     * when the user taps the Volume Up or Volume Down icon buttons in the player.
     */
    fun adjustDeviceVolumeStep(raise: Boolean) {
        val am = audioManager
        if (am != null) {
            try {
                val direction = if (raise) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
                val updated = readDeviceVolumeFraction(am)
                _playerState.update { it.copy(volumeFraction = updated) }
                try {
                    val mpGain = if (updated <= 0.005f) 0f else 1f
                    mediaPlayer?.setVolume(mpGain, mpGain)
                } catch (_: Exception) {
                }
                return
            } catch (_: Exception) {
            }
        }
        val delta = if (raise) 0.08f else -0.08f
        setVolume(_playerState.value.volumeFraction + delta)
    }

    fun setVolume(fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        val am = audioManager
        if (am != null) {
            try {
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                val targetIndex = (clamped * max).roundToInt().coerceIn(0, max)
                am.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, 0)
            } catch (_: Exception) {
            }
        }
        try {
            val mpGain = if (am != null) {
                if (clamped <= 0.005f) 0f else 1f
            } else {
                clamped
            }
            mediaPlayer?.setVolume(mpGain, mpGain)
        } catch (_: Exception) {
        }
        _playerState.update { it.copy(volumeFraction = clamped) }
    }

    fun toggleShuffle() {
        _playerState.update { state ->
            val nextShuffle = !state.isShuffleEnabled
            val updatedQueue = if (nextShuffle && state.queue.size > 1) {
                val current = state.currentTrack
                val others = state.queue.filter { it.id != current?.id }.shuffled()
                if (current != null) listOf(current) + others else state.queue.shuffled()
            } else {
                state.queue
            }
            val newIndex = updatedQueue.indexOfFirst { it.id == state.currentTrack?.id }.coerceAtLeast(0)
            state.copy(
                isShuffleEnabled = nextShuffle,
                queue = updatedQueue,
                currentIndex = newIndex
            )
        }
        notifyPlaybackService()
    }

    fun toggleRepeat() {
        _playerState.update { st ->
            val nextMode = when (st.repeatMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            st.copy(
                repeatMode = nextMode,
                isRepeatEnabled = nextMode != RepeatMode.OFF
            )
        }
        notifyPlaybackService()
    }

    fun toggleAutoPlay() {
        _playerState.update { it.copy(isAutoPlayEnabled = !it.isAutoPlayEnabled) }
    }

    fun setAutoPlayQueue(items: List<MusicItem>) {
        val currentId = _playerState.value.currentTrack?.id
        val filtered = items.filter { it.id != currentId }
        if (filtered.isNotEmpty()) {
            _playerState.update { st ->
                val combined = (filtered + st.autoPlayQueue)
                    .filter { it.id != currentId }
                    .distinctBy { "${it.id}_${it.title.lowercase()}" }
                    .take(25)
                st.copy(autoPlayQueue = combined)
            }
        }
    }

    fun removeTrackFromQueue(trackId: String) {
        _playerState.update { st ->
            val newQueue = st.queue.filterNot { it.id == trackId }
            val newAutoPlay = st.autoPlayQueue.filterNot { it.id == trackId }
            val newIdx = newQueue.indexOfFirst { it.id == st.currentTrack?.id }.coerceAtLeast(0)
            st.copy(
                queue = newQueue,
                autoPlayQueue = newAutoPlay,
                currentIndex = newIdx
            )
        }
    }

    fun playTrackFromQueueOrAutoPlay(track: MusicItem) {
        val st = _playerState.value
        val existingIdx = st.queue.indexOfFirst { it.id == track.id }
        if (existingIdx >= 0) {
            startTrackAt(st.queue, existingIdx, st.playingFromSource)
        } else {
            val remainingAuto = st.autoPlayQueue.filterNot { it.id == track.id }
            val newQueue = st.queue + track
            _playerState.update { it.copy(autoPlayQueue = remainingAuto) }
            startTrackAt(newQueue, newQueue.lastIndex, st.playingFromSource)
        }
    }

    fun playNextInQueue(
        tracks: List<MusicItem>,
        sourceLabel: String = "Playing next",
        resolveStream: (suspend (MusicItem) -> AudioStreamInfo?)? = null,
        onTrackStarted: ((MusicItem) -> Unit)? = null
    ) {
        if (tracks.isEmpty()) return
        if (resolveStream != null) currentStreamResolver = resolveStream
        if (onTrackStarted != null) currentTrackPlayedCallback = onTrackStarted
        val st = _playerState.value
        if (st.currentTrack == null || st.queue.isEmpty()) {
            startTrackAt(tracks, 0, sourceLabel)
            return
        }
        val currentIdx = st.currentIndex.coerceIn(0, st.queue.lastIndex)
        val before = st.queue.take(currentIdx + 1)
        val after = st.queue.drop(currentIdx + 1).filterNot { existing ->
            tracks.any { it.id == existing.id }
        }
        val updatedQueue = before + tracks + after
        _playerState.update {
            it.copy(
                queue = updatedQueue,
                currentIndex = currentIdx
            )
        }
        notifyPlaybackService()
    }

    fun addToQueue(
        tracks: List<MusicItem>,
        sourceLabel: String = "Added to queue",
        resolveStream: (suspend (MusicItem) -> AudioStreamInfo?)? = null,
        onTrackStarted: ((MusicItem) -> Unit)? = null
    ) {
        if (tracks.isEmpty()) return
        if (resolveStream != null) currentStreamResolver = resolveStream
        if (onTrackStarted != null) currentTrackPlayedCallback = onTrackStarted
        val st = _playerState.value
        if (st.currentTrack == null || st.queue.isEmpty()) {
            startTrackAt(tracks, 0, sourceLabel)
            return
        }
        val existingIds = st.queue.map { it.id }.toSet()
        val uniqueNew = tracks.filterNot { it.id in existingIds }.ifEmpty { tracks }
        val updatedQueue = st.queue + uniqueNew
        _playerState.update {
            it.copy(queue = updatedQueue)
        }
        notifyPlaybackService()
    }

    fun playTrackWithQueue(
        track: MusicItem,
        queue: List<MusicItem>,
        sourceLabel: String = "Playing from Pilihan cepat",
        resolveStream: suspend (MusicItem) -> AudioStreamInfo?,
        onTrackStarted: ((MusicItem) -> Unit)? = null
    ) {
        currentStreamResolver = resolveStream
        currentTrackPlayedCallback = onTrackStarted

        val effectiveQueue = if (queue.isNotEmpty()) queue else listOf(track)
        val startIndex = effectiveQueue.indexOfFirst { it.id == track.id }.takeIf { it >= 0 } ?: 0
        startTrackAt(effectiveQueue, startIndex, sourceLabel)
    }

    fun playNext() {
        playNextInternal(fromCrossfade = false)
    }

    private fun playNextInternal(fromCrossfade: Boolean) {
        val st = _playerState.value
        val q = st.queue
        if (q.isEmpty() && st.autoPlayQueue.isEmpty()) return

        if (st.repeatMode == RepeatMode.ONE && st.currentTrack != null) {
            startTrackAt(
                queue = q.ifEmpty { listOf(st.currentTrack) },
                index = st.currentIndex,
                sourceLabel = st.playingFromSource,
                useCrossfade = fromCrossfade
            )
            return
        }

        if (st.currentIndex + 1 < q.size) {
            startTrackAt(q, st.currentIndex + 1, st.playingFromSource, useCrossfade = fromCrossfade)
            return
        }

        if (st.repeatMode == RepeatMode.ALL && q.isNotEmpty()) {
            startTrackAt(q, 0, st.playingFromSource, useCrossfade = fromCrossfade)
            return
        }

        if (st.isAutoPlayEnabled && st.autoPlayQueue.isNotEmpty()) {
            val nextAutoTrack = st.autoPlayQueue.first()
            val remainingAuto = st.autoPlayQueue.drop(1)
            val updatedQueue = q + nextAutoTrack
            _playerState.update { it.copy(autoPlayQueue = remainingAuto) }
            startTrackAt(updatedQueue, updatedQueue.lastIndex, "AutoPlay • Lagu Serupa", useCrossfade = fromCrossfade)
            return
        }

        if (q.isNotEmpty()) {
            startTrackAt(q, 0, st.playingFromSource, useCrossfade = fromCrossfade)
        }
    }

    fun playPrevious() {
        val st = _playerState.value
        if (st.currentPositionSec > 4) {
            seekToFraction(0f)
            return
        }
        val q = st.queue
        if (q.isEmpty()) return
        val prevIndex = if (st.currentIndex - 1 < 0) q.lastIndex else st.currentIndex - 1
        startTrackAt(q, prevIndex, st.playingFromSource, useCrossfade = false)
    }

    private fun startTrackAt(
        queue: List<MusicItem>,
        index: Int,
        sourceLabel: String,
        useCrossfade: Boolean = false
    ) {
        val safeIndex = index.coerceIn(0, (queue.size - 1).coerceAtLeast(0))
        val track = queue.getOrNull(safeIndex) ?: return

        crossfadeTriggeredForTrackId = null
        val fadeSec = AppSettings.crossfadeSeconds.value
        if (useCrossfade && fadeSec > 0 && mediaPlayer != null && _playerState.value.isPlaying) {
            transitionToCrossfadeOutgoingPlayer()
        } else {
            stopInternal()
        }
        currentTrackPlayedCallback?.invoke(track)

        val upcomingFromContext = (queue.drop(safeIndex + 1) + queue.take(safeIndex))
            .filter { it.id != track.id }
            .distinctBy { it.id }

        val prevState = _playerState.value
        val initialAutoPlay = when {
            upcomingFromContext.isNotEmpty() -> upcomingFromContext
            prevState.autoPlayQueue.isNotEmpty() -> prevState.autoPlayQueue.filter { it.id != track.id }
            else -> com.example.data.remote.CuratedHomeFallback.getAutoPlayQueueFallback(track)
        }

        _playerState.update {
            prevState.copy(
                currentTrack = track,
                playingFromSource = sourceLabel,
                queue = queue,
                currentIndex = safeIndex,
                autoPlayQueue = initialAutoPlay,
                isPlaying = false,
                isLoading = true,
                progressFraction = 0f,
                currentPositionSec = 0,
                currentPositionMs = 0L,
                exactPositionSec = 0.0,
                durationSec = parseDurationSeconds(track.duration) ?: 187
            )
        }
        notifyPlaybackService()

        val resolver = currentStreamResolver
        scope.launch {
            val localPath = findLocalOfflinePath(track)
            if (localPath != null) {
                val localThumb = findLocalOfflineThumbnail(track)
                val updatedOfflineTrack = if (!localThumb.isNullOrBlank()) {
                    track.copy(thumbnailUrl = localThumb)
                } else {
                    track
                }
                startMediaPlayer(
                    track = updatedOfflineTrack,
                    streamInfo = AudioStreamInfo(
                        videoId = track.videoId ?: track.id,
                        url = localPath,
                        mimeType = if (localPath.endsWith(".mp3", ignoreCase = true)) "audio/mpeg" else "audio/mp4",
                        format = if (localPath.endsWith(".mp3", ignoreCase = true)) "Offline • MP3" else "Offline • Tersimpan",
                        bitrate = 256000,
                        durationSeconds = parseDurationSeconds(track.duration) ?: 187,
                        thumbnailUrl = localThumb
                    )
                )
                return@launch
            }

            val streamInfo = try {
                resolver?.invoke(track)
            } catch (_: Exception) {
                null
            }

            if (streamInfo != null && streamInfo.url.isNotBlank()) {
                val hdThumb = streamInfo.thumbnailUrl?.takeIf { it.isNotBlank() }
                val updatedTrack = if (
                    hdThumb != null &&
                    !track.thumbnailUrl.startsWith("file://") &&
                    !track.thumbnailUrl.startsWith("content://")
                ) {
                    track.copy(thumbnailUrl = hdThumb)
                } else {
                    track
                }
                startMediaPlayer(updatedTrack, streamInfo)
            } else {
                startFallbackPlaybackProgress(track)
            }
        }
    }

    private fun findLocalOfflineThumbnail(track: MusicItem): String? {
        if (track.thumbnailUrl.startsWith("file://") || track.thumbnailUrl.startsWith("content://")) {
            return track.thumbnailUrl
        }
        val ctx = appContext ?: return null
        val key = (track.videoId ?: track.id).replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val thumbDir = File(ctx.filesDir, "offline_thumbnails")
        val jpg = File(thumbDir, "$key.jpg")
        if (jpg.exists() && jpg.length() > 256L) {
            return "file://${jpg.absolutePath}"
        }
        return null
    }

    private fun findLocalOfflinePath(track: MusicItem): String? {
        val direct = track.localFilePath?.trim()
        if (!direct.isNullOrBlank()) {
            if (direct.startsWith("content://")) return direct
            val f = File(direct)
            if (f.exists() && f.length() > 1024L) return f.absolutePath
        }
        val ctx = appContext ?: return null
        val key = (track.videoId ?: track.id).replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val dir = File(ctx.filesDir, "offline_downloads")
        val mp3 = File(dir, "$key.mp3")
        if (mp3.exists() && mp3.length() > 1024L) return mp3.absolutePath
        val m4a = File(dir, "$key.m4a")
        if (m4a.exists() && m4a.length() > 1024L) return m4a.absolutePath
        val wav = File(dir, "$key.wav")
        if (wav.exists() && wav.length() > 1024L) return wav.absolutePath
        return null
    }

    private fun startMediaPlayer(track: MusicItem, streamInfo: AudioStreamInfo) {
        try {
            _playerState.update { st ->
                val updatedQueue = st.queue.map { qItem ->
                    if (qItem.id == track.id) track else qItem
                }
                st.copy(currentTrack = track, queue = updatedQueue)
            }
            val vol = _playerState.value.volumeFraction
            val mpGain = if (audioManager != null) {
                if (vol <= 0.005f) 0f else 1f
            } else {
                vol
            }
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                val ctx = appContext
                if (streamInfo.url.startsWith("content://") && ctx != null) {
                    setDataSource(ctx, Uri.parse(streamInfo.url))
                } else {
                    setDataSource(streamInfo.url)
                }
                setVolume(if (outgoingMediaPlayer != null) 0f else mpGain, if (outgoingMediaPlayer != null) 0f else mpGain)
                setOnPreparedListener { player ->
                    attachAndConfigureAudioEffects(player.audioSessionId)
                    player.start()
                    if (outgoingMediaPlayer != null && AppSettings.crossfadeSeconds.value > 0) {
                        runDualPlayerCrossfadeRamp(
                            incoming = player,
                            outgoing = outgoingMediaPlayer,
                            durationSec = AppSettings.crossfadeSeconds.value
                        )
                    } else {
                        applyStereoBalanceAndVolume()
                    }
                    val totalSec = (player.duration / 1000).takeIf { it > 0 }
                        ?: streamInfo.durationSeconds.takeIf { it > 0 }
                        ?: 187
                    _playerState.update {
                        it.copy(
                            currentTrack = track,
                            isPlaying = true,
                            isLoading = false,
                            durationSec = totalSec,
                            streamFormat = streamInfo.format
                        )
                    }
                    notifyPlaybackService()
                    startProgressPolling()
                }
                setOnCompletionListener { completedPlayer ->
                    if (completedPlayer !== mediaPlayer) return@setOnCompletionListener
                    progressJob?.cancel()
                    val st = _playerState.value
                    if (st.sleepTimerEndOfTrack) {
                        _playerState.update {
                            it.copy(
                                isPlaying = false,
                                progressFraction = 1f,
                                currentPositionSec = it.durationSec,
                                currentPositionMs = it.durationSec * 1000L,
                                exactPositionSec = it.durationSec.toDouble(),
                                sleepTimerEndOfTrack = false,
                                sleepTimerLabel = null,
                                sleepTimerRemainingSec = 0
                            )
                        }
                        notifyPlaybackService()
                    } else if (st.hasNext || st.isRepeatEnabled) {
                        playNext()
                    } else {
                        _playerState.update {
                            it.copy(
                                isPlaying = false,
                                progressFraction = 1f,
                                currentPositionSec = it.durationSec,
                                currentPositionMs = it.durationSec * 1000L,
                                exactPositionSec = it.durationSec.toDouble()
                            )
                        }
                        notifyPlaybackService()
                    }
                }
                setOnErrorListener { erroredPlayer, _, _ ->
                    if (erroredPlayer !== mediaPlayer) return@setOnErrorListener true
                    startFallbackPlaybackProgress(track)
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (_: Exception) {
            startFallbackPlaybackProgress(track)
        }
    }

    private fun transitionToCrossfadeOutgoingPlayer() {
        progressJob?.cancel()
        virtualPlaybackJob?.cancel()
        crossfadeJob?.cancel()
        try {
            outgoingMediaPlayer?.setOnCompletionListener(null)
            outgoingMediaPlayer?.setOnErrorListener(null)
            outgoingMediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            outgoingMediaPlayer?.release()
        } catch (_: Exception) {
        }
        // Detach listeners from the current player before moving it to outgoingMediaPlayer
        // so that when the outgoing track reaches its natural end during the crossfade ramp,
        // it NEVER fires onCompletion -> playNext() to skip the newly started incoming track!
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.setOnErrorListener(null)
        mediaPlayer?.setOnPreparedListener(null)
        outgoingMediaPlayer = mediaPlayer
        mediaPlayer = null
    }

    private fun runDualPlayerCrossfadeRamp(
        incoming: MediaPlayer,
        outgoing: MediaPlayer?,
        durationSec: Int
    ) {
        crossfadeJob?.cancel()
        val fadeMs = (durationSec.coerceIn(1, 12) * 1000L)
        crossfadeJob = scope.launch(Dispatchers.Main) {
            val steps = 25
            val stepDelay = (fadeMs / steps).coerceAtLeast(30L)
            val vol = _playerState.value.volumeFraction
            val baseGain = if (audioManager != null) {
                if (vol <= 0.005f) 0f else 1f
            } else {
                vol
            }
            val bal = AppSettings.equalizerBalance.value.coerceIn(-1f, 1f)
            val maxLeft = (baseGain * if (bal > 0f) (1f - bal) else 1f).coerceIn(0f, 1f)
            val maxRight = (baseGain * if (bal < 0f) (1f + bal) else 1f).coerceIn(0f, 1f)

            for (step in 1..steps) {
                if (!isActive) break
                val t = step.toFloat() / steps.toFloat()
                val inGainL = (maxLeft * t).coerceIn(0f, 1f)
                val inGainR = (maxRight * t).coerceIn(0f, 1f)
                val outGainL = (maxLeft * (1f - t)).coerceIn(0f, 1f)
                val outGainR = (maxRight * (1f - t)).coerceIn(0f, 1f)
                runCatching { incoming.setVolume(inGainL, inGainR) }
                runCatching { outgoing?.setVolume(outGainL, outGainR) }
                delay(stepDelay)
            }
            runCatching {
                outgoing?.setOnCompletionListener(null)
                outgoing?.setOnErrorListener(null)
                outgoing?.stop()
            }
            runCatching {
                outgoing?.release()
            }
            if (outgoingMediaPlayer === outgoing) {
                outgoingMediaPlayer = null
            }
            applyStereoBalanceAndVolume()
        }
    }

    /**
     * Adaptive position polling:
     * - When Full Player is expanded, polls at 65ms so lyrics & wavy seekbar stay locked.
     * - When Full Player is minimized (user scrolling Home/Library/Search), polls at 500ms
     *   and only emits state updates when seconds change, eliminating scroll lag/frame drops!
     * - Automatically triggers dual-player crossfade during the final N seconds of a song when Crossfade > 0s.
     */
    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Main) {
            var lastNotifiedSec = -1
            var lastEmittedSec = -1
            while (isActive) {
                val mp = mediaPlayer
                val currentState = _playerState.value
                val fullVisible = isFullPlayerVisible
                if (mp != null && currentState.isPlaying) {
                    try {
                        val posMs = mp.currentPosition.toLong().coerceAtLeast(0L)
                        val exactSec = posMs / 1000.0
                        val posSec = (posMs / 1000L).toInt()
                        val hardwareDurMs = runCatching { mp.duration.toLong() }.getOrDefault(0L)
                        val durMs = if (hardwareDurMs >= 5_000L) {
                            hardwareDurMs
                        } else {
                            (currentState.durationSec.coerceAtLeast(1) * 1000L)
                        }
                        val frac = (posMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f)

                        if (fullVisible || posSec != lastEmittedSec) {
                            lastEmittedSec = posSec
                            _playerState.update {
                                it.copy(
                                    currentPositionSec = posSec,
                                    currentPositionMs = posMs,
                                    exactPositionSec = exactSec,
                                    progressFraction = frac
                                )
                            }
                        }

                        // Check if we should trigger Crossfade into the next song:
                        // Require outgoingMediaPlayer == null (previous crossfade finished) and posMs >= 5000L
                        val fadeSec = AppSettings.crossfadeSeconds.value
                        val remainingMs = durMs - posMs
                        val trackId = currentState.currentTrack?.id
                        if (
                            fadeSec > 0 &&
                            outgoingMediaPlayer == null &&
                            posMs >= 5_000L &&
                            durMs > (fadeSec + 6) * 1000L &&
                            remainingMs in 400L..(fadeSec * 1000L) &&
                            !currentState.sleepTimerEndOfTrack &&
                            (currentState.hasNext || currentState.isRepeatEnabled) &&
                            trackId != null &&
                            crossfadeTriggeredForTrackId != trackId
                        ) {
                            crossfadeTriggeredForTrackId = trackId
                            playNextInternal(fromCrossfade = true)
                            break
                        }

                        if (posSec != lastNotifiedSec && posSec % 2 == 0) {
                            lastNotifiedSec = posSec
                            notifyPlaybackService()
                        }
                    } catch (_: Exception) {
                    }
                }
                delay(if (fullVisible) 65L else 500L)
            }
        }
    }

    private fun startFallbackPlaybackProgress(track: MusicItem) {
        val totalSec = parseDurationSeconds(track.duration) ?: 187
        _playerState.update {
            it.copy(
                currentTrack = track,
                isPlaying = true,
                isLoading = false,
                durationSec = totalSec,
                streamFormat = "m4a (aac)"
            )
        }
        notifyPlaybackService()
        virtualPlaybackJob?.cancel()
        virtualPlaybackJob = scope.launch(Dispatchers.Main) {
            var lastNotifiedSec = -1
            var internalMs = _playerState.value.currentPositionMs
            while (isActive) {
                val fullVisible = isFullPlayerVisible
                val stepMs = if (fullVisible) 65L else 500L
                delay(stepMs)
                val st = _playerState.value
                if (st.isPlaying) {
                    internalMs += stepMs
                    val nextExactSec = internalMs / 1000.0
                    val nextSec = (internalMs / 1000L).toInt()
                    val durMs = st.durationSec.coerceAtLeast(1) * 1000L
                    if (internalMs >= durMs) {
                        if (st.sleepTimerEndOfTrack) {
                            _playerState.update {
                                it.copy(
                                    isPlaying = false,
                                    currentPositionSec = st.durationSec,
                                    currentPositionMs = durMs,
                                    exactPositionSec = st.durationSec.toDouble(),
                                    progressFraction = 1f,
                                    sleepTimerEndOfTrack = false,
                                    sleepTimerLabel = null,
                                    sleepTimerRemainingSec = 0
                                )
                            }
                            notifyPlaybackService()
                        } else if (st.hasNext || st.isRepeatEnabled) {
                            playNext()
                        } else {
                            _playerState.update {
                                it.copy(
                                    isPlaying = false,
                                    currentPositionSec = st.durationSec,
                                    currentPositionMs = durMs,
                                    exactPositionSec = st.durationSec.toDouble(),
                                    progressFraction = 1f
                                )
                            }
                            notifyPlaybackService()
                        }
                        break
                    } else {
                        _playerState.update {
                            it.copy(
                                currentPositionSec = nextSec,
                                currentPositionMs = internalMs,
                                exactPositionSec = nextExactSec,
                                progressFraction = (internalMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f)
                            )
                        }
                        if (nextSec != lastNotifiedSec && nextSec % 2 == 0) {
                            lastNotifiedSec = nextSec
                            notifyPlaybackService()
                        }
                    }
                }
            }
        }
    }


    fun togglePlayPause() {
        val st = _playerState.value
        if (st.currentTrack == null) return

        val mp = mediaPlayer
        if (mp != null) {
            try {
                if (st.isPlaying) {
                    mp.pause()
                    _playerState.update { it.copy(isPlaying = false) }
                } else {
                    mp.start()
                    _playerState.update { it.copy(isPlaying = true) }
                }
                notifyPlaybackService()
                return
            } catch (_: Exception) {
            }
        }

        _playerState.update { it.copy(isPlaying = !it.isPlaying) }
        notifyPlaybackService()
    }

    fun pausePlayback() {
        val st = _playerState.value
        if (!st.isPlaying) return
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) mp.pause()
            }
        } catch (_: Exception) {
        }
        _playerState.update { it.copy(isPlaying = false) }
        notifyPlaybackService()
    }

    fun setSleepTimerMinutes(minutes: Int, label: String = "${minutes}m") {
        sleepTimerJob?.cancel()
        val totalSeconds = (minutes * 60).coerceAtLeast(1)
        _playerState.update {
            it.copy(
                sleepTimerLabel = label,
                sleepTimerRemainingSec = totalSeconds,
                sleepTimerEndOfTrack = false
            )
        }
        sleepTimerJob = scope.launch(Dispatchers.Main) {
            var remaining = totalSeconds
            while (isActive && remaining > 0) {
                delay(1000L)
                remaining -= 1
                val formattedLabel = if (remaining >= 3600) {
                    val h = remaining / 3600
                    val m = (remaining % 3600) / 60
                    if (m > 0) "${h}h ${m}m" else "${h}h"
                } else if (remaining >= 60) {
                    val m = (remaining + 59) / 60
                    "${m}m"
                } else {
                    "${remaining}s"
                }
                _playerState.update {
                    it.copy(
                        sleepTimerRemainingSec = remaining,
                        sleepTimerLabel = formattedLabel
                    )
                }
            }
            if (remaining <= 0) {
                pausePlayback()
                _playerState.update {
                    it.copy(
                        sleepTimerLabel = null,
                        sleepTimerRemainingSec = 0,
                        sleepTimerEndOfTrack = false
                    )
                }
            }
        }
    }

    fun setSleepTimerEndOfTrack() {
        sleepTimerJob?.cancel()
        _playerState.update {
            it.copy(
                sleepTimerLabel = "End of track",
                sleepTimerRemainingSec = 0,
                sleepTimerEndOfTrack = true
            )
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _playerState.update {
            it.copy(
                sleepTimerLabel = null,
                sleepTimerRemainingSec = 0,
                sleepTimerEndOfTrack = false
            )
        }
    }

    fun seekToFraction(fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        val durSec = _playerState.value.durationSec.coerceAtLeast(1)
        val targetMs = (clamped * durSec * 1000.0).toLong()
        val targetExactSec = targetMs / 1000.0
        val targetSec = (targetMs / 1000L).toInt()
        try {
            mediaPlayer?.seekTo(targetMs.toInt())
        } catch (_: Exception) {
        }
        _playerState.update {
            it.copy(
                progressFraction = clamped,
                currentPositionSec = targetSec,
                currentPositionMs = targetMs,
                exactPositionSec = targetExactSec
            )
        }
        notifyPlaybackService()
    }

    fun seekToPositionMs(positionMs: Long) {
        val durMs = (_playerState.value.durationSec.coerceAtLeast(1) * 1000L)
        val clampedMs = positionMs.coerceIn(0L, durMs)
        val fraction = (clampedMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f)
        seekToFraction(fraction)
    }

    fun dismissPlayer() {
        stopInternal()
        _playerState.value = PlayerUiState()
        appContext?.let { MusicPlaybackService.stop(it) }
    }

    private fun stopInternal() {
        progressJob?.cancel()
        virtualPlaybackJob?.cancel()
        crossfadeJob?.cancel()
        try {
            outgoingMediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            outgoingMediaPlayer?.release()
        } catch (_: Exception) {
        }
        outgoingMediaPlayer = null
        try {
            audioEqualizer?.release()
        } catch (_: Exception) {
        }
        audioEqualizer = null
        try {
            loudnessEnhancer?.release()
        } catch (_: Exception) {
        }
        loudnessEnhancer = null
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }

    private fun parseDurationSeconds(durationStr: String?): Int? {
        if (durationStr.isNullOrBlank()) return null
        val parts = durationStr.trim().split(":").mapNotNull { it.toIntOrNull() }
        return when (parts.size) {
            2 -> parts[0] * 60 + parts[1]
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            else -> null
        }
    }
}
