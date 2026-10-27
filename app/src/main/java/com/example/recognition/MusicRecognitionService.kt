package com.example.recognition

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.data.local.AppDatabase
import com.example.data.local.RecognitionHistoryEntity
import com.example.player.AudioPlayerController
import com.example.shazamkit.Shazam
import com.example.shazamkit.models.RecognitionStatus
import com.example.shazamkit.models.RecognitionUiState
import com.example.shazamkit.models.ShazamRecognitionResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

/**
 * MusicRecognitionService ported from Metrolist (`recognition/MusicRecognitionService.kt`).
 * Captures real-time PCM audio via Android AudioRecord (supporting both external real-life audio
 * and device/in-app playback), generates Shazam signatures via VibraSignature, queries Shazam's
 * discovery API, and persists recognized tracks into SQLite history.
 */
object MusicRecognitionService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow<RecognitionUiState>(RecognitionUiState.Idle)
    val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()

    private val _recognitionStatus = MutableStateFlow(RecognitionStatus.Ready)
    val recognitionStatus: StateFlow<RecognitionStatus> = _recognitionStatus.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private var recognitionJob: Job? = null

    @Volatile
    private var stopRecordingEarlyRequested: Boolean = false

    fun resetToIdle() {
        recognitionJob?.cancel()
        recognitionJob = null
        stopRecordingEarlyRequested = false
        _amplitude.value = 0f
        _recognitionStatus.value = RecognitionStatus.Ready
        _uiState.value = RecognitionUiState.Idle
    }

    fun cancelRecognition(context: Context? = null) {
        recognitionJob?.cancel()
        recognitionJob = null
        stopRecordingEarlyRequested = false
        _amplitude.value = 0f
        _recognitionStatus.value = RecognitionStatus.Ready
        _uiState.value = RecognitionUiState.Idle
        if (context != null) {
            RecognitionForegroundService.stop(context)
        }
    }

    /**
     * Triggered when the user taps the "Cukup" button while listening:
     * immediately stops recording and processes the captured audio sample.
     */
    fun finishListeningEarly() {
        if (_uiState.value is RecognitionUiState.Listening) {
            stopRecordingEarlyRequested = true
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecognition(context: Context) {
        recognitionJob?.cancel()
        stopRecordingEarlyRequested = false
        val appContext = context.applicationContext

        RecognitionForegroundService.start(appContext)

        recognitionJob = scope.launch {
            _recognitionStatus.value = RecognitionStatus.Listening
            _uiState.value = RecognitionUiState.Listening(
                amplitude = 0f,
                elapsedSeconds = 0,
                canFinishEarly = false
            )

            val candidateRates = intArrayOf(16000, 44100, 48000)
            var selectedRate = 16000
            var minBufferSize = AudioRecord.ERROR_BAD_VALUE

            for (rate in candidateRates) {
                val size = AudioRecord.getMinBufferSize(
                    rate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                if (size > 0) {
                    selectedRate = rate
                    minBufferSize = size
                    break
                }
            }

            val bufferSize = if (minBufferSize > 0) {
                (minBufferSize * 2).coerceAtLeast(selectedRate)
            } else {
                selectedRate * 2
            }

            var audioRecord: AudioRecord? = null
            val audioSources = intArrayOf(
                MediaRecorder.AudioSource.UNPROCESSED,
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                MediaRecorder.AudioSource.DEFAULT
            )

            for (source in audioSources) {
                try {
                    val candidate = AudioRecord(
                        source,
                        selectedRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                    )
                    if (candidate.state == AudioRecord.STATE_INITIALIZED) {
                        audioRecord = candidate
                        break
                    } else {
                        candidate.release()
                    }
                } catch (_: Throwable) {
                }
            }

            val signatureGenerator = ShazamSignatureGenerator()
            val startTimeMs = System.currentTimeMillis()
            val maxRecordDurationMs = 11_000L
            var matchedResult: ShazamRecognitionResult? = null
            var lastQueryElapsedMs = 0L

            try {
                audioRecord?.startRecording()
                val readChunk = ShortArray(2048)

                while (isActive) {
                    val now = System.currentTimeMillis()
                    val elapsedMs = now - startTimeMs
                    val elapsedSec = (elapsedMs / 1000L).toInt()
                    val canStopEarly = elapsedMs >= 2800L

                    if (stopRecordingEarlyRequested || elapsedMs >= maxRecordDurationMs) {
                        break
                    }

                    val readCount = try {
                        audioRecord?.read(readChunk, 0, readChunk.size) ?: 0
                    } catch (_: Throwable) {
                        0
                    }

                    if (readCount > 0) {
                        // Compute RMS amplitude for smooth ripple rings
                        var sumSquares = 0.0
                        for (i in 0 until readCount) {
                            val norm = readChunk[i] / 32768.0
                            sumSquares += norm * norm
                        }
                        val rms = sqrt(sumSquares / readCount).toFloat()
                        val visualAmp = (rms * 5.5f).coerceIn(0.06f, 1.0f)
                        _amplitude.value = visualAmp
                        _uiState.value = RecognitionUiState.Listening(
                            amplitude = visualAmp,
                            elapsedSeconds = elapsedSec,
                            canFinishEarly = canStopEarly
                        )

                        val slice = readChunk.copyOfRange(0, readCount)
                        val resampled = AudioResampler.resampleTo16kHzMono(
                            inputSamples = slice,
                            inputSampleRate = selectedRate,
                            channelCount = 1
                        )
                        signatureGenerator.feedPcm(resampled)
                    } else {
                        // Gentle breathing amplitude if mic stream is quiet
                        val fallbackAmp = 0.12f
                        _amplitude.value = fallbackAmp
                        _uiState.value = RecognitionUiState.Listening(
                            amplitude = fallbackAmp,
                            elapsedSeconds = elapsedSec,
                            canFinishEarly = canStopEarly
                        )
                        delay(40)
                    }

                    // Progressive Shazam query at ~3.6s and ~7.0s while continuing to listen
                    if (elapsedMs >= 3600L && (elapsedMs - lastQueryElapsedMs) >= 3400L && !stopRecordingEarlyRequested) {
                        lastQueryElapsedMs = elapsedMs
                        val partialUri = signatureGenerator.createSignatureDataUri()
                        val attempt = Shazam.recognize(
                            signatureDataUri = partialUri,
                            sampleDurationMs = elapsedMs
                        ).getOrNull()
                        if (attempt != null) {
                            matchedResult = attempt
                            break
                        }
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Throwable) {
            } finally {
                try {
                    audioRecord?.stop()
                } catch (_: Throwable) {
                }
                try {
                    audioRecord?.release()
                } catch (_: Throwable) {
                }
                RecognitionForegroundService.stop(appContext)
            }

            if (!isActive) return@launch

            // Transition to State 3: PROCESSING (shows Golden Harp Logo animation)
            _amplitude.value = 0f
            _recognitionStatus.value = RecognitionStatus.Processing
            _uiState.value = RecognitionUiState.Processing

            val totalElapsedMs = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(3000L)

            if (matchedResult == null) {
                val finalSignatureUri = signatureGenerator.createSignatureDataUri()
                val shazamAttempt = Shazam.recognize(
                    signatureDataUri = finalSignatureUri,
                    sampleDurationMs = totalElapsedMs
                )
                matchedResult = shazamAttempt.getOrNull()
            }

            // Ensure the Golden Harp processing animation is visible smoothly for at least 950ms
            delay(950)

            // Fallback: if audio is currently playing inside Lyra itself and acoustic loopback wasn't captured by mic
            if (matchedResult == null) {
                val inAppState = AudioPlayerController.state.value
                val inAppTrack = inAppState.currentTrack
                if (inAppState.isPlaying && inAppTrack != null && inAppTrack.title.isNotBlank()) {
                    matchedResult = ShazamRecognitionResult(
                        trackId = inAppTrack.videoId ?: inAppTrack.id,
                        title = inAppTrack.title,
                        artist = inAppTrack.subtitle.substringBefore("•").trim().ifBlank { inAppTrack.subtitle },
                        album = inAppTrack.subtitle.takeIf { "•" in it }?.substringAfter("•")?.trim(),
                        coverArtUrl = inAppTrack.thumbnailUrl,
                        coverArtHqUrl = inAppTrack.thumbnailUrl,
                        genres = emptyList(),
                        youtubeUrl = inAppTrack.videoId?.let { "https://music.youtube.com/watch?v=$it" }
                    )
                }
            }

            if (matchedResult != null) {
                saveToHistory(appContext, matchedResult)
                _recognitionStatus.value = RecognitionStatus.Success
                _uiState.value = RecognitionUiState.Success(matchedResult)
            } else {
                _recognitionStatus.value = RecognitionStatus.NoMatch
                _uiState.value = RecognitionUiState.NoMatch(
                    "Lagu tidak ditemukan. Pastikan sumber musik terdengar jelas lalu coba lagi."
                )
            }
        }
    }

    private suspend fun saveToHistory(context: Context, result: ShazamRecognitionResult) {
        withContext(Dispatchers.IO) {
            runCatching {
                val dao = AppDatabase.getInstance(context).recentTrackDao()
                dao.insertRecognitionHistory(
                    RecognitionHistoryEntity(
                        trackId = result.trackId,
                        title = result.title,
                        artist = result.artist,
                        album = result.album,
                        coverArtUrl = result.coverArtUrl,
                        coverArtHqUrl = result.coverArtHqUrl ?: result.coverArtUrl,
                        genre = result.genres.firstOrNull(),
                        releaseDate = result.releaseDate,
                        label = result.label,
                        isrc = result.isrc,
                        shazamUrl = result.shazamUrl,
                        appleMusicUrl = result.appleMusicUrl,
                        spotifyUrl = result.spotifyUrl,
                        youtubeVideoId = result.youtubeUrl,
                        recognizedAt = System.currentTimeMillis(),
                        liked = false
                    )
                )
            }
        }
    }
}
