package com.example.auth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import kotlin.coroutines.resume

enum class WebSessionMode {
    SIGN_IN,
    SWITCH_CHANNEL,
}

data class CapturedSession(
    val cookie: String,
    val dataSyncId: String? = null,
    val pageId: String? = null,
    val authUser: String? = null,
    val visitorData: String? = null,
    val clientVersion: String? = null,
    val loggedIn: Boolean = true,
    val channelName: String? = null,
    val channelHandle: String? = null,
    val channelAvatar: String? = null,
)

data class YouTubeProfile(
    val profileId: String,
    val name: String,
    val handle: String,
    val avatar: String? = null,
    val isBrandAccount: Boolean = false,
)

data class GoogleAccountSession(
    val accountId: String,
    val email: String,
    val name: String,
    val profiles: List<YouTubeProfile> = emptyList(),
)

fun normalizeDataSyncId(raw: String?): String? = DataSyncId.parse(raw).dataSyncId

data class YtWebIdentity(
    val cookie: String?,
    val visitorData: String?,
    val dataSyncId: String?,
    val pageId: String?,
    val authUser: String?
)

object BrowserSession {
    fun installGoogleCookies(cookieHeader: String) {
        if (cookieHeader.isBlank()) return
        runCatching {
            val cm = CookieManager.getInstance()
            val urls = listOf(
                "https://music.youtube.com",
                "https://www.youtube.com",
                "https://accounts.google.com",
                "https://google.com"
            )
            cookieHeader.split(";").forEach { rawPair ->
                val pair = rawPair.trim()
                if (pair.contains("=")) {
                    for (url in urls) {
                        cm.setCookie(url, "$pair; Path=/; Secure")
                    }
                }
            }
            cm.flush()
        }
    }

    fun clearGoogleCookies() {
        runCatching {
            val cm = CookieManager.getInstance()
            val urls = listOf(
                "https://music.youtube.com",
                "https://www.youtube.com",
                "https://accounts.google.com",
                "https://google.com"
            )
            for (url in urls) {
                val cookies = cm.getCookie(url) ?: continue
                cookies.split(";").forEach { part ->
                    val name = part.substringBefore("=").trim()
                    if (name.isNotEmpty()) {
                        cm.setCookie(url, "$name=; Max-Age=0; Path=/")
                    }
                }
            }
            cm.flush()
        }
    }
}

object WebSession {

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun switchChannelAndCapture(
        context: Context,
        targetAuthUser: String? = null,
        targetPageId: String? = null
    ): YtWebIdentity? = withTimeoutOrNull(14_000L) {
        suspendCancellableCoroutine { cont ->
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                val webView = WebView(context.applicationContext)
                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true
                val url = buildString {
                    append("https://music.youtube.com/?")
                    if (!targetAuthUser.isNullOrBlank()) append("authuser=$targetAuthUser&")
                    if (!targetPageId.isNullOrBlank()) append("pageId=$targetPageId")
                }.trimEnd('?', '&')

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, loadedUrl: String?) {
                        val js = """
                            (function() {
                                try {
                                    var cfg = window.ytcfg && window.ytcfg.data_ ? window.ytcfg.data_ : {};
                                    return JSON.stringify({
                                        visitor: cfg.VISITOR_DATA || null,
                                        datasync: cfg.DATASYNC_ID || null,
                                        pageId: cfg.DELEGATED_SESSION_ID || null,
                                        sessionIndex: (cfg.SESSION_INDEX !== undefined && cfg.SESSION_INDEX !== null)
                                            ? String(cfg.SESSION_INDEX) : null
                                    });
                                } catch (e) { return "{}"; }
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(js) { raw ->
                            val clean = raw?.trim()?.removeSurrounding("\"")?.replace("\\\"", "\"")
                            val obj = runCatching { JSONObject(clean ?: "{}") }.getOrNull()
                            val cm = CookieManager.getInstance()
                            val cookie = listOf(
                                cm.getCookie("https://music.youtube.com"),
                                cm.getCookie("https://www.youtube.com")
                            ).filterNotNull().joinToString("; ")
                            val parsedDs = DataSyncId.parse(obj?.optString("datasync"))
                            val result = YtWebIdentity(
                                cookie = cookie.takeIf { it.isNotBlank() },
                                visitorData = obj?.optString("visitor")?.takeIf { it.isNotBlank() && it != "null" },
                                dataSyncId = parsedDs.dataSyncId,
                                pageId = obj?.optString("pageId")?.takeIf { it.isNotBlank() && it != "null" } ?: parsedDs.pageId ?: targetPageId,
                                authUser = obj?.optString("sessionIndex")?.takeIf { it.isNotBlank() && it != "null" } ?: targetAuthUser
                            )
                            runCatching { webView.destroy() }
                            if (cont.isActive) cont.resume(result)
                        }
                    }
                }
                cont.invokeOnCancellation {
                    mainHandler.post { runCatching { webView.destroy() } }
                }
                webView.loadUrl(url)
            }
        }
    }
}
