package com.example.data.innertube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.TimeUnit

class Innertube(
    private val http: OkHttpClient = defaultHttpClient()
) {
    var cookie: String? = null
    var visitorData: String? = null
    var dataSyncId: String? = null
    var pageId: String? = null
    var authUser: String? = null
    var gl: String = Locale.getDefault().country.takeIf { it.length == 2 } ?: "ID"
    var hl: String = Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "id"

    companion object {
        private const val BASE_URL = "https://music.youtube.com/youtubei/v1"
        private const val ORIGIN = "https://music.youtube.com"
        private const val WEB_CLIENT_NAME = "WEB_REMIX"
        private const val WEB_CLIENT_VERSION = "1.20250312.01.00"
        private const val WEB_CLIENT_ID = "67"
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()

        fun extractSapSid(cookie: String): String? {
            val map = parseCookieMap(cookie)
            return map["SAPISID"] ?: map["__Secure-3PAPISID"] ?: map["__Secure-1PAPISID"]
        }

        private fun parseCookieMap(cookie: String): Map<String, String> =
            cookie.split(';')
                .mapNotNull { part ->
                    val eq = part.indexOf('=')
                    if (eq <= 0) null
                    else part.substring(0, eq).trim() to part.substring(eq + 1).trim()
                }
                .toMap()

        private fun sha1Hex(input: String): String {
            val digest = MessageDigest.getInstance("SHA-1").digest(input.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }

        fun buildSapSidHash(cookie: String, origin: String = ORIGIN): String? {
            val sapisid = extractSapSid(cookie) ?: return null
            val ts = System.currentTimeMillis() / 1000
            val hash = sha1Hex("$ts $sapisid $origin")
            return "SAPISIDHASH ${ts}_$hash SAPISID1PHASH ${ts}_$hash SAPISID3PHASH ${ts}_$hash"
        }
    }

    private fun webContext(): JSONObject = JSONObject().apply {
        put("client", JSONObject().apply {
            put("clientName", WEB_CLIENT_NAME)
            put("clientVersion", WEB_CLIENT_VERSION)
            put("gl", gl)
            put("hl", hl)
            visitorData?.let { put("visitorData", it) }
        })
        put("user", JSONObject().apply {
            put("lockedSafetyMode", false)
            pageId?.takeIf { it.isNotBlank() }?.let { put("onBehalfOfUser", it) }
        })
    }

    private suspend fun postJson(
        endpoint: String,
        queryParams: Map<String, String> = emptyMap(),
        bodyBuilder: JSONObject.() -> Unit
    ): JSONObject = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("context", webContext())
            bodyBuilder()
        }
        val queryString = buildString {
            append("prettyPrint=false")
            for ((k, v) in queryParams) {
                append("&").append(k).append("=").append(java.net.URLEncoder.encode(v, "UTF-8"))
            }
        }
        val url = "$BASE_URL/$endpoint?$queryString"
        val reqBuilder = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .header("User-Agent", USER_AGENT)
            .header("Origin", ORIGIN)
            .header("Referer", "$ORIGIN/")
            .header("X-Goog-Api-Format-Version", "2")
            .header("X-YouTube-Client-Name", WEB_CLIENT_ID)
            .header("X-YouTube-Client-Version", WEB_CLIENT_VERSION)

        visitorData?.takeIf { it.isNotBlank() }?.let {
            reqBuilder.header("X-Goog-Visitor-Id", it)
        }
        val ck = cookie
        if (!ck.isNullOrBlank()) {
            reqBuilder.header("Cookie", ck)
            buildSapSidHash(ck)?.let { auth ->
                reqBuilder.header("Authorization", auth)
                reqBuilder.header("X-Origin", ORIGIN)
                reqBuilder.header("X-Goog-AuthUser", authUser ?: "0")
                pageId?.takeIf { it.isNotBlank() }?.let {
                    reqBuilder.header("X-Goog-PageId", it)
                }
            }
        }

        http.newCall(reqBuilder.build()).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (text.isBlank()) JSONObject() else JSONObject(text)
        }
    }

    suspend fun browse(
        browseId: String? = null,
        params: String? = null,
        continuation: String? = null
    ): JSONObject {
        val qp = if (continuation != null) {
            mapOf("ctoken" to continuation, "continuation" to continuation, "type" to "next")
        } else emptyMap()
        return postJson("browse", qp) {
            if (browseId != null && continuation == null) put("browseId", browseId)
            if (params != null && continuation == null) put("params", params)
        }
    }

    suspend fun accountMenu(): JSONObject = postJson("account/account_menu") {
        put("deviceTheme", "DEVICE_THEME_SUPPORTED")
        put("userInterfaceTheme", "USER_INTERFACE_THEME_DARK")
    }

    suspend fun accountsList(): JSONObject = postJson("account/accounts_list") {
        put("AccountReadMask", JSONObject().apply {
            put("returnOwner", true)
            put("returnBrandAccounts", true)
            put("returnPersonaAccounts", true)
        })
    }

    suspend fun accountSwitcher(): JSONObject = withContext(Dispatchers.IO) {
        val ck = cookie ?: return@withContext JSONObject()
        val reqBuilder = Request.Builder()
            .url("https://www.youtube.com/getAccountSwitcherEndpoint")
            .get()
            .header("User-Agent", USER_AGENT)
            .header("Referer", "https://www.youtube.com/")
            .header("Origin", "https://www.youtube.com")
            .header("Cookie", ck)
        buildSapSidHash(ck, "https://www.youtube.com")?.let { auth ->
            reqBuilder.header("Authorization", auth)
            reqBuilder.header("X-Origin", "https://www.youtube.com")
            reqBuilder.header("X-Goog-AuthUser", authUser ?: "0")
        }
        http.newCall(reqBuilder.build()).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            val cleaned = raw.removePrefix(")]}'").trimStart()
            if (cleaned.startsWith("{")) JSONObject(cleaned) else JSONObject()
        }
    }

    suspend fun likeChange(endpoint: String, videoId: String): JSONObject =
        postJson("like/$endpoint") {
            put("target", JSONObject().apply {
                put("videoId", videoId)
            })
        }

    suspend fun editPlaylist(playlistId: String, actions: JSONArray): JSONObject =
        postJson("browse/edit_playlist") {
            put("playlistId", playlistId.removePrefix("VL"))
            put("actions", actions)
        }

    suspend fun createPlaylist(
        title: String,
        description: String = "",
        privacyStatus: String = "PRIVATE",
        videoIds: List<String> = emptyList()
    ): JSONObject = postJson("playlist/create") {
        put("title", title)
        put("description", description)
        put("privacyStatus", privacyStatus)
        if (videoIds.isNotEmpty()) {
            put("videoIds", JSONArray(videoIds))
        }
    }

    suspend fun deletePlaylist(playlistId: String): JSONObject =
        postJson("playlist/delete") {
            put("playlistId", playlistId.removePrefix("VL"))
        }
}
