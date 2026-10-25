package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists remotely-discovered playlist/album cover URLs (keyed by clean ID
 * without the `VL` prefix) so Library cards can display the user's custom
 * YouTube Music playlist cover even when the `FEmusic_liked_playlists` grid
 * only returned a generic or stale thumbnail.
 */
object RemoteArtworkStore {
    private const val PREFS = "bitchord_remote_artwork"
    private lateinit var prefs: SharedPreferences

    private val _overrides = MutableStateFlow<Map<String, String>>(emptyMap())
    val overrides: StateFlow<Map<String, String>> = _overrides.asStateFlow()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val loaded = buildMap<String, String> {
            for ((k, v) in prefs.all) {
                if (v is String && v.isNotBlank()) put(k, v)
            }
        }
        _overrides.value = loaded
    }

    fun get(id: String): String? {
        val clean = id.removePrefix("VL")
        return _overrides.value[clean] ?: _overrides.value[id]
    }

    fun getPlaylistCoverOverride(id: String): String? = get(id)

    fun put(id: String, url: String) {
        val clean = id.removePrefix("VL").trim()
        val trimmedUrl = url.trim()
        if (clean.isEmpty() || trimmedUrl.isEmpty()) return
        if (_overrides.value[clean] == trimmedUrl) return
        _overrides.value = _overrides.value + (clean to trimmedUrl)
        if (::prefs.isInitialized) {
            prefs.edit().putString(clean, trimmedUrl).apply()
        }
    }

    fun setPlaylistCoverOverride(id: String, url: String) = put(id, url)
}
