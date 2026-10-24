package com.example.data.innertube

import com.example.auth.DataSyncId
import com.example.data.model.Account
import com.example.data.model.AccountChannel
import com.example.data.model.BitChordSong
import com.example.data.model.HomeFeed
import com.example.data.model.HomeShelf
import com.example.data.model.LibraryPage
import com.example.data.model.MoodGenre
import com.example.data.model.MoodGenreSection
import com.example.data.model.PlaylistDetail
import com.example.data.model.ShelfItem
import org.json.JSONArray
import org.json.JSONObject

object InnertubeParser {

    // ---- JSON navigation helpers ----

    private fun JSONObject?.obj(key: String): JSONObject? = this?.optJSONObject(key)
    private fun JSONObject?.arr(key: String): JSONArray? = this?.optJSONArray(key)
    private fun JSONObject?.str(key: String): String? =
        this?.optString(key)?.takeIf { it.isNotEmpty() && it != "null" }

    private fun JSONObject?.runsText(key: String = "text"): String? {
        val container = if (key.isEmpty()) this else this?.optJSONObject(key)
        container?.str("simpleText")?.let { return it }
        val runs = container?.optJSONArray("runs") ?: return null
        val sb = StringBuilder()
        for (i in 0 until runs.length()) {
            val t = runs.optJSONObject(i)?.optString("text").orEmpty()
            sb.append(t)
        }
        return sb.toString().takeIf { it.isNotEmpty() }
    }

    private fun bestThumbnailFromList(thumbnails: JSONArray?): String? {
        if (thumbnails == null || thumbnails.length() == 0) return null
        var bestUrl: String? = null
        var bestWidth = -1
        for (i in 0 until thumbnails.length()) {
            val item = thumbnails.optJSONObject(i) ?: continue
            val url = item.str("url") ?: continue
            val w = item.optInt("width", 0)
            if (w >= bestWidth) {
                bestWidth = w
                bestUrl = url
            }
        }
        return bestUrl?.let { upscaleArtwork(it) }
    }

    private fun bestThumbnail(container: JSONObject?): String? {
        if (container == null) return null
        bestThumbnailFromList(container.arr("thumbnails"))?.let { return it }
        bestThumbnailFromList(
            container.obj("musicThumbnailRenderer")?.obj("thumbnail")?.arr("thumbnails")
        )?.let { return it }
        bestThumbnailFromList(
            container.obj("croppedSquareThumbnailRenderer")?.obj("thumbnail")?.arr("thumbnails")
        )?.let { return it }
        bestThumbnailFromList(
            container.obj("thumbnail")?.obj("musicThumbnailRenderer")?.obj("thumbnail")?.arr("thumbnails")
        )?.let { return it }
        return null
    }

    fun upscaleArtwork(url: String): String {
        val clean = if (url.startsWith("//")) "https:$url" else url
        return if (clean.contains("googleusercontent.com") || clean.contains("ggpht.com")) {
            clean.replace(Regex("=w\\d+-h\\d+[^&?]*"), "=w544-h544-l90-rj")
                .replace(Regex("=s\\d+[^&?]*"), "=s544")
        } else {
            clean
        }
    }

    private fun findAllObjectsByKey(root: Any?, targetKey: String, out: MutableList<JSONObject> = mutableListOf()): List<JSONObject> {
        when (root) {
            is JSONObject -> {
                val keys = root.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val v = root.opt(k)
                    if (k == targetKey && v is JSONObject) {
                        out.add(v)
                    }
                    findAllObjectsByKey(v, targetKey, out)
                }
            }
            is JSONArray -> {
                for (i in 0 until root.length()) {
                    findAllObjectsByKey(root.opt(i), targetKey, out)
                }
            }
        }
        return out
    }

    // ---- Explore: Moods & Genres ----

    fun moodsAndGenres(root: JSONObject): List<MoodGenreSection> {
        val tabs = root.obj("contents")
            ?.obj("singleColumnBrowseResultsRenderer")
            ?.arr("tabs")
        val sectionContents = tabs?.optJSONObject(0)
            ?.obj("tabRenderer")
            ?.obj("content")
            ?.obj("sectionListRenderer")
            ?.arr("contents") ?: return emptyList()

        val result = mutableListOf<MoodGenreSection>()
        for (i in 0 until sectionContents.length()) {
            val grid = sectionContents.optJSONObject(i)?.obj("gridRenderer") ?: continue
            val header = grid.obj("header")?.obj("gridHeaderRenderer")?.runsText("title")
                ?: "Moods & Genres"
            val itemsArr = grid.arr("items") ?: continue
            val items = mutableListOf<MoodGenre>()
            for (j in 0 until itemsArr.length()) {
                val btn = itemsArr.optJSONObject(j)?.obj("musicNavigationButtonRenderer") ?: continue
                val title = btn.runsText("buttonText") ?: continue
                val browse = btn.obj("clickCommand")?.obj("browseEndpoint") ?: continue
                val browseId = browse.str("browseId") ?: continue
                val params = browse.str("params")
                val color = btn.obj("solid")?.optLong("leftStripeColor")?.takeIf { it != 0L }
                items.add(
                    MoodGenre(
                        title = title,
                        browseId = browseId,
                        params = params,
                        stripeColor = color
                    )
                )
            }
            if (items.isNotEmpty()) {
                result.add(MoodGenreSection(title = header, items = items))
            }
        }
        return result
    }

    fun exploreShelves(root: JSONObject): List<HomeShelf> = homeFeed(root).shelves

    fun moodGenreArtwork(root: JSONObject): String? {
        for (twoRow in findAllObjectsByKey(root, "musicTwoRowItemRenderer")) {
            val art = bestThumbnail(twoRow.obj("thumbnailRenderer"))
            if (!art.isNullOrBlank()) return art
        }
        for (item in findAllObjectsByKey(root, "musicResponsiveListItemRenderer")) {
            val art = bestThumbnail(item.obj("thumbnail"))
            if (!art.isNullOrBlank()) return art
        }
        return null
    }

    // ---- Home / Shelf Feed ----

    fun homeFeed(root: JSONObject): HomeFeed {
        val sectionList = root.obj("contents")
            ?.obj("singleColumnBrowseResultsRenderer")
            ?.arr("tabs")
            ?.optJSONObject(0)
            ?.obj("tabRenderer")
            ?.obj("content")
            ?.obj("sectionListRenderer")
            ?: root.obj("continuationContents")?.obj("sectionListContinuation")

        val contents = sectionList?.arr("contents") ?: JSONArray()
        val shelves = mutableListOf<HomeShelf>()
        for (i in 0 until contents.length()) {
            val section = contents.optJSONObject(i) ?: continue
            parseCarouselShelf(section.obj("musicCarouselShelfRenderer"))?.let { shelves.add(it) }
            parseGridShelf(section.obj("gridRenderer"))?.let { shelves.add(it) }
        }
        val continuation = sectionList?.arr("continuations")
            ?.optJSONObject(0)
            ?.obj("nextContinuationData")
            ?.str("continuation")
        return HomeFeed(shelves = shelves, continuation = continuation)
    }

    private fun parseCarouselShelf(carousel: JSONObject?): HomeShelf? {
        if (carousel == null) return null
        val header = carousel.obj("header")?.obj("musicCarouselShelfBasicHeaderRenderer")
        val title = header?.runsText("title") ?: return null
        val strapline = header.runsText("strapline")
        val moreEndpoint = header.obj("moreContentButton")
            ?.obj("buttonRenderer")
            ?.obj("navigationEndpoint")
            ?.obj("browseEndpoint")
        val browseId = moreEndpoint?.str("browseId")
        val browseParams = moreEndpoint?.str("params")
        val contents = carousel.arr("contents") ?: return null

        val items = mutableListOf<ShelfItem>()
        var trackCount = 0
        for (i in 0 until contents.length()) {
            val itemObj = contents.optJSONObject(i) ?: continue
            itemObj.obj("musicTwoRowItemRenderer")?.let { twoRow ->
                parseTwoRowCard(twoRow)?.let { items.add(it) }
            }
            itemObj.obj("musicResponsiveListItemRenderer")?.let { row ->
                parseResponsiveTrack(row)?.let {
                    items.add(ShelfItem.Track(it))
                    trackCount++
                }
            }
        }
        if (items.isEmpty()) return null
        return HomeShelf(
            title = title,
            strapline = strapline,
            items = items,
            browseId = browseId,
            browseParams = browseParams,
            isQuickPicks = trackCount > 0 && trackCount == items.size
        )
    }

    private fun parseGridShelf(grid: JSONObject?): HomeShelf? {
        if (grid == null) return null
        val title = grid.obj("header")?.obj("gridHeaderRenderer")?.runsText("title") ?: "Playlists"
        val itemsArr = grid.arr("items") ?: return null
        val cards = mutableListOf<ShelfItem>()
        for (i in 0 until itemsArr.length()) {
            val twoRow = itemsArr.optJSONObject(i)?.obj("musicTwoRowItemRenderer") ?: continue
            parseTwoRowCard(twoRow)?.let { cards.add(it) }
        }
        if (cards.isEmpty()) return null
        return HomeShelf(title = title, items = cards)
    }

    private fun parseTwoRowCard(twoRow: JSONObject): ShelfItem.Card? {
        val title = twoRow.runsText("title") ?: return null
        val subtitle = twoRow.runsText("subtitle").orEmpty()
        val nav = twoRow.obj("navigationEndpoint")
        val browseEndpoint = nav?.obj("browseEndpoint")
        val watchEndpoint = nav?.obj("watchEndpoint") ?: nav?.obj("watchPlaylistEndpoint")
        val browseId = browseEndpoint?.str("browseId")
            ?: watchEndpoint?.str("playlistId")?.let { if (it.startsWith("VL")) it else "VL$it" }
            ?: return null
        val playlistId = watchEndpoint?.str("playlistId")
            ?: browseId.removePrefix("VL").takeIf { !it.startsWith("UC") && !it.startsWith("MPREb") }
        val artwork = bestThumbnail(twoRow.obj("thumbnailRenderer"))
        val aspectRatio = twoRow.str("aspectRatio").orEmpty()
        val isCircular = aspectRatio.contains("CIRCLE", ignoreCase = true) || browseId.startsWith("UC")
        return ShelfItem.Card(
            browseId = browseId,
            title = title,
            subtitle = subtitle,
            artworkUrl = artwork,
            playlistId = playlistId,
            isCircular = isCircular
        )
    }

    private fun parseResponsiveTrack(row: JSONObject): BitChordSong? {
        val videoId = row.obj("playlistItemData")?.str("videoId")
            ?: row.obj("overlay")
                ?.obj("musicItemThumbnailOverlayRenderer")
                ?.obj("content")
                ?.obj("musicPlayButtonRenderer")
                ?.obj("playNavigationEndpoint")
                ?.obj("watchEndpoint")
                ?.str("videoId")
            ?: return null

        val flexCols = row.arr("flexColumns") ?: return null
        val title = flexCols.optJSONObject(0)
            ?.obj("musicResponsiveListItemFlexColumnRenderer")
            ?.runsText("text")
            ?: return null
        val subtitleText = flexCols.optJSONObject(1)
            ?.obj("musicResponsiveListItemFlexColumnRenderer")
            ?.runsText("text")
            .orEmpty()
        val parts = subtitleText.split("•").map { it.trim() }.filter { it.isNotEmpty() }
        val artist = parts.firstOrNull().orEmpty().ifBlank { "Artis" }
        val album = parts.getOrNull(1)
        val artwork = bestThumbnail(row.obj("thumbnail"))
        val setVideoId = row.obj("playlistItemData")?.str("playlistSetVideoId")
        return BitChordSong(
            id = videoId,
            title = title,
            artist = artist,
            album = album,
            artworkUrl = artwork,
            setVideoId = setVideoId
        )
    }

    // ---- Account & Multi-Profile Channels ----

    fun accountInfo(root: JSONObject): Account? {
        for (header in findAllObjectsByKey(root, "activeAccountHeaderRenderer")) {
            val name = header.runsText("accountName") ?: continue
            val handle = header.runsText("channelHandle") ?: header.runsText("email")
            val avatar = bestThumbnail(header.obj("accountPhoto"))
            return Account(name = name, handle = handle, avatarUrl = avatar)
        }
        return null
    }

    fun accountChannels(root: JSONObject): List<AccountChannel> {
        val items = findAllObjectsByKey(root, "accountItem")
        if (items.isEmpty()) return emptyList()
        val channels = mutableListOf<AccountChannel>()
        for (item in items) {
            val name = item.runsText("accountName") ?: continue
            val handle = item.runsText("channelHandle")
                ?: item.obj("accountByline")?.runsText("")
            val avatar = bestThumbnail(item.obj("accountPhoto"))
            val isSelected = item.optBoolean("isSelected", false)
            val signInEndpoint = item.obj("serviceEndpoint")?.obj("selectActiveIdentityEndpoint")
            val supportedTokens = signInEndpoint?.arr("supportedTokens")
            var pageId: String? = null
            var rawDataSyncId: String? = null
            var sessionIndex: String? = null
            if (supportedTokens != null) {
                for (i in 0 until supportedTokens.length()) {
                    val tok = supportedTokens.optJSONObject(i) ?: continue
                    tok.obj("pageIdToken")?.str("pageId")?.let { pageId = it }
                    tok.obj("datasyncIdToken")?.str("datasyncIdToken")?.let { rawDataSyncId = it }
                    tok.obj("accountStateToken")?.str("sessionIndex")?.let { sessionIndex = it }
                }
            }
            if (pageId == null && rawDataSyncId != null) {
                pageId = DataSyncId.parse(rawDataSyncId).pageId
            }
            channels.add(
                AccountChannel(
                    name = name,
                    handle = handle,
                    avatarUrl = avatar,
                    pageId = pageId,
                    isSelected = isSelected,
                    rawDataSyncId = rawDataSyncId,
                    authUser = sessionIndex
                )
            )
        }
        return channels
    }

    fun accountSwitcherEndpoint(root: JSONObject): List<AccountChannel> =
        accountChannels(root)

    // ---- Library Playlists & Albums Sync ----

    fun libraryPlaylistCards(root: JSONObject): LibraryPage<ShelfItem.Card> {
        val cards = mutableListOf<ShelfItem.Card>()
        for (twoRow in findAllObjectsByKey(root, "musicTwoRowItemRenderer")) {
            val card = parseTwoRowCard(twoRow) ?: continue
            val cleanId = card.browseId.removePrefix("VL")
            if (cleanId == "SE" || cleanId == "FEmusic_liked_playlists" || cleanId == "FEmusic_liked_albums") continue
            cards.add(card)
        }
        val cont = findAllObjectsByKey(root, "nextContinuationData")
            .firstOrNull()
            ?.str("continuation")
        return LibraryPage(items = cards.distinctBy { it.browseId }, continuation = cont)
    }

    // ---- Playlist / Album Detail (with Custom Header Cover preservation) ----

    fun parsePlaylistDetail(root: JSONObject, browseId: String): PlaylistDetail? {
        var title = ""
        var subtitle = ""
        var description = ""
        var headerCover: String? = null

        val headerRenderer = findAllObjectsByKey(root, "musicResponsiveHeaderRenderer").firstOrNull()
            ?: findAllObjectsByKey(root, "musicDetailHeaderRenderer").firstOrNull()
            ?: findAllObjectsByKey(root, "musicEditablePlaylistDetailHeaderRenderer").firstOrNull()
                ?.obj("header")?.obj("musicResponsiveHeaderRenderer")

        if (headerRenderer != null) {
            title = headerRenderer.runsText("title").orEmpty()
            subtitle = headerRenderer.runsText("subtitle")
                ?: headerRenderer.runsText("straplineTextOne").orEmpty()
            description = headerRenderer.obj("description")
                ?.obj("musicDescriptionShelfRenderer")
                ?.runsText("description")
                ?: headerRenderer.runsText("secondSubtitle").orEmpty()
            headerCover = bestThumbnail(headerRenderer.obj("thumbnail"))
                ?: bestThumbnail(headerRenderer.obj("foregroundThumbnail"))
        }

        val tracks = mutableListOf<com.example.data.model.MusicItem>()
        for (row in findAllObjectsByKey(root, "musicResponsiveListItemRenderer")) {
            parseResponsiveTrack(row)?.let { tracks.add(it.toMusicItem()) }
        }

        if (title.isBlank() && tracks.isEmpty()) return null
        val isAlbum = browseId.startsWith("MPREb") || browseId.startsWith("OLAK5uy_")
        val effectiveCover = headerCover?.takeIf { it.isNotBlank() }
            ?: tracks.firstOrNull()?.highResThumbnail.orEmpty()

        return PlaylistDetail(
            browseId = browseId,
            title = title.ifBlank { if (isAlbum) "Album" else "Playlist" },
            authorSubtitle = subtitle.ifBlank { "YouTube Music" },
            metaSubtitle = "${tracks.size} lagu",
            description = description.ifBlank { "Disimpan di Perpustakaan Anda" },
            thumbnailUrl = effectiveCover,
            totalTracks = tracks.size,
            tracks = tracks,
            id = browseId.removePrefix("VL"),
            subtitle = subtitle.ifBlank { "${tracks.size} lagu" },
            isAlbum = isAlbum
        )
    }
}
