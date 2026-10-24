package com.example.auth

import com.example.data.innertube.Innertube

/**
 * Single source of truth for "is the user signed into YouTube Music right now?"
 *
 * A cookie blob without SAPISID / __Secure-3PAPISID / __Secure-1PAPISID cannot
 * produce a SAPISIDHASH Authorization header, so InnerTube treats every request
 * as anonymous even if stale cookies are stored in prefs.
 */
object SignedIn {
    fun hasValidCookie(cookie: String?): Boolean =
        !cookie.isNullOrBlank() && Innertube.extractSapSid(cookie) != null

    val now: Boolean
        get() = hasValidCookie(AuthStore.cookie.value)
}
