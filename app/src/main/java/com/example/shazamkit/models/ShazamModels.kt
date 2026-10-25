package com.example.shazamkit.models

data class ShazamRecognitionResult(
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val coverArtUrl: String? = null,
    val coverArtHqUrl: String? = null,
    val genres: List<String> = emptyList(),
    val releaseDate: String? = null,
    val label: String? = null,
    val isrc: String? = null,
    val shazamUrl: String? = null,
    val appleMusicUrl: String? = null,
    val spotifyUrl: String? = null,
    val youtubeUrl: String? = null,
    val lyrics: List<String>? = null
)

data class RecognizedTrack(
    val key: String = "",
    val title: String = "",
    val subtitle: String = "",
    val album: String? = null,
    val coverArtUrl: String? = null,
    val coverArtHqUrl: String? = null,
    val genre: String? = null,
    val releaseDate: String? = null,
    val label: String? = null,
    val isrc: String? = null,
    val shazamUrl: String? = null,
    val appleMusicUrl: String? = null,
    val youtubeUrl: String? = null,
    val lyrics: List<String>? = null
)

enum class RecognitionStatus {
    Ready,
    Listening,
    Processing,
    Success,
    NoMatch,
    Error
}

sealed class RecognitionUiState {
    object Idle : RecognitionUiState()
    data class Listening(
        val amplitude: Float = 0f,
        val elapsedSeconds: Int = 0,
        val canFinishEarly: Boolean = false
    ) : RecognitionUiState()
    object Processing : RecognitionUiState()
    data class Success(val result: ShazamRecognitionResult) : RecognitionUiState()
    data class NoMatch(val message: String = "Lagu tidak dikenali. Coba dekatkan sumber suara.") : RecognitionUiState()
    data class Error(val message: String) : RecognitionUiState()
}
