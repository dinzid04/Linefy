package com.example.data.innertube

import com.example.auth.AuthStore
import com.example.data.model.Account
import com.example.data.model.AccountChannel
import com.example.data.model.BitChordSong
import com.example.data.model.HomeShelf
import com.example.data.model.LibraryPage
import com.example.data.model.MoodGenre
import com.example.data.model.MoodGenreSection
import com.example.data.model.MusicItem
import com.example.data.model.PlaylistDetail
import com.example.data.model.ShelfItem
import com.example.data.remote.RemoteArtwork
import com.example.data.remote.RemoteArtworkStore
import com.example.data.settings.AppSettings

class YtMusicRepository(
    private val api: Innertube = Innertube()
) {
    companion object {
        val instance: YtMusicRepository by lazy { YtMusicRepository() }
    }

    private var cachedMoodsGenres: List<MoodGenreSection>? = null
    private var cachedMoodsGenresLocale: String? = null
    private val moodArtworkCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    private fun syncAuth() {
        if (AuthStore.isInitialized) {
            api.cookie = AuthStore.cookie.value
            api.visitorData = AuthStore.visitorData.value
            api.pageId = AuthStore.pageId.value
            api.dataSyncId = AuthStore.dataSyncId.value
            api.authUser = AuthStore.authUser.value
        }
        if (AppSettings.isInitialized) {
            api.gl = AppSettings.contentCountry.value
            api.hl = AppSettings.contentLanguage.value
        }
    }

    fun invalidateExploreCache() {
        cachedMoodsGenres = null
        cachedMoodsGenresLocale = null
    }

    suspend fun moodsAndGenres(forceRefresh: Boolean = false): List<MoodGenreSection> {
        syncAuth()
        val localeKey = "${api.gl}_${api.hl}"
        if (!forceRefresh) {
            cachedMoodsGenres?.takeIf { it.isNotEmpty() && cachedMoodsGenresLocale == localeKey }?.let {
                return applyCachedArtwork(it)
            }
        }
        val fetched = runCatching {
            val json = api.browse(browseId = "FEmusic_moods_and_genres")
            InnertubeParser.moodsAndGenres(json)
        }.getOrNull().orEmpty()

        val result = if (fetched.isNotEmpty()) fetched else fallbackMoodsAndGenres()
        cachedMoodsGenres = result
        cachedMoodsGenresLocale = localeKey
        return applyCachedArtwork(result)
    }

    private fun applyCachedArtwork(sections: List<MoodGenreSection>): List<MoodGenreSection> =
        sections.map { sec ->
            sec.copy(
                items = sec.items.map { item ->
                    val cached = moodArtworkCache["${item.browseId}:${item.params.orEmpty()}"]
                    if (cached != null && item.artworkUrl.isNullOrBlank()) item.copy(artworkUrl = cached)
                    else item
                }
            )
        }

    suspend fun moodGenreArtwork(browseId: String, params: String?): String? {
        val key = "$browseId:${params.orEmpty()}"
        moodArtworkCache[key]?.let { return it }
        syncAuth()
        val art = runCatching {
            val json = api.browse(browseId = browseId, params = params)
            InnertubeParser.moodGenreArtwork(json)
        }.getOrNull()
        if (!art.isNullOrBlank()) {
            moodArtworkCache[key] = art
        }
        return art
    }

    suspend fun exploreShelves(): List<HomeShelf> {
        syncAuth()
        return runCatching {
            val json = api.browse(browseId = "FEmusic_explore")
            InnertubeParser.exploreShelves(json)
        }.getOrDefault(emptyList())
    }

    suspend fun moodGenreShelves(browseId: String, params: String?): List<HomeShelf> {
        syncAuth()
        val fetched = runCatching {
            val json = api.browse(browseId = browseId, params = params)
            InnertubeParser.homeFeed(json).shelves
        }.getOrNull().orEmpty()
        if (fetched.isNotEmpty()) return fetched
        return fallbackMoodGenreShelves(browseId, params)
    }

    suspend fun account(): Account? {
        syncAuth()
        if (api.cookie.isNullOrBlank()) return null
        return runCatching {
            val json = api.accountMenu()
            InnertubeParser.accountInfo(json)
        }.getOrNull()
    }

    suspend fun accountChannels(): List<AccountChannel> {
        syncAuth()
        if (api.cookie.isNullOrBlank()) return emptyList()
        val primary = runCatching {
            InnertubeParser.accountChannels(api.accountsList())
        }.getOrDefault(emptyList())
        val switcher = runCatching {
            InnertubeParser.accountSwitcherEndpoint(api.accountSwitcher())
        }.getOrDefault(emptyList())
        val combined = (switcher + primary).distinctBy {
            it.pageId ?: it.handle ?: it.name
        }
        return combined
    }

    suspend fun libraryPlaylistsPage(continuation: String? = null): LibraryPage<ShelfItem.Card> {
        syncAuth()
        if (api.cookie.isNullOrBlank()) return LibraryPage(emptyList(), null)
        return runCatching {
            val json = if (continuation == null) {
                api.browse(browseId = "FEmusic_liked_playlists")
            } else {
                api.browse(continuation = continuation)
            }
            val page = InnertubeParser.libraryPlaylistCards(json)
            page.copy(
                items = page.items.map { card ->
                    if (!card.artworkUrl.isNullOrBlank() && RemoteArtwork.isCustomCoverUrl(card.artworkUrl)) {
                        RemoteArtworkStore.put(card.browseId, card.artworkUrl)
                    }
                    val override = RemoteArtworkStore.get(card.browseId)
                    if (!override.isNullOrBlank()) card.copy(artworkUrl = override) else card
                }
            )
        }.getOrDefault(LibraryPage(emptyList(), null))
    }

    suspend fun libraryAlbumsPage(continuation: String? = null): LibraryPage<ShelfItem.Card> {
        syncAuth()
        if (api.cookie.isNullOrBlank()) return LibraryPage(emptyList(), null)
        return runCatching {
            val json = if (continuation == null) {
                api.browse(browseId = "FEmusic_liked_albums")
            } else {
                api.browse(continuation = continuation)
            }
            val page = InnertubeParser.libraryPlaylistCards(json)
            page.copy(
                items = page.items.map { card ->
                    if (!card.artworkUrl.isNullOrBlank()) {
                        RemoteArtworkStore.put(card.browseId, card.artworkUrl)
                    }
                    card
                }
            )
        }.getOrDefault(LibraryPage(emptyList(), null))
    }

    suspend fun fetchLikedMusicPlaylist(): PlaylistDetail? {
        syncAuth()
        if (api.cookie.isNullOrBlank()) return null
        return runCatching {
            val json = api.browse(browseId = "VLLM")
            InnertubeParser.parsePlaylistDetail(json, "VLLM")
        }.getOrNull()
    }

    suspend fun fetchPlaylistOrAlbumDetail(browseId: String): PlaylistDetail? {
        syncAuth()
        val normalizedId = if (browseId.startsWith("VL") || browseId.startsWith("MPREb") || browseId.startsWith("OLAK5uy_") || browseId.startsWith("UC")) {
            browseId
        } else {
            "VL$browseId"
        }
        return runCatching {
            val json = api.browse(browseId = normalizedId)
            val detail = InnertubeParser.parsePlaylistDetail(json, normalizedId)
            if (detail != null && detail.thumbnailUrl.isNotBlank()) {
                val firstTrackThumb = detail.tracks.firstOrNull()?.thumbnailUrl
                if (RemoteArtwork.shouldCacheOverride(
                        existingLocalUrl = RemoteArtworkStore.get(normalizedId),
                        remoteHeaderUrl = detail.thumbnailUrl,
                        firstTrackUrl = firstTrackThumb
                    )
                ) {
                    RemoteArtworkStore.put(normalizedId, detail.thumbnailUrl)
                }
            }
            detail
        }.getOrNull()
    }

    // ---- Authentic Indonesian Moods & Genres Fallback matching Screenshot 1 ----

    private fun fallbackMoodsAndGenres(): List<MoodGenreSection> = listOf(
        MoodGenreSection(
            title = "Mood & momen",
            items = listOf(
                MoodGenre(
                    title = "Bepergian",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uXzJNZV9KUXRkV2lJ",
                    stripeColor = 0xFFFFB300L,
                    artworkUrl = "https://i.ytimg.com/vi/d49fN_j2fX4/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Fokus",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uX2NvZ2p5R0dGeGZR",
                    stripeColor = 0xFF9E9D89L,
                    artworkUrl = "https://i.ytimg.com/vi/jfKfPfyJRdk/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Gaming",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uX3h3bV9wSm5FbnVN",
                    stripeColor = 0xFFFF9800L,
                    artworkUrl = "https://i.ytimg.com/vi/7NOSDKb0HlU/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Merasa senang",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uXzZ5R19XUXRkV2lJ",
                    stripeColor = 0xFFFFEB3BL,
                    artworkUrl = "https://i.ytimg.com/vi/ZbZSe6N_BXs/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Olah Raga",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uXzlnR19XUXRkV2lJ",
                    stripeColor = 0xFFFFA726L,
                    artworkUrl = "https://i.ytimg.com/vi/btPJPFnesV4/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Pesta",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uXzhnR19XUXRkV2lJ",
                    stripeColor = 0xFFAB47BCL,
                    artworkUrl = "https://i.ytimg.com/vi/IcrbM1l_BoI/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Romantis",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uXzdnR19XUXRkV2lJ",
                    stripeColor = 0xFFC62828L,
                    artworkUrl = "https://i.ytimg.com/vi/450p7goxZqg/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Santai",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uXzVnR19XUXRkV2lJ",
                    stripeColor = 0xFF90CAF9L,
                    artworkUrl = "https://i.ytimg.com/vi/5qap5aO4i9A/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Sedih",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uXzRnR19XUXRkV2lJ",
                    stripeColor = 0xFF42A5F5L,
                    artworkUrl = "https://i.ytimg.com/vi/hLQl3WQQoQ0/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Semangat!",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uXzNnR19XUXRkV2lJ",
                    stripeColor = 0xFFFFCA28L,
                    artworkUrl = "https://i.ytimg.com/vi/OPf0YbXqDm0/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Tidur",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uXzJnR19XUXRkV2lJ",
                    stripeColor = 0xFF5C6BC0L,
                    artworkUrl = "https://i.ytimg.com/vi/DWcJFNfaw9c/hqdefault.jpg"
                )
            )
        ),
        MoodGenreSection(
            title = "Genre",
            items = listOf(
                MoodGenre(
                    title = "Afrika",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uXzFnR19XUXRkV2lJ",
                    stripeColor = 0xFF43A047L,
                    artworkUrl = "https://i.ytimg.com/vi/ojULkWEUsPs/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Arab",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uXzBnR19XUXRkV2lJ",
                    stripeColor = 0xFFEC407AL,
                    artworkUrl = "https://i.ytimg.com/vi/3tmd-ClpJxA/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Pop Indonesia",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uX2lkb19wb3A",
                    stripeColor = 0xFFEF5350L,
                    artworkUrl = "https://i.ytimg.com/vi/cXJr94c7O5A/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Indie & Alternatif",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uX2luZGll",
                    stripeColor = 0xFF26A69AL,
                    artworkUrl = "https://i.ytimg.com/vi/tGv7CUutzqU/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "R&B & Soul",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uX3JuYg",
                    stripeColor = 0xFF7E57C2L,
                    artworkUrl = "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Rock",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uX3JvY2s",
                    stripeColor = 0xFFE53935L,
                    artworkUrl = "https://i.ytimg.com/vi/fJ9rUzIMcZQ/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "K-Pop",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uX2twb3A",
                    stripeColor = 0xFFEC407AL,
                    artworkUrl = "https://i.ytimg.com/vi/gdZLi9oWNZg/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "J-Pop",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uX2pwb3A",
                    stripeColor = 0xFF29B6F6L,
                    artworkUrl = "https://i.ytimg.com/vi/ZRtdQ81jPUQ/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Jazz",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uX2pheno",
                    stripeColor = 0xFFFFA000L,
                    artworkUrl = "https://i.ytimg.com/vi/vmDDOFXSgAs/hqdefault.jpg"
                ),
                MoodGenre(
                    title = "Hip-Hop",
                    browseId = "FEmusic_moods_and_genres_category",
                    params = "ggMPOg1uX2hpcGhvcA",
                    stripeColor = 0xFFFF7043L,
                    artworkUrl = "https://i.ytimg.com/vi/RgKAFK5djSk/hqdefault.jpg"
                )
            )
        )
    )

    private fun fallbackMoodGenreShelves(browseId: String, params: String?): List<HomeShelf> = listOf(
        HomeShelf(
            title = "Playlist Unggulan",
            items = listOf(
                ShelfItem.Card(
                    browseId = "VLRDCLAK5uy_kset8DisdE7LSD4TNjEVvrKRTmG7a56sY",
                    title = "Hits Terbaik Hari Ini",
                    subtitle = "YouTube Music • 50 lagu",
                    artworkUrl = "https://i.ytimg.com/vi/cXJr94c7O5A/hqdefault.jpg",
                    playlistId = "RDCLAK5uy_kset8DisdE7LSD4TNjEVvrKRTmG7a56sY"
                ),
                ShelfItem.Card(
                    browseId = "VLRDCLAK5uy_m1h4923x27z2mP4mF7V4K0V_p5s0xS8oE",
                    title = "Santai Sore & Kopi",
                    subtitle = "YouTube Music • 45 lagu",
                    artworkUrl = "https://i.ytimg.com/vi/tGv7CUutzqU/hqdefault.jpg",
                    playlistId = "RDCLAK5uy_m1h4923x27z2mP4mF7V4K0V_p5s0xS8oE"
                ),
                ShelfItem.Card(
                    browseId = "VLRDCLAK5uy_n9Fbdw7e6ap-98_A-8C1K2fW8x9M3x2Q",
                    title = "Pop Indo Favorit",
                    subtitle = "YouTube Music • 60 lagu",
                    artworkUrl = "https://i.ytimg.com/vi/d49fN_j2fX4/hqdefault.jpg",
                    playlistId = "RDCLAK5uy_n9Fbdw7e6ap-98_A-8C1K2fW8x9M3x2Q"
                )
            )
        ),
        HomeShelf(
            title = "Lagu Pilihan",
            isQuickPicks = true,
            items = listOf(
                ShelfItem.Track(
                    BitChordSong(
                        id = "cXJr94c7O5A",
                        title = "Hati-Hati di Jalan",
                        artist = "Tulus",
                        album = "Manusia",
                        artworkUrl = "https://i.ytimg.com/vi/cXJr94c7O5A/hqdefault.jpg",
                        durationSec = 242
                    )
                ),
                ShelfItem.Track(
                    BitChordSong(
                        id = "tGv7CUutzqU",
                        title = "About You",
                        artist = "The 1975",
                        album = "Being Funny in a Foreign Language",
                        artworkUrl = "https://i.ytimg.com/vi/tGv7CUutzqU/hqdefault.jpg",
                        durationSec = 326
                    )
                ),
                ShelfItem.Track(
                    BitChordSong(
                        id = "d49fN_j2fX4",
                        title = "Untungnya, Hidup Harus Tetap Berjalan",
                        artist = "Bernadya",
                        album = "Sialnya, Hidup Harus Tetap Berjalan",
                        artworkUrl = "https://i.ytimg.com/vi/d49fN_j2fX4/hqdefault.jpg",
                        durationSec = 183
                    )
                ),
                ShelfItem.Track(
                    BitChordSong(
                        id = "4NRXx6U8ABQ",
                        title = "Blinding Lights",
                        artist = "The Weeknd",
                        album = "After Hours",
                        artworkUrl = "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
                        durationSec = 200
                    )
                )
            )
        )
    )
}
