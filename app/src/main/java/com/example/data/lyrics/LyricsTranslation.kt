package com.example.data.lyrics

import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

object LyricsTranslation {
    private const val TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single"
    private const val BROWSER_USER_AGENT = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    private val translationCache = LruCache<String, List<LyricLine>>(64)
    private val romanizationCache = LruCache<String, List<LyricLine>>(64)

    fun normalizeTargetLanguage(rawLang: String): String {
        val trimmed = rawLang.trim()
        if (trimmed.isEmpty()) {
            val sysLang = Locale.getDefault().language.lowercase(Locale.ROOT)
            return when {
                sysLang == "in" || sysLang.isBlank() -> "id"
                else -> sysLang
            }
        }
        return when (trimmed.lowercase(Locale.ROOT)) {
            "in" -> "id"
            "iw" -> "he"
            "ji" -> "yi"
            else -> trimmed
        }
    }

    suspend fun translate(
        lines: List<LyricLine>,
        targetLang: String,
    ): List<LyricLine>? = withContext(Dispatchers.IO) {
        if (lines.isEmpty()) return@withContext null
        val effectiveLang = normalizeTargetLanguage(targetLang)
        val nonGapIndices = lines.indices.filter { !lines[it].isGap && lines[it].text.isNotBlank() }
        if (nonGapIndices.isEmpty()) return@withContext null

        val cacheKey = buildString {
            append(effectiveLang).append('|')
            append(lines.size).append('|')
            append(lines.take(8).joinToString { it.text }.hashCode())
        }
        translationCache.get(cacheKey)?.let { return@withContext it }

        val sourceTexts = nonGapIndices.map { lines[it].text }
        val translatedTexts = translateBatch(sourceTexts, effectiveLang) ?: return@withContext null

        val mapByIndex = HashMap<Int, String>(nonGapIndices.size)
        for (i in nonGapIndices.indices) {
            val t = translatedTexts.getOrNull(i)?.trim().orEmpty()
            if (t.isNotEmpty()) {
                mapByIndex[nonGapIndices[i]] = t
            }
        }

        if (mapByIndex.isEmpty()) return@withContext null

        val out = lines.mapIndexed { idx, original ->
            val translated = mapByIndex[idx]?.takeIf { it.isNotBlank() } ?: return@mapIndexed original
            original.copy(
                text = translated,
                words = emptyList(),
                timingSource = original,
            )
        }
        translationCache.put(cacheKey, out)
        out
    }

    suspend fun romanize(
        lines: List<LyricLine>,
    ): List<LyricLine>? = withContext(Dispatchers.IO) {
        if (lines.isEmpty()) return@withContext null
        val nonGapIndices = lines.indices.filter { !lines[it].isGap && lines[it].text.isNotBlank() }
        if (nonGapIndices.isEmpty()) return@withContext null

        val hasNonLatin = nonGapIndices.any { idx ->
            lines[idx].text.any { ch -> ch.code > 0x024F && ch.isLetter() }
        }
        if (!hasNonLatin) return@withContext null

        val cacheKey = "rom|${lines.size}|${lines.take(8).joinToString { it.text }.hashCode()}"
        romanizationCache.get(cacheKey)?.let { return@withContext it }

        val sourceTexts = nonGapIndices.map { lines[it].text }
        val romanizedTexts = romanizeBatch(sourceTexts) ?: return@withContext null

        val mapByIndex = HashMap<Int, String>(nonGapIndices.size)
        for (i in nonGapIndices.indices) {
            val r = romanizedTexts.getOrNull(i)?.trim().orEmpty()
            if (r.isNotEmpty()) {
                mapByIndex[nonGapIndices[i]] = r
            }
        }

        if (mapByIndex.isEmpty()) return@withContext null

        val out = lines.mapIndexed { idx, original ->
            val rom = mapByIndex[idx] ?: return@mapIndexed original
            original.copy(
                text = rom,
                words = emptyList(),
                timingSource = original,
            )
        }
        romanizationCache.put(cacheKey, out)
        out
    }

    private suspend fun translateBatch(
        texts: List<String>,
        targetLang: String,
    ): List<String>? {
        // Primary: BitChord marker-delimited POST request via client=gtx
        val markerResult = translateViaMarkerPost(texts, targetLang)
        if (markerResult != null && markerResult.any { it.isNotBlank() }) {
            return markerResult
        }

        // Secondary: Chunked GET via client=gtx / client=dict-chrome-ex
        val result = ArrayList<String>(texts.size)
        val chunkSize = 12
        for (chunk in texts.chunked(chunkSize)) {
            val joined = chunk.joinToString("\n")
            val encoded = URLEncoder.encode(joined, "UTF-8")
            val tlEncoded = URLEncoder.encode(targetLang, "UTF-8")
            val gtxUrl = "$TRANSLATE_URL?client=gtx&sl=auto&tl=$tlEncoded&dt=t&q=$encoded"
            val raw = lyricsGet(gtxUrl, userAgent = BROWSER_USER_AGENT)
                ?: lyricsGet(
                    "$TRANSLATE_URL?client=dict-chrome-ex&sl=auto&tl=$tlEncoded&dt=t&q=$encoded",
                    userAgent = BROWSER_USER_AGENT,
                )
            var parsedChunk = if (raw != null) parseTranslatedLines(raw, chunk.size) else null

            if (parsedChunk == null || parsedChunk.all { it.isBlank() }) {
                parsedChunk = translateMyMemoryChunk(chunk, targetLang)
            }

            if (parsedChunk != null) {
                result.addAll(parsedChunk)
            } else {
                result.addAll(chunk)
            }
        }
        return if (result.isNotEmpty()) result else null
    }

    private fun translateViaMarkerPost(
        texts: List<String>,
        targetLang: String,
    ): List<String>? {
        return try {
            val marked = texts.mapIndexed { idx, line ->
                "\uE000$idx\uE001 ${line.replace('\n', ' ').trim()}"
            }.joinToString("\n")
            val tlEncoded = URLEncoder.encode(targetLang, "UTF-8")
            val url = "$TRANSLATE_URL?client=gtx&sl=auto&tl=$tlEncoded&dt=t"
            val body = "q=" + URLEncoder.encode(marked, "UTF-8")

            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 8_000
                readTimeout = 10_000
                setRequestProperty("User-Agent", BROWSER_USER_AGENT)
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            }
            val rawJson = try {
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                if (conn.responseCode !in 200..299) return null
                conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } finally {
                conn.disconnect()
            }

            val root = JSONArray(rawJson)
            val sentences = root.optJSONArray(0) ?: return null
            val combined = StringBuilder()
            for (i in 0 until sentences.length()) {
                val seg = sentences.optJSONArray(i) ?: continue
                val piece = seg.optString(0, "")
                if (piece.isNotEmpty()) combined.append(piece)
            }

            val fullText = combined.toString()
            val markerRegex = Regex("\uE000(\\d+)\uE001")
            val matches = markerRegex.findAll(fullText).toList()
            if (matches.isEmpty()) {
                val splitLines = fullText.split("\n").map { it.trim() }
                return if (splitLines.size == texts.size) splitLines else null
            }

            val out = MutableList(texts.size) { "" }
            for (mIdx in matches.indices) {
                val match = matches[mIdx]
                val lineIdx = match.groupValues[1].toIntOrNull() ?: continue
                val start = match.range.last + 1
                val end = if (mIdx + 1 < matches.size) matches[mIdx + 1].range.first else fullText.length
                if (lineIdx in out.indices && start <= end) {
                    out[lineIdx] = fullText.substring(start, end).trim()
                }
            }
            out
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun translateMyMemoryChunk(chunk: List<String>, targetLang: String): List<String>? {
        val result = ArrayList<String>(chunk.size)
        val langPair = "autodetect|${URLEncoder.encode(targetLang, "UTF-8")}"
        for (line in chunk) {
            if (line.isBlank()) {
                result.add("")
                continue
            }
            try {
                val encoded = URLEncoder.encode(line, "UTF-8")
                val url = "https://api.mymemory.translated.net/get?q=$encoded&langpair=$langPair"
                val raw = lyricsGet(url, userAgent = BROWSER_USER_AGENT)
                if (raw != null) {
                    val jsonObj = JSONObject(raw)
                    val respData = jsonObj.optJSONObject("responseData")
                    val translatedText = respData?.optString("translatedText", "")?.trim().orEmpty()
                    if (translatedText.isNotEmpty()) {
                        result.add(translatedText)
                    } else {
                        result.add(line)
                    }
                } else {
                    result.add(line)
                }
            } catch (_: Exception) {
                result.add(line)
            }
        }
        return result
    }

    private suspend fun romanizeBatch(texts: List<String>): List<String>? {
        val result = ArrayList<String>(texts.size)
        val chunkSize = 15
        for (chunk in texts.chunked(chunkSize)) {
            val joined = chunk.joinToString("\n")
            val encoded = URLEncoder.encode(joined, "UTF-8")
            val url = "$TRANSLATE_URL?client=gtx&sl=auto&tl=en&dt=rm&dt=t&q=$encoded"
            val raw = lyricsGet(url, userAgent = BROWSER_USER_AGENT)
            val parsed = if (raw != null) parseRomanizedLines(raw, chunk) else null
            if (parsed != null) {
                result.addAll(parsed)
            } else {
                result.addAll(chunk)
            }
        }
        return if (result.isNotEmpty()) result else null
    }

    private fun parseTranslatedLines(rawJson: String, expectedCount: Int): List<String>? {
        return try {
            val root = JSONArray(rawJson)
            val sentences = root.optJSONArray(0) ?: return null
            val sb = StringBuilder()
            for (i in 0 until sentences.length()) {
                val item = sentences.optJSONArray(i) ?: continue
                val piece = item.optString(0, "")
                sb.append(piece)
            }
            val lines = sb.toString().split("\n").map { it.trim() }
            if (lines.size == expectedCount) {
                lines
            } else if (lines.size > expectedCount) {
                lines.take(expectedCount)
            } else {
                lines + List(expectedCount - lines.size) { "" }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseRomanizedLines(rawJson: String, fallbackChunk: List<String>): List<String>? {
        return try {
            val root = JSONArray(rawJson)
            val sentences = root.optJSONArray(0) ?: return null
            val sb = StringBuilder()
            for (i in 0 until sentences.length()) {
                val item = sentences.optJSONArray(i) ?: continue
                val rom = item.optString(3, "").ifBlank { item.optString(2, "") }
                if (rom.isNotBlank()) {
                    if (sb.isNotEmpty() && !sb.endsWith("\n")) sb.append(" ")
                    sb.append(rom)
                }
            }
            val romLines = sb.toString().split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            if (romLines.size == fallbackChunk.size) {
                romLines
            } else if (sb.isNotBlank()) {
                val rawSplit = sb.toString().lines().map { it.trim() }
                fallbackChunk.mapIndexed { idx, orig ->
                    rawSplit.getOrNull(idx)?.takeIf { it.isNotEmpty() } ?: orig
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
