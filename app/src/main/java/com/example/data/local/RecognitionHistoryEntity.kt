package com.example.data.local

data class RecognitionHistoryEntity(
    val id: Long = 0L,
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val coverArtUrl: String? = null,
    val coverArtHqUrl: String? = null,
    val genre: String? = null,
    val releaseDate: String? = null,
    val label: String? = null,
    val isrc: String? = null,
    val shazamUrl: String? = null,
    val appleMusicUrl: String? = null,
    val spotifyUrl: String? = null,
    val youtubeVideoId: String? = null,
    val recognizedAt: Long = System.currentTimeMillis(),
    val liked: Boolean = false
)
