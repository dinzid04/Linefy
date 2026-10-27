package com.example.auth

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private const val LOGIN_URL =
    "https://accounts.spotify.com/login?continue=https%3A%2F%2Fopen.spotify.com%2F"

private const val DESKTOP_CHROME_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

private const val BRIDGE_NAME = "LyrafySpotifyBridge"

private val SPOTIFY_COOKIE_URLS = listOf(
    "https://open.spotify.com",
    "https://accounts.spotify.com",
    "https://www.spotify.com",
    "https://spotify.com",
)

private val LOGIN_HOST_SUFFIXES = listOf(
    "spotify.com", "scdn.co", "spotifycdn.com", "google.com", "gstatic.com", "facebook.com", "apple.com",
)

private val CAPTURE_BEARER_HOOK_JS = """
(function() {
  if (window.__lyrafySpHooked) return;
  window.__lyrafySpHooked = true;
  function reportAuth(val) {
    try {
      if (val && typeof val === 'string' && val.toLowerCase().indexOf('bearer ') === 0 && val.length > 30) {
        window.$BRIDGE_NAME.onBearer(val.substring(7).trim());
      }
    } catch (e) {}
  }
  try {
    var origFetch = window.fetch;
    if (origFetch) {
      window.fetch = function(input, init) {
        try {
          var h = (init && init.headers) || (input && input.headers);
          if (h) {
            if (typeof Headers !== 'undefined' && h instanceof Headers) {
              reportAuth(h.get('authorization') || h.get('Authorization'));
            } else if (typeof h === 'object') {
              Object.keys(h).forEach(function(k) {
                if (k.toLowerCase() === 'authorization') reportAuth(h[k]);
              });
            }
          }
        } catch (e) {}
        return origFetch.apply(this, arguments);
      };
    }
  } catch (e) {}
  try {
    var origSet = XMLHttpRequest.prototype.setRequestHeader;
    XMLHttpRequest.prototype.setRequestHeader = function(name, value) {
      if (String(name).toLowerCase() === 'authorization') reportAuth(String(value));
      return origSet.apply(this, arguments);
    };
  } catch (e) {}
  try {
    var sessionEl = document.getElementById('session');
    if (sessionEl && sessionEl.textContent) {
      var data = JSON.parse(sessionEl.textContent);
      if (data && data.accessToken) {
        window.$BRIDGE_NAME.onBearer(String(data.accessToken));
      }
    }
  } catch (e) {}
})();
""".trimIndent()

private fun hostMatches(uri: Uri?, suffixes: List<String>): Boolean {
    if (uri?.scheme != "https") return false
    val host = uri.host?.lowercase() ?: return false
    return suffixes.any { host == it || host.endsWith(".$it") }
}

private fun isLoginHost(uri: Uri?) = hostMatches(uri, LOGIN_HOST_SUFFIXES)

fun clearSpotifyWebSession() {
    val cookies = runCatching { CookieManager.getInstance() }.getOrNull() ?: return
    for (url in SPOTIFY_COOKIE_URLS) {
        val names = cookies.getCookie(url)?.split(";")
            ?.map { it.substringBefore("=").trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()
        for (name in names) {
            val expired = "$name=; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/"
            cookies.setCookie(url, expired)
            cookies.setCookie(url, "$expired; Domain=.spotify.com")
        }
    }
    runCatching { cookies.flush() }
}

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun SpotifyLoginScreen(
    onConnected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.userAgentString = browserUserAgent(settings.userAgentString)
                setBackgroundColor(0xFF121212.toInt())
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                var sent = false
                var capturedBearer: String? = null

                fun tryCompleteLogin(view: WebView) {
                    if (sent) return
                    val raw = SPOTIFY_COOKIE_URLS
                        .mapNotNull { CookieManager.getInstance().getCookie(it) }
                        .joinToString("; ")
                    val spDc = raw.split(";")
                        .map { it.trim() }
                        .firstOrNull { it.startsWith("sp_dc=") }
                        ?.substringAfter("=")
                        ?.takeIf { it.isNotBlank() }
                    val bearer = capturedBearer?.takeIf { it.length > 30 }

                    if (spDc != null) {
                        sent = true
                        val combined = if (bearer != null) "$spDc|Bearer $bearer" else spDc
                        view.post {
                            view.visibility = View.GONE
                            onConnected(combined)
                        }
                    } else if (bearer != null) {
                        sent = true
                        view.post {
                            view.visibility = View.GONE
                            onConnected("Bearer $bearer")
                        }
                    }
                }

                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onBearer(token: String?) {
                            if (!token.isNullOrBlank() && token.length > 30) {
                                capturedBearer = token.trim()
                                tryCompleteLogin(this@apply)
                            }
                        }
                    },
                    BRIDGE_NAME
                )

                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest) {
                        val protectedMedia = arrayOf(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)
                        if (request.resources.any { it == PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID }) {
                            request.grant(protectedMedia)
                        } else {
                            request.deny()
                        }
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        val uri = request.url ?: return true
                        // Block any deep-link / intent / market URLs that ask to install the external Spotify app
                        val scheme = uri.scheme?.lowercase().orEmpty()
                        if (scheme == "spotify" || scheme == "intent" || scheme == "market") {
                            return true
                        }
                        val host = uri.host?.lowercase().orEmpty()
                        if (host.contains("play.google.com") || host.contains("apps.apple.com") || host.contains("spotify.link")) {
                            return true
                        }
                        if (!isLoginHost(uri)) return true

                        // Switch to desktop User-Agent before loading open.spotify.com so Spotify never shows
                        // "To use Spotify, please install the Spotify app on your device"
                        if (host == "open.spotify.com" && view.settings.userAgentString != DESKTOP_CHROME_UA) {
                            view.settings.userAgentString = DESKTOP_CHROME_UA
                            view.settings.loadWithOverviewMode = true
                            tryCompleteLogin(view)
                        }
                        return false
                    }

                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest,
                    ): WebResourceResponse? {
                        val auth = request.requestHeaders?.entries
                            ?.firstOrNull { it.key.equals("authorization", ignoreCase = true) }
                            ?.value
                        if (!auth.isNullOrBlank() && auth.startsWith("Bearer ", ignoreCase = true)) {
                            val token = auth.substring(7).trim()
                            if (token.length > 30) {
                                capturedBearer = token
                                tryCompleteLogin(view)
                            }
                        }
                        return null
                    }

                    override fun onPageFinished(view: WebView, url: String?) {
                        canGoBack = view.canGoBack()
                        if (url?.startsWith("https://open.spotify.com") == true) {
                            if (view.settings.userAgentString != DESKTOP_CHROME_UA) {
                                view.settings.userAgentString = DESKTOP_CHROME_UA
                                view.settings.loadWithOverviewMode = true
                            }
                            view.evaluateJavascript(CAPTURE_BEARER_HOOK_JS, null)
                        }
                        tryCompleteLogin(view)
                    }
                }
                webView = this
                loadUrl(LOGIN_URL)
            }
        },
        onRelease = { it.destroy() },
    )

    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }
}
