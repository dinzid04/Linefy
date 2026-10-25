package com.example.auth

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

private const val MUSIC_ORIGIN = "https://music.youtube.com"

private const val LOGIN_URL =
    "https://accounts.google.com/ServiceLogin" +
        "?ltmpl=music&service=youtube&prompt=select_account" +
        "&continue=https%3A%2F%2Fmusic.youtube.com%2F"

private const val TAG = "BitChord"

/**
 * In-app Google sign-in for YouTube Music, and the way to change which channel
 * it listens as.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YtMusicLoginScreen(
    mode: WebSessionMode,
    initialCookie: String? = null,
    onCaptured: (CapturedSession) -> Unit,
    modifier: Modifier = Modifier,
    captureRequest: Int = 0,
    onCaptureUnavailable: () -> Unit = {},
    onPageReady: (Boolean) -> Unit = {},
    configureWebView: (WebView) -> Unit = {},
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    val currentOnCaptured by rememberUpdatedState(onCaptured)
    val currentOnUnavailable by rememberUpdatedState(onCaptureUnavailable)
    val currentOnPageReady by rememberUpdatedState(onPageReady)

    LaunchedEffect(captureRequest) {
        if (captureRequest == 0) return@LaunchedEffect
        val view = webView
        if (view == null) currentOnUnavailable()
        else captureFrom(view, currentOnCaptured, currentOnUnavailable)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            if (mode == WebSessionMode.SIGN_IN) {
                BrowserSession.clearGoogleCookies()
            } else initialCookie?.let(BrowserSession::installGoogleCookies)
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setBackgroundColor(0xFF121212.toInt())
                if (mode == WebSessionMode.SIGN_IN) {
                    clearCache(true)
                    clearHistory()
                    clearFormData()
                }
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString = browserUserAgent(settings.userAgentString)
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                configureWebView(this)

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val ready = url?.startsWith(MUSIC_ORIGIN) == true
                        currentOnPageReady(ready)
                    }
                }

                webView = this
                loadUrl(if (mode == WebSessionMode.SIGN_IN) LOGIN_URL else "$MUSIC_ORIGIN/")
            }
        },
    )
}

private fun captureFrom(
    view: WebView,
    onCaptured: (CapturedSession) -> Unit,
    onUnavailable: () -> Unit,
) {
    val cookies = CookieManager.getInstance().getCookie(MUSIC_ORIGIN)
    if (cookies == null || !AuthStore.hasApiSid(cookies)) {
        onUnavailable()
        return
    }
    runCatching { CookieManager.getInstance().flush() }

    view.evaluateJavascript(YTCFG_PROBE) { raw ->
        val config = parseConfig(raw)
        val loggedIn = config?.optString("loggedIn") == "true"
        if (config == null || !loggedIn) {
            Log.w(TAG, "confirmation requested before the page exposed a signed-in identity")
            // Even if ytcfg hasn't exposed loggedIn yet, if SAPISID cookie is present on music.youtube.com, allow fallback capture
            if (AuthStore.hasApiSid(cookies)) {
                val pageId = config?.optString("pageId")?.takeIf { it.isNotBlank() && it != "null" }
                val rawSync = config?.optString("dataSyncId")?.takeIf { it.isNotBlank() && it != "null" }
                onCaptured(
                    CapturedSession(
                        cookie = cookies,
                        dataSyncId = pageId ?: normalizeDataSyncId(rawSync),
                        pageId = pageId,
                        authUser = config?.optString("authUser")?.takeIf { it.isNotBlank() && it != "null" },
                        visitorData = config?.optString("visitorData")?.takeIf { it.isNotBlank() && it != "null" },
                        clientVersion = config?.optString("clientVersion")?.takeIf { it.isNotBlank() && it != "null" },
                        loggedIn = true,
                    ),
                )
            } else {
                onUnavailable()
            }
            return@evaluateJavascript
        }
        val pageId = config.optString("pageId").takeIf { it.isNotBlank() && it != "null" }
        val rawSync = config.optString("dataSyncId").takeIf { it.isNotBlank() && it != "null" }
        onCaptured(
            CapturedSession(
                cookie = cookies,
                dataSyncId = pageId ?: normalizeDataSyncId(rawSync),
                pageId = pageId,
                authUser = config.optString("authUser").takeIf { it.isNotBlank() && it != "null" },
                visitorData = config.optString("visitorData").takeIf { it.isNotBlank() && it != "null" },
                clientVersion = config.optString("clientVersion").takeIf { it.isNotBlank() && it != "null" },
                loggedIn = true,
            ),
        )
    }
}

private const val YTCFG_PROBE = """
(function () {
  try {
    if (!window.ytcfg || !window.ytcfg.get) return null;
    var get = function (key) {
      var value = window.ytcfg.get(key);
      return (value === undefined || value === null || value === '') ? null : String(value);
    };
    return {
      loggedIn: String(!!window.ytcfg.get('LOGGED_IN')),
      pageId: get('DELEGATED_SESSION_ID'),
      dataSyncId: get('DATASYNC_ID'),
      authUser: get('SESSION_INDEX'),
      visitorData: get('VISITOR_DATA'),
      clientVersion: get('INNERTUBE_CLIENT_VERSION')
    };
  } catch (e) {
    return null;
  }
})()
"""

private fun parseConfig(raw: String?): JSONObject? {
    val text = raw?.trim()?.takeIf { it.isNotEmpty() && it != "null" } ?: return null
    return runCatching { JSONObject(text) }.getOrNull()
}
