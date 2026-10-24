package com.example.data.remote

/**
 * Pure policy for deciding whether a remotely fetched playlist/album thumbnail
 * is worth caching as a local override for a collection's stored `artworkUrl`.
 *
 * Custom playlist covers uploaded on YouTube Music are served from
 * `yt3.ggpht.com` or `lh3.googleusercontent.com`. Auto-generated 2x2 grids and
 * first-track fallbacks are served from `i.ytimg.com/vi/<videoId>/...`.
 *
 * When a local playlist row only has a first-track `i.ytimg.com` fallback (or
 * nothing at all), replacing it with a `ggpht`/`googleusercontent` cover
 * upgrades the user's Library card to their custom artwork.
 */
object RemoteArtwork {

    fun isCustomCoverUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.contains("googleusercontent.com") || url.contains("ggpht.com")
    }

    fun isAutoVideoThumb(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.contains("i.ytimg.com/vi/") || url.contains("i.ytimg.com/vi_webp/")
    }

    fun shouldCacheOverride(
        existingLocalUrl: String?,
        remoteHeaderUrl: String?,
        firstTrackUrl: String?
    ): Boolean {
        if (remoteHeaderUrl.isNullOrBlank()) return false
        if (remoteHeaderUrl == existingLocalUrl) return false
        if (!firstTrackUrl.isNullOrBlank() && remoteHeaderUrl == firstTrackUrl) {
            return existingLocalUrl.isNullOrBlank()
        }
        if (isCustomCoverUrl(remoteHeaderUrl)) return true
        return existingLocalUrl.isNullOrBlank() ||
            (isAutoVideoThumb(existingLocalUrl) && !isAutoVideoThumb(remoteHeaderUrl))
    }

    fun pickBestCover(
        localOverride: String?,
        remoteHeaderUrl: String?,
        existingUrl: String?,
        firstTrackUrl: String?
    ): String {
        if (!localOverride.isNullOrBlank() && isCustomCoverUrl(localOverride)) return localOverride
        if (!remoteHeaderUrl.isNullOrBlank() && isCustomCoverUrl(remoteHeaderUrl)) return remoteHeaderUrl
        if (!existingUrl.isNullOrBlank() && isCustomCoverUrl(existingUrl)) return existingUrl
        return localOverride?.takeIf { it.isNotBlank() }
            ?: remoteHeaderUrl?.takeIf { it.isNotBlank() }
            ?: existingUrl?.takeIf { it.isNotBlank() }
            ?: firstTrackUrl.orEmpty()
    }
}
