package com.example.shazamkit

import com.example.shazamkit.models.ShazamRecognitionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Shazam API Client from Metrolist (`shazamkit/Shazam.kt`).
 * Sends acoustic signatures (`data:audio/vnd.shazam.sig;base64,...`) to Shazam's discovery endpoint
 * and parses the matched track metadata.
 */
object Shazam {

    private val USER_AGENTS = listOf(
        "Dalvik/2.1.0 (Linux; U; Android 14; Pixel 8 Pro Build/UQ1A.240205.004)",
        "Dalvik/2.1.0 (Linux; U; Android 13; SM-S918B Build/TP1A.220624.014)",
        "Shazam/14.18.0 (Android/14; Pixel 8; en_US)"
    )

    suspend fun recognize(
        signatureDataUri: String,
        sampleDurationMs: Long = 3500L
    ): Result<ShazamRecognitionResult?> = withContext(Dispatchers.IO) {
        runCatching {
            val deviceId = UUID.randomUUID().toString().uppercase(Locale.US)
            val requestId = UUID.randomUUID().toString().uppercase(Locale.US)
            val language = Locale.getDefault().language.ifBlank { "en" }
            val country = Locale.getDefault().country.ifBlank { "US" }
            val endpoint =
                "https://amp.shazam.com/discovery/v5/$language/$country/android/-/tag/$deviceId/$requestId" +
                    "?sync=true&webv3=true&sampling=true&connected=&shazamapiversion=v3&sharehub=true&video=v3"

            val timestamp = System.currentTimeMillis()
            val timezone = TimeZone.getDefault().id.ifBlank { "Asia/Jakarta" }

            val bodyJson = JSONObject().apply {
                put("geolocation", JSONObject().apply {
                    put("altitude", 300)
                    put("latitude", -6.2088)
                    put("longitude", 106.8456)
                })
                put("signature", JSONObject().apply {
                    put("samplems", sampleDurationMs.coerceIn(2000L, 12000L))
                    put("timestamp", timestamp)
                    put("uri", signatureDataUri)
                })
                put("timestamp", timestamp)
                put("timezone", timezone)
            }

            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doInput = true
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Accept-Language", "$language-$country,$language;q=0.9,en-US;q=0.8")
                setRequestProperty("User-Agent", USER_AGENTS.random())
                setRequestProperty("X-Shazam-Platform", "ANDROID")
                setRequestProperty("X-Shazam-AppVersion", "14.18.0")
            }

            try {
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(bodyJson.toString())
                    writer.flush()
                }

                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.let { s ->
                    BufferedReader(InputStreamReader(s, Charsets.UTF_8)).use { it.readText() }
                }.orEmpty()

                if (code !in 200..299 || responseText.isBlank()) {
                    return@runCatching null
                }

                parseShazamResponse(JSONObject(responseText))
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun parseShazamResponse(root: JSONObject): ShazamRecognitionResult? {
        val matches = root.optJSONArray("matches")
        val trackObj = root.optJSONObject("track") ?: return null
        if (matches != null && matches.length() == 0 && !root.has("track")) {
            return null
        }

        val key = trackObj.optString("key").ifBlank { UUID.randomUUID().toString() }
        val title = trackObj.optString("title").trim()
        val subtitle = trackObj.optString("subtitle").trim()
        if (title.isBlank()) return null

        val imagesObj = trackObj.optJSONObject("images")
        val coverArt = imagesObj?.optString("coverart")?.takeIf { it.isNotBlank() }
            ?: imagesObj?.optString("background")?.takeIf { it.isNotBlank() }
        val coverArtHq = imagesObj?.optString("coverarthq")?.takeIf { it.isNotBlank() }
            ?: coverArt?.replace("400x400", "800x800")

        val genresList = mutableListOf<String>()
        val genresObj = trackObj.optJSONObject("genres")
        val primaryGenre = genresObj?.optString("primary")?.trim().orEmpty()
        if (primaryGenre.isNotBlank()) genresList.add(primaryGenre)

        val isrc = trackObj.optString("isrc").takeIf { it.isNotBlank() }
        val shazamUrl = trackObj.optString("url").takeIf { it.isNotBlank() }

        var album: String? = null
        var releaseDate: String? = null
        var label: String? = null
        var lyricsLines: List<String>? = null
        var youtubeUrl: String? = null

        val sections = trackObj.optJSONArray("sections") ?: JSONArray()
        for (i in 0 until sections.length()) {
            val section = sections.optJSONObject(i) ?: continue
            when (section.optString("type")) {
                "SONG" -> {
                    val metadata = section.optJSONArray("metadata") ?: JSONArray()
                    for (m in 0 until metadata.length()) {
                        val item = metadata.optJSONObject(m) ?: continue
                        val metaTitle = item.optString("title").lowercase(Locale.ROOT)
                        val metaText = item.optString("text").trim()
                        if (metaText.isNotBlank()) {
                            when {
                                "album" in metaTitle -> album = metaText
                                "released" in metaTitle || "rilis" in metaTitle -> releaseDate = metaText
                                "label" in metaTitle -> label = metaText
                            }
                        }
                    }
                }
                "LYRICS" -> {
                    val textArr = section.optJSONArray("text")
                    if (textArr != null && textArr.length() > 0) {
                        val lines = ArrayList<String>(textArr.length())
                        for (l in 0 until textArr.length()) {
                            val line = textArr.optString(l)
                            if (line != null) lines.add(line)
                        }
                        lyricsLines = lines
                    }
                }
                "VIDEO" -> {
                    val yt = section.optJSONObject("youtubeurl")
                    val actions = yt?.optJSONArray("actions")
                    if (actions != null && actions.length() > 0) {
                        youtubeUrl = actions.optJSONObject(0)?.optString("uri")?.takeIf { it.isNotBlank() }
                    }
                }
            }
        }

        var appleMusicUrl: String? = null
        var spotifyUrl: String? = null
        val hub = trackObj.optJSONObject("hub")
        val providers = hub?.optJSONArray("providers") ?: JSONArray()
        for (p in 0 until providers.length()) {
            val provider = providers.optJSONObject(p) ?: continue
            val providerType = provider.optString("type").uppercase(Locale.ROOT)
            val actions = provider.optJSONArray("actions") ?: JSONArray()
            for (a in 0 until actions.length()) {
                val uri = actions.optJSONObject(a)?.optString("uri").orEmpty()
                if (uri.isNotBlank()) {
                    if ("SPOTIFY" in providerType && spotifyUrl == null) spotifyUrl = uri
                    if ("APPLE" in providerType && appleMusicUrl == null) appleMusicUrl = uri
                }
            }
        }

        return ShazamRecognitionResult(
            trackId = key,
            title = title,
            artist = subtitle.ifBlank { "Unknown Artist" },
            album = album,
            coverArtUrl = coverArt,
            coverArtHqUrl = coverArtHq ?: coverArt,
            genres = genresList,
            releaseDate = releaseDate,
            label = label,
            isrc = isrc,
            shazamUrl = shazamUrl,
            appleMusicUrl = appleMusicUrl,
            spotifyUrl = spotifyUrl,
            youtubeUrl = youtubeUrl,
            lyrics = lyricsLines
        )
    }
}
