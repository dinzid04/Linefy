package com.example.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ActiveDownloadProgress
import com.example.data.model.MusicItem
import com.example.data.model.PlaylistPrivacy
import com.example.data.model.UserLibraryPlaylist
import com.example.ui.components.BitChordTopHeader
import com.example.ui.components.ContinueListeningArtistsSection
import com.example.ui.components.FourRowHorizontalTrackShelf
import com.example.ui.components.ListenNowSkeletonLoading
import com.example.ui.components.LyrafyLogoLoadingIndicator
import com.example.ui.components.MusicCarouselSection
import com.example.ui.components.PintasanCepatGridSection
import com.example.ui.components.SimilarToArtistSection
import com.example.ui.library.AddToPlaylistBottomSheet
import com.example.ui.library.UniversalTrackMenuBottomSheet
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.settings.AppSettings
import com.example.data.model.HomeShelf
import com.example.data.model.ShelfItem
import com.example.data.model.toBitChordSong
import com.example.ui.components.ShelfBlock
import com.example.ui.theme.SurfaceObsidian
import com.example.ui.theme.rememberDominantAccentColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.ui.components.floatingtabbar.FloatingTabBarScrollConnection

@Composable
fun ListenNowScreen(
    uiState: HomeUiState,
    favoriteIds: Set<String> = emptySet(),
    downloadedTrackIds: Set<String> = emptySet(),
    activeDownloads: Map<String, ActiveDownloadProgress> = emptyMap(),
    libraryPlaylists: List<UserLibraryPlaylist> = emptyList(),
    scrollConnection: FloatingTabBarScrollConnection? = null,
    ytMusicAvatarUrl: String? = null,
    activeSongArtworkUrl: String? = null,
    onTrackClick: (MusicItem) -> Unit,
    onCarouselItemClick: (MusicItem) -> Unit,
    onPlaySectionAll: (String, List<MusicItem>) -> Unit,
    onPlayCommunityTrack: (MusicItem, List<MusicItem>, String) -> Unit = { item, _, _ -> onCarouselItemClick(item) },
    onPlayCommunityFirstTrack: (MusicItem) -> Unit = onCarouselItemClick,
    onStartCommunityRadio: (MusicItem) -> Unit = onCarouselItemClick,
    onToggleSaveCommunityPlaylist: (MusicItem) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenAccountIntegrations: () -> Unit = {},
    onOpenTrackMenu: (MusicItem) -> Unit,
    onDismissTrackMenu: () -> Unit,
    onToggleFavoriteTrack: (MusicItem) -> Unit = {},
    onAddTrackToPlaylist: (playlistId: String, track: MusicItem) -> Unit = { _, _ -> },
    onCreatePlaylistAndAddTrack: (title: String, privacy: PlaylistPrivacy, track: MusicItem) -> Unit = { _, _, _ -> },
    onDownloadTrack: (MusicItem) -> Unit = {},
    onRemoveDownload: (String) -> Unit = {},
    onOpenArtistByName: (String) -> Unit = {},
    onRemoveFromRecents: (MusicItem) -> Unit,
    onClearAllRecents: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val useBitChordHomeUi by AppSettings.useBitChordHomeUi.collectAsStateWithLifecycle()
    val useDynamicMusicTheme by AppSettings.useDynamicMusicTheme.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var trackForAddToPlaylist by remember { mutableStateOf<MusicItem?>(null) }

    val scrollProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / 520f).coerceIn(0f, 1f)
            }
        }
    }

    val ambientImageUrl = remember(activeSongArtworkUrl, uiState.pintasanCepatItems, uiState.topShelfItems) {
        activeSongArtworkUrl?.takeIf { it.isNotBlank() }
            ?: uiState.pintasanCepatItems.firstOrNull()?.highResThumbnail
            ?: uiState.topShelfItems.firstOrNull()?.highResThumbnail
    }

    val extractedDominant = rememberDominantAccentColor(
        imageUrl = if (useDynamicMusicTheme) ambientImageUrl else null,
        defaultColor = SurfaceObsidian,
    )
    val animatedDynamicTopColor by animateColorAsState(
        targetValue = if (useDynamicMusicTheme) {
            extractedDominant.copy(alpha = (0.38f * (1f - scrollProgress * 0.65f)).coerceIn(0f, 0.42f))
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 600),
        label = "home_dynamic_top_color",
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("listen_now_scaffold"),
        containerColor = SurfaceObsidian,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            animatedDynamicTopColor,
                            SurfaceObsidian,
                            SurfaceObsidian,
                        ),
                    ),
                )
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (scrollConnection != null) Modifier.nestedScroll(scrollConnection) else Modifier)
                    .testTag("listen_now_lazy_column"),
                contentPadding = PaddingValues(bottom = 185.dp)
            ) {
                item(
                    key = "top_header",
                    contentType = "header"
                ) {
                    BitChordTopHeader(
                        profileAvatarUrl = ytMusicAvatarUrl,
                        onProfileClick = onOpenSettings
                    )
                }

                if (useBitChordHomeUi) {
                    // BitChord Original Home UI
                    if (uiState.topShelfItems.isNotEmpty()) {
                        item(key = "bitchord_quick_picks", contentType = "bitchord_shelf") {
                            val qpShelf = remember(uiState.topShelfTitle, uiState.topShelfItems) {
                                HomeShelf(
                                    title = uiState.topShelfTitle,
                                    items = uiState.topShelfItems.map { ShelfItem.Track(it.toBitChordSong()) },
                                    isQuickPicks = true
                                )
                            }
                            ShelfBlock(
                                shelf = qpShelf,
                                onPlayTrack = { song, queue -> onPlaySectionAll(qpShelf.title, queue.map { it.toMusicItem() }) },
                                onOpenCard = { card -> onCarouselItemClick(card.toMusicItem()) }
                            )
                        }
                    }

                    // "Lanjut mendengarkan" directly below "Pilihan cepat"
                    if (uiState.continueListeningArtists.isNotEmpty()) {
                        item(key = "bitchord_continue_listening_artists", contentType = "continue_artists") {
                            ContinueListeningArtistsSection(
                                artists = uiState.continueListeningArtists,
                                onArtistClick = onOpenArtistByName
                            )
                        }
                    }

                    val coverRemixIdx = uiState.carouselSections.indexOfFirst { sec ->
                        val low = sec.title.lowercase()
                        low.contains("cover") || low.contains("remix")
                    }.let { if (it >= 0) it else 0 }

                    itemsIndexed(
                        items = uiState.carouselSections,
                        key = { idx, section -> "bitchord_section_${idx}_${section.id}" },
                        contentType = { _, _ -> "bitchord_shelf" }
                    ) { idx, section ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val shelf = remember(section) {
                                HomeShelf(
                                    title = section.title,
                                    strapline = section.strapline,
                                    items = section.items.map {
                                        if (it.isCollection) {
                                            ShelfItem.Card(
                                                browseId = it.browseId ?: it.id,
                                                title = it.title,
                                                subtitle = it.cleanArtist,
                                                artworkUrl = it.highResThumbnail,
                                                playlistId = it.playlistId,
                                                isCircular = it.isArtist
                                            )
                                        } else {
                                            ShelfItem.Track(it.toBitChordSong())
                                        }
                                    }
                                )
                            }
                            ShelfBlock(
                                shelf = shelf,
                                onPlayTrack = { song, queue -> onPlaySectionAll(shelf.title, queue.map { it.toMusicItem() }) },
                                onOpenCard = { card -> onCarouselItemClick(card.toMusicItem()) },
                                onSeeAll = { onPlaySectionAll(section.title, section.items) }
                            )

                            // "Serupa dengan <Nama Artist>" directly below "Cover dan remix"
                            if (idx == coverRemixIdx && uiState.similarArtistRecommendations.isNotEmpty()) {
                                uiState.similarArtistRecommendations.forEach { rec ->
                                    SimilarToArtistSection(
                                        recommendation = rec,
                                        onOpenArtist = onOpenArtistByName,
                                        onItemClick = { clicked ->
                                            if (clicked.isArtist) {
                                                onOpenArtistByName(clicked.title)
                                            } else if (clicked.isCollection) {
                                                onCarouselItemClick(clicked)
                                            } else {
                                                onTrackClick(clicked)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Conditional "Pintasan cepat" 3x3 Grid: ONLY appears AFTER user plays music
                    if (uiState.hasListenedToMusic && uiState.pintasanCepatItems.isNotEmpty()) {
                        item(
                            key = "pintasan_cepat_section",
                            contentType = "pintasan_cepat"
                        ) {
                            PintasanCepatGridSection(
                                items = uiState.pintasanCepatItems,
                                onItemClick = { clicked ->
                                    if (clicked.isCollection) {
                                        onCarouselItemClick(clicked)
                                    } else {
                                        onPlayCommunityTrack(
                                            clicked,
                                            uiState.pintasanCepatItems,
                                            "Playing from Pintasan cepat"
                                        )
                                    }
                                },
                                onExpandAllClick = {
                                    onPlaySectionAll("Pintasan cepat", uiState.pintasanCepatItems)
                                }
                            )
                        }
                    }

                    if (uiState.isLoading && uiState.topShelfItems.isEmpty()) {
                        item(
                            key = "skeleton_loading",
                            contentType = "skeleton"
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                LyrafyLogoLoadingIndicator(size = 56.dp)
                                Spacer(modifier = Modifier.height(14.dp))
                                ListenNowSkeletonLoading()
                            }
                        }
                    } else {
                        // Top Horizontal 4-Row Track Shelf ("Pilihan cepat")
                        if (uiState.topShelfItems.isNotEmpty()) {
                            item(
                                key = "pilihan_cepat_shelf",
                                contentType = "four_row_shelf"
                            ) {
                                FourRowHorizontalTrackShelf(
                                    title = uiState.topShelfTitle,
                                    items = uiState.topShelfItems,
                                    onTrackClick = onTrackClick,
                                    onTrackMenuClick = onOpenTrackMenu,
                                    onPlayAllClick = {
                                        onPlaySectionAll(uiState.topShelfTitle, uiState.topShelfItems)
                                    }
                                )
                            }
                        }

                        // "Lanjut mendengarkan" directly below "Pilihan cepat"
                        if (uiState.continueListeningArtists.isNotEmpty()) {
                            item(
                                key = "continue_listening_artists_section",
                                contentType = "continue_artists"
                            ) {
                                ContinueListeningArtistsSection(
                                    artists = uiState.continueListeningArtists,
                                    onArtistClick = onOpenArtistByName
                                )
                            }
                        }

                        val coverRemixIdx = uiState.carouselSections.indexOfFirst { sec ->
                            val low = sec.title.lowercase()
                            low.contains("cover") || low.contains("remix")
                        }.let { if (it >= 0) it else 0 }

                        // Dynamic YTM Sections
                        itemsIndexed(
                            items = uiState.carouselSections,
                            key = { idx, section -> "section_${idx}_${section.id}" },
                            contentType = { _, section -> section.style.name }
                        ) { idx, section ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                MusicCarouselSection(
                                    section = section,
                                    homeThemeMode = uiState.homeThemeMode,
                                    savedPlaylistIds = uiState.savedPlaylistIds,
                                    onItemClick = onCarouselItemClick,
                                    onTrackMenuClick = onOpenTrackMenu,
                                    onPlayAllClick = {
                                        onPlaySectionAll(section.title, section.items)
                                    },
                                    onPlayCommunityTrack = onPlayCommunityTrack,
                                    onPlayCommunityFirstTrack = onPlayCommunityFirstTrack,
                                    onStartCommunityRadio = onStartCommunityRadio,
                                    onToggleSaveCommunityPlaylist = onToggleSaveCommunityPlaylist
                                )

                                // "Serupa dengan <Nama Artist>" directly below "Cover dan remix"
                                if (idx == coverRemixIdx && uiState.similarArtistRecommendations.isNotEmpty()) {
                                    uiState.similarArtistRecommendations.forEach { rec ->
                                        SimilarToArtistSection(
                                            recommendation = rec,
                                            onOpenArtist = onOpenArtistByName,
                                            onItemClick = { clicked ->
                                                if (clicked.isArtist) {
                                                    onOpenArtistByName(clicked.title)
                                                } else if (clicked.isCollection) {
                                                    onCarouselItemClick(clicked)
                                                } else {
                                                    onTrackClick(clicked)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )
                }
            }

            // Universal 3-Dot Track Menu Sheet (Like, Add to Playlist, Download Offline, Remove Recent, Artist)
            uiState.selectedTrackForMenu?.let { selectedTrack ->
                val isFav = favoriteIds.contains(selectedTrack.id) ||
                    (selectedTrack.videoId != null && favoriteIds.contains(selectedTrack.videoId))
                val isDownloaded = downloadedTrackIds.contains(selectedTrack.id)
                val activeDl = activeDownloads[selectedTrack.id]

                UniversalTrackMenuBottomSheet(
                    track = selectedTrack,
                    isFavorite = isFav,
                    isDownloaded = isDownloaded,
                    activeDownloadProgress = activeDl,
                    onDismiss = onDismissTrackMenu,
                    onToggleFavorite = { onToggleFavoriteTrack(selectedTrack) },
                    onAddToPlaylist = { trackForAddToPlaylist = selectedTrack },
                    onDownloadTrack = { onDownloadTrack(selectedTrack) },
                    onRemoveDownload = { onRemoveDownload(selectedTrack.id) },
                    onOpenArtist = { onOpenArtistByName(selectedTrack.cleanArtist) },
                    onRemoveFromCurrentPlaylist = if (uiState.pintasanCepatItems.any { it.id == selectedTrack.id }) {
                        { onRemoveFromRecents(selectedTrack) }
                    } else null
                )
            }

            trackForAddToPlaylist?.let { targetTrack ->
                AddToPlaylistBottomSheet(
                    track = targetTrack,
                    userPlaylists = libraryPlaylists,
                    onDismiss = { trackForAddToPlaylist = null },
                    onSelectPlaylist = { playlist ->
                        onAddTrackToPlaylist(playlist.id, targetTrack)
                        trackForAddToPlaylist = null
                    },
                    onCreateNewPlaylistAndAdd = { title, privacy ->
                        onCreatePlaylistAndAddTrack(title, privacy, targetTrack)
                        trackForAddToPlaylist = null
                    }
                )
            }
        }
    }
}
