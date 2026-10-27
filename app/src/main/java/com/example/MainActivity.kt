package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.lyrics.LyricLine as BitChordLyricLine
import com.example.data.lyrics.LyricsProviderState
import com.example.data.lyrics.LyricsSettingsState
import com.example.data.lyrics.LyricsSource
import com.example.data.model.ActiveDownloadProgress
import com.example.data.model.LyricsData
import com.example.data.model.LyricsProvider
import com.example.data.model.MusicItem
import com.example.data.model.PlaylistPrivacy
import com.example.data.model.UserLibraryPlaylist
import com.example.player.PlayerUiState
import com.example.ui.components.BitChordTab
import com.example.ui.components.FloatingMiniPlayer
import com.example.ui.components.FloatingPillBottomBar
import com.example.ui.components.FullMusicPlayerScreen
import com.example.ui.components.backdrop.backdrops.layerBackdrop
import com.example.ui.components.backdrop.backdrops.rememberLayerBackdrop
import com.example.ui.home.ListenNowScreen
import com.example.ui.home.MusicHomeViewModel
import com.example.ui.home.PlayerSubView
import com.example.ui.library.LibraryScreen
import com.example.ui.playlist.PlaylistDetailScreen
import com.example.ui.screens.AccountIntegrationsScreen
import com.example.ui.screens.ArtistProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.search.SearchScreen
import com.example.ui.theme.BitChordTheme
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SurfaceObsidian
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            com.example.auth.AuthStore.init(applicationContext)
            com.example.data.remote.RemoteArtworkStore.init(applicationContext)
            com.example.data.settings.AppSettings.init(applicationContext)
        }
        enableEdgeToEdge()
        setContent {
            val highPerf by com.example.data.settings.AppSettings.highPerformanceMode.collectAsStateWithLifecycle()
            val targetHz by com.example.data.settings.AppSettings.performanceRefreshRate.collectAsStateWithLifecycle()
            LaunchedEffect(highPerf, targetHz) {
                runCatching {
                    val lp = window.attributes
                    lp.preferredRefreshRate = if (highPerf) targetHz.toFloat() else 0f
                    window.attributes = lp
                }
            }
            var showIntroSplash by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
            BitChordTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    BitChordApp()
                    if (showIntroSplash) {
                        com.example.ui.components.LyrafyIntroSplashScreen(
                            onFinished = { showIntroSplash = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BitChordApp(
    homeViewModel: MusicHomeViewModel = viewModel()
) {
    val context = LocalContext.current

    // Runtime permission launcher for Android 13+ Notification progress bar during music downloads
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    // Proactively request POST_NOTIFICATIONS permission on Android 13+ so status bar download % works immediately
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                runCatching {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    val ensureNotificationPermissionAndRun: (() -> Unit) -> Unit = remember(context) {
        { action ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    runCatching {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
            action()
        }
    }

    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val playlistScreenState by homeViewModel.playlistScreenState.collectAsStateWithLifecycle()
    val artistScreenState by homeViewModel.artistScreenState.collectAsStateWithLifecycle()
    val isSettingsVisible by homeViewModel.isSettingsVisible.collectAsStateWithLifecycle()
    val isAccountIntegrationsVisible by homeViewModel.isAccountIntegrationsVisible.collectAsStateWithLifecycle()
    val spotifyAccount by homeViewModel.spotifyAccount.collectAsStateWithLifecycle()
    val youtubeMusicAccount by homeViewModel.youtubeMusicAccount.collectAsStateWithLifecycle()
    val isIntegrationSyncing by homeViewModel.isIntegrationSyncing.collectAsStateWithLifecycle()
    val spotifyImportProgress by homeViewModel.spotifyImportProgress.collectAsStateWithLifecycle()
    val activeImportSourceLabel by homeViewModel.activeImportSourceLabel.collectAsStateWithLifecycle()
    val spotifyCollections by homeViewModel.spotifyCollections.collectAsStateWithLifecycle()
    val selectedSpotifyCollectionIds by homeViewModel.selectedSpotifyCollectionIds.collectAsStateWithLifecycle()
    val isLoadingSpotifyCollections by homeViewModel.isLoadingSpotifyCollections.collectAsStateWithLifecycle()
    val integrationStatusMessage by homeViewModel.integrationStatusMessage.collectAsStateWithLifecycle()

    val searchUiState by homeViewModel.searchUiState.collectAsStateWithLifecycle()
    val hasActiveTrack by homeViewModel.hasActiveTrack.collectAsStateWithLifecycle()
    val currentPlayingTrackId by homeViewModel.currentPlayingTrackId.collectAsStateWithLifecycle()
    val isPlayingAudio by homeViewModel.isPlayingAudio.collectAsStateWithLifecycle()
    val favoriteIds by homeViewModel.favoriteIds.collectAsStateWithLifecycle()
    val favoriteTracks by homeViewModel.favoriteTracks.collectAsStateWithLifecycle()
    val recentTracks by homeViewModel.recentTracks.collectAsStateWithLifecycle()
    val artistAvatarMap by homeViewModel.artistAvatarMap.collectAsStateWithLifecycle()
    val libraryPlaylists by homeViewModel.libraryPlaylists.collectAsStateWithLifecycle()
    val subscribedArtists by homeViewModel.subscribedArtists.collectAsStateWithLifecycle()
    val savedPlaylistIds by homeViewModel.savedPlaylistIds.collectAsStateWithLifecycle()
    val downloadedTracks by homeViewModel.downloadedTracks.collectAsStateWithLifecycle()
    val downloadedTrackIds by homeViewModel.downloadedTrackIds.collectAsStateWithLifecycle()
    val activeDownloads by homeViewModel.activeDownloads.collectAsStateWithLifecycle()
    val downloadStorageLocation by homeViewModel.downloadStorageLocation.collectAsStateWithLifecycle()
    val hasAudioPermission by homeViewModel.hasAudioPermission.collectAsStateWithLifecycle()
    val localDeviceTracks by homeViewModel.localDeviceTracks.collectAsStateWithLifecycle()
    val listeningStats by homeViewModel.listeningStats.collectAsStateWithLifecycle()

    val isPlayerExpanded by homeViewModel.isPlayerExpanded.collectAsStateWithLifecycle()
    val activePlayerSubView by homeViewModel.activePlayerSubView.collectAsStateWithLifecycle()
    val currentLyrics by homeViewModel.currentLyrics.collectAsStateWithLifecycle()
    val selectedLyricsProvider by homeViewModel.selectedLyricsProvider.collectAsStateWithLifecycle()
    val lyricsSettings by homeViewModel.lyricsSettings.collectAsStateWithLifecycle()
    val bitChordLyricsLines by homeViewModel.bitChordLyricsLines.collectAsStateWithLifecycle()
    val activeLyricsSource by homeViewModel.activeLyricsSource.collectAsStateWithLifecycle()
    val isEmbeddedLyrics by homeViewModel.isEmbeddedLyrics.collectAsStateWithLifecycle()
    val isLoadingLyrics by homeViewModel.isLoadingLyrics.collectAsStateWithLifecycle()
    val lyricsProviderStates by homeViewModel.lyricsProviderStates.collectAsStateWithLifecycle()
    val lyricsProviderResults by homeViewModel.lyricsProviderResults.collectAsStateWithLifecycle()
    val translatedLyrics by homeViewModel.translatedLyrics.collectAsStateWithLifecycle()
    val romanizedLyrics by homeViewModel.romanizedLyrics.collectAsStateWithLifecycle()
    val isTranslationEnabled by homeViewModel.isTranslationEnabled.collectAsStateWithLifecycle()
    val isRomanizationEnabled by homeViewModel.isRomanizationEnabled.collectAsStateWithLifecycle()
    val isTranslatingLyrics by homeViewModel.isTranslatingLyrics.collectAsStateWithLifecycle()
    val customCoverOverrides by homeViewModel.customCoverOverrides.collectAsStateWithLifecycle()
    val isVideoModeActive by homeViewModel.isVideoModeActive.collectAsStateWithLifecycle()
    val activeVideoStreamUrl by homeViewModel.activeVideoStreamUrl.collectAsStateWithLifecycle()
    val isResolvingVideoStream by homeViewModel.isResolvingVideoStream.collectAsStateWithLifecycle()
    val selectedTabIndex by homeViewModel.selectedTab.collectAsStateWithLifecycle()
    val uiMessage by homeViewModel.uiMessage.collectAsStateWithLifecycle()
    val selectedMoodGenre by homeViewModel.selectedMoodGenre.collectAsStateWithLifecycle()
    val ytMusicProfile by homeViewModel.ytMusicProfile.collectAsStateWithLifecycle()
    val ytMusicChannelName by homeViewModel.ytMusicChannelName.collectAsStateWithLifecycle()
    val ytMusicCookie by homeViewModel.ytMusicCookie.collectAsStateWithLifecycle()
    val liquidGlassSetting by com.example.data.settings.AppSettings.liquidGlass.collectAsStateWithLifecycle()
    val fullLiquidGlassSetting by com.example.data.settings.AppSettings.fullLiquidGlassUi.collectAsStateWithLifecycle()
    val showMusicRecognitionButton by com.example.data.settings.AppSettings.showMusicRecognitionButton.collectAsStateWithLifecycle()
    val liquidGlassEnabled = (liquidGlassSetting || fullLiquidGlassSetting) && com.example.ui.components.isGlassSupported()

    val appBackdrop = com.example.ui.components.backdrop.backdrops.rememberLayerBackdrop()
    val tabBarScrollConnection = com.example.ui.components.floatingtabbar.rememberFloatingTabBarScrollConnection()
    val playerDock = remember { com.example.ui.player.PlayerDock() }
    var isRecognitionScreenVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiMessage) {
        if (uiMessage != null) {
            delay(3000L)
            homeViewModel.clearUiMessage()
        }
    }

    val selectedTab = when (selectedTabIndex) {
        1 -> BitChordTab.EXPLORE
        2 -> BitChordTab.LIBRARY
        3 -> BitChordTab.SEARCH
        else -> BitChordTab.HOME
    }

    LaunchedEffect(selectedTab) {
        tabBarScrollConnection.expand()
    }

    BackHandler(
        enabled = selectedTab == BitChordTab.EXPLORE &&
            selectedMoodGenre != null &&
            !isPlayerExpanded &&
            !playlistScreenState.isVisible &&
            !artistScreenState.isVisible &&
            !isSettingsVisible &&
            !isAccountIntegrationsVisible
    ) {
        homeViewModel.closeMoodGenre()
    }

    BackHandler(
        enabled = selectedTab != BitChordTab.HOME &&
            (selectedTab != BitChordTab.EXPLORE || selectedMoodGenre == null) &&
            !isPlayerExpanded &&
            !playlistScreenState.isVisible &&
            !artistScreenState.isVisible &&
            !isSettingsVisible &&
            !isAccountIntegrationsVisible
    ) {
        homeViewModel.selectTab(0)
    }

    val candidateTracksForAdd = remember(homeState.pintasanCepatItems, homeState.topShelfItems, favoriteTracks) {
        (favoriteTracks + homeState.pintasanCepatItems + homeState.topShelfItems).distinctBy { it.id }
    }

    val playerState by homeViewModel.playerState.collectAsStateWithLifecycle()
    val useDynamicMusicTheme by com.example.data.settings.AppSettings.useDynamicMusicTheme.collectAsStateWithLifecycle()
    val activeSongArtworkUrl = playerState.currentTrack?.highResThumbnail ?: playerState.currentTrack?.smallThumbnail
    val dynamicArtworkColor = rememberDynamicArtworkColor(
        artworkUrl = activeSongArtworkUrl,
        enabled = useDynamicMusicTheme
    )

    val updateInfo by com.example.data.remote.AppUpdateChecker.updateInfo.collectAsStateWithLifecycle()
    val showUpdateDialog by com.example.data.remote.AppUpdateChecker.showUpdateDialog.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        com.example.data.remote.AppUpdateChecker.checkForUpdates(manualTrigger = false)
    }

    androidx.compose.runtime.CompositionLocalProvider(
        com.example.ui.components.LocalLiquidGlassEnabled provides liquidGlassEnabled,
        com.example.ui.components.LocalAppBackdrop provides appBackdrop,
        com.example.ui.player.LocalPlayerDock provides playerDock
    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceObsidian)
    ) {
        if (useDynamicMusicTheme && dynamicArtworkColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                dynamicArtworkColor.copy(alpha = 0.38f),
                                dynamicArtworkColor.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (liquidGlassEnabled) {
                        Modifier.layerBackdrop(appBackdrop)
                    } else {
                        Modifier
                    }
                )
        ) {
        // 1. Primary Tab Content (Home, Explore, Library, Search)
        val effectiveYtMusicAvatarUrl = ytMusicProfile?.avatarUrl?.takeIf { it.isNotBlank() }
            ?: youtubeMusicAccount.avatarUrl.takeIf { it.isNotBlank() }
        val isYtMusicSignedIn = youtubeMusicAccount.isConnected || (ytMusicProfile != null)
        val effectiveYtMusicUserName = ytMusicProfile?.name?.takeIf { it.isNotBlank() }
            ?: youtubeMusicAccount.accountName.takeIf { it.isNotBlank() }

        when (selectedTab) {
            BitChordTab.HOME -> {
                ListenNowScreen(
                    uiState = homeState,
                    favoriteIds = favoriteIds,
                    downloadedTrackIds = downloadedTrackIds,
                    activeDownloads = activeDownloads,
                    libraryPlaylists = libraryPlaylists,
                    scrollConnection = tabBarScrollConnection,
                    ytMusicAvatarUrl = effectiveYtMusicAvatarUrl,
                    activeSongArtworkUrl = activeSongArtworkUrl,
                    onTrackClick = homeViewModel::onHomeItemClicked,
                    onCarouselItemClick = homeViewModel::onHomeItemClicked,
                    onPlaySectionAll = homeViewModel::onPlaySectionAll,
                    onPlayCommunityTrack = { track, queue, sourceLabel ->
                        homeViewModel.playSingleOrQueueTrack(
                            item = track,
                            contextQueue = queue,
                            sourceLabel = sourceLabel
                        )
                    },
                    onPlayCommunityFirstTrack = homeViewModel::onPlayCommunityPlaylistFirstTrack,
                    onStartCommunityRadio = homeViewModel::onStartCommunityPlaylistRadio,
                    onToggleSaveCommunityPlaylist = homeViewModel::onToggleSaveCommunityPlaylist,
                    onOpenSettings = homeViewModel::openSettingsScreen,
                    onOpenAccountIntegrations = homeViewModel::openAccountIntegrationsScreen,
                    onOpenTrackMenu = homeViewModel::onOpenTrackMenu,
                    onDismissTrackMenu = homeViewModel::onDismissTrackMenu,
                    onToggleFavoriteTrack = homeViewModel::toggleFavoriteTrack,
                    onAddTrackToPlaylist = homeViewModel::addTrackToLibraryPlaylist,
                    onCreatePlaylistAndAddTrack = homeViewModel::createCustomPlaylistAndAddTrack,
                    onDownloadTrack = { track ->
                        ensureNotificationPermissionAndRun {
                            homeViewModel.downloadSingleTrack(track)
                        }
                    },
                    onRemoveDownload = homeViewModel::deleteDownloadedTrack,
                    onOpenArtistByName = { artistName ->
                        homeViewModel.openArtistProfileByName(artistName)
                    },
                    onRemoveFromRecents = homeViewModel::onRemoveFromRecents,
                    onClearAllRecents = homeViewModel::onClearAllRecents,
                    onRetry = homeViewModel::loadHomeData
                )
            }

            BitChordTab.EXPLORE -> {
                val activeMoodGenre = selectedMoodGenre
                if (activeMoodGenre == null) {
                    com.example.ui.screens.ExploreScreen(
                        repo = homeViewModel.ytMusicRepository,
                        scrollConnection = tabBarScrollConnection,
                        activeSongArtworkUrl = activeSongArtworkUrl,
                        onOpenMoodGenre = homeViewModel::openMoodGenre,
                        onPlaySong = { song, queue ->
                            homeViewModel.playSingleOrQueueTrack(
                                item = song.toMusicItem(),
                                contextQueue = queue.map { it.toMusicItem() },
                                sourceLabel = "Playing from Jelajahi"
                            )
                        },
                        onOpenCard = { card ->
                            val musicItem = card.toMusicItem()
                            if (musicItem.isArtist) {
                                homeViewModel.openArtistProfile(musicItem)
                            } else {
                                homeViewModel.openPlaylistOrAlbumDetail(musicItem)
                            }
                        }
                    )
                } else {
                    com.example.ui.screens.MoodGenrePlaylistsScreen(
                        title = activeMoodGenre.title,
                        browseId = activeMoodGenre.browseId,
                        params = activeMoodGenre.params,
                        repo = homeViewModel.ytMusicRepository,
                        onBack = homeViewModel::closeMoodGenre,
                        onPlaySong = { song, queue ->
                            homeViewModel.playSingleOrQueueTrack(
                                item = song.toMusicItem(),
                                contextQueue = queue.map { it.toMusicItem() },
                                sourceLabel = "Playing from ${activeMoodGenre.title}"
                            )
                        },
                        onOpenCard = { card ->
                            val musicItem = card.toMusicItem()
                            if (musicItem.isArtist) {
                                homeViewModel.openArtistProfile(musicItem)
                            } else {
                                homeViewModel.openPlaylistOrAlbumDetail(musicItem)
                            }
                        }
                    )
                }
            }

            BitChordTab.LIBRARY -> {
                LibraryScreen(
                    listeningStats = listeningStats,
                    favoriteTracks = favoriteTracks,
                    libraryPlaylists = libraryPlaylists,
                    downloadedTracks = downloadedTracks,
                    localDeviceTracks = localDeviceTracks,
                    activeDownloads = activeDownloads,
                    candidateTracksForAdd = candidateTracksForAdd,
                    scrollConnection = tabBarScrollConnection,
                    subscribedArtists = subscribedArtists,
                    recentTracks = recentTracks,
                    artistAvatarMap = artistAvatarMap,
                    hasAudioPermission = hasAudioPermission,
                    isYtMusicSignedIn = isYtMusicSignedIn,
                    ytMusicUserName = effectiveYtMusicUserName,
                    ytMusicAvatarUrl = effectiveYtMusicAvatarUrl,
                    activeSongArtworkUrl = activeSongArtworkUrl,
                    importProgress = spotifyImportProgress,
                    importSourceLabel = activeImportSourceLabel,
                    onPreviewPlaylistUrl = homeViewModel::previewPlaylistUrl,
                    onImportPlaylistUrl = homeViewModel::importAnyPlaylistUrl,
                    onImportPlaylistFile = homeViewModel::importPlaylistFromFile,
                    onAudioPermissionResult = { granted ->
                        homeViewModel.onAudioPermissionResult(granted, openPlaylistAfterGrant = true)
                    },
                    onResolveArtistAvatar = homeViewModel::resolveArtistAvatarIfNeeded,
                    onOpenLikedSongs = homeViewModel::openLikedSongsPlaylist,
                    onOpenDownloads = homeViewModel::openDownloadsPlaylist,
                    onOpenLocalMusic = homeViewModel::openLocalMusicPlaylist,
                    onOpenPlaylist = homeViewModel::openLibraryPlaylist,
                    onCreatePlaylist = homeViewModel::createCustomPlaylist,
                    onRenamePlaylist = homeViewModel::renameLibraryPlaylist,
                    onDeletePlaylist = homeViewModel::deleteLibraryPlaylist,
                    onTogglePinPlaylist = homeViewModel::togglePinLibraryPlaylist,
                    onAddTrackToPlaylist = homeViewModel::addTrackToLibraryPlaylist,
                    onDownloadPlaylist = { userPlaylist ->
                        ensureNotificationPermissionAndRun {
                            homeViewModel.downloadMusicItemCollection(userPlaylist.toMusicItem())
                        }
                    },
                    onOpenImportIntegrations = homeViewModel::openAccountIntegrationsScreen,
                    onOpenSettings = homeViewModel::openSettingsScreen,
                    onOpenArtistByName = { artistName ->
                        homeViewModel.openArtistProfileByName(artistName)
                    },
                    onUpdateUsername = homeViewModel::updateUsername,
                    onPlayTopTrack = { topTrack ->
                        homeViewModel.playSingleOrQueueTrack(
                            item = topTrack,
                            contextQueue = listOf(topTrack),
                            sourceLabel = "Playing from Top Song"
                        )
                    }
                )
            }

            BitChordTab.SEARCH -> {
                SearchScreen(
                    searchState = searchUiState,
                    hasActiveMiniPlayer = hasActiveTrack,
                    scrollConnection = tabBarScrollConnection,
                    onQueryChange = homeViewModel::onSearchQueryChange,
                    onFillQuery = homeViewModel::onFillQueryFromSuggestion,
                    onClearQuery = homeViewModel::onClearSearchQuery,
                    onSubmitSearch = homeViewModel::onSubmitSearch,
                    onSelectFilter = homeViewModel::onSelectSearchFilter,
                    onItemClick = homeViewModel::onSearchItemClicked,
                    onOpenTrackMenu = homeViewModel::onOpenTrackMenu,
                    onBackToHome = { homeViewModel.selectTab(0) }
                )
            }
        }

        // 2. Dedicated Artist Profile Screen (rendered before PlaylistDetailScreen so playlists clicked inside ArtistProfile open on top!)
        AnimatedVisibility(
            visible = artistScreenState.isVisible && artistScreenState.artistProfile != null,
            enter = fadeIn(tween(220)) + slideInHorizontally(
                animationSpec = tween(260),
                initialOffsetX = { it / 3 }
            ),
            exit = fadeOut(tween(180)) + slideOutHorizontally(
                animationSpec = tween(220),
                targetOffsetX = { it / 3 }
            )
        ) {
            val profile = artistScreenState.artistProfile
            if (profile != null) {
                val isFollowed = artistScreenState.followedArtistIds.contains(profile.id) ||
                    artistScreenState.followedArtistIds.contains(profile.name) ||
                    subscribedArtists.any { it.id == profile.id || it.name.equals(profile.name, ignoreCase = true) }
                ArtistProfileScreen(
                    artistProfile = profile,
                    isLoading = artistScreenState.isLoading,
                    isFollowing = isFollowed,
                    currentPlayingTrackId = currentPlayingTrackId,
                    favoriteIds = favoriteIds,
                    hasActiveMiniPlayer = hasActiveTrack,
                    onBack = homeViewModel::closeArtistProfile,
                    onToggleFollow = { homeViewModel.toggleFollowArtist(profile.id) },
                    onPlayTrack = { track, queue, sourceLabel ->
                        homeViewModel.playSingleOrQueueTrack(
                            item = track,
                            contextQueue = queue,
                            sourceLabel = sourceLabel
                        )
                    },
                    onOpenReleaseOrPlaylist = { item ->
                        homeViewModel.openPlaylistOrAlbumDetail(item)
                    },
                    onPlayReleaseDirect = { item ->
                        homeViewModel.onPlayCommunityPlaylistFirstTrack(item)
                    },
                    onOpenRelatedArtist = { relatedItem ->
                        homeViewModel.openArtistProfile(relatedItem)
                    },
                    onToggleFavoriteTrack = homeViewModel::toggleFavoriteTrack,
                    onOpenTrackMenu = homeViewModel::onOpenTrackMenu
                )
            }
        }

        // 3. Playlist / Album Detail Overlay Screen (rendered above ArtistProfileScreen so clicking Artist Releases / Curated Playlists opens visibly)
        AnimatedVisibility(
            visible = playlistScreenState.isVisible && playlistScreenState.playlist != null,
            enter = fadeIn(tween(220)) + slideInHorizontally(
                animationSpec = tween(260),
                initialOffsetX = { it / 3 }
            ),
            exit = fadeOut(tween(180)) + slideOutHorizontally(
                animationSpec = tween(220),
                targetOffsetX = { it / 3 }
            )
        ) {
            val openPlaylistId = playlistScreenState.playlist?.id
            val isLibraryOwned = openPlaylistId != null && libraryPlaylists.any { it.id == openPlaylistId }

            PlaylistDetailScreen(
                state = playlistScreenState,
                currentPlayingTrackId = currentPlayingTrackId,
                isPlayingAudio = isPlayingAudio,
                favoriteIds = favoriteIds,
                downloadedTrackIds = downloadedTrackIds,
                activeDownloads = activeDownloads,
                libraryPlaylists = libraryPlaylists,
                savedPlaylistIds = savedPlaylistIds,
                username = listeningStats.userHandle,
                onBackClick = homeViewModel::closePlaylistDetail,
                onPlayAllClick = homeViewModel::onPlayAllFromPlaylist,
                onShufflePlayClick = homeViewModel::onShufflePlayPlaylist,
                onPlayPlaylistNext = homeViewModel::playPlaylistNext,
                onAddPlaylistToQueue = homeViewModel::addPlaylistToQueue,
                onToggleSearchClick = homeViewModel::onTogglePlaylistSearch,
                onSearchQueryChange = homeViewModel::onUpdatePlaylistSearchQuery,
                onToggleSortClick = homeViewModel::onTogglePlaylistSort,
                onTrackClick = { clickedTrack, displayedList ->
                    val plTitle = playlistScreenState.playlist?.title ?: "Playlist"
                    homeViewModel.playSingleOrQueueTrack(
                        item = clickedTrack,
                        contextQueue = displayedList,
                        sourceLabel = "Playing from $plTitle"
                    )
                },
                onTrackMenuClick = homeViewModel::onOpenTrackMenu,
                onToggleFavoriteTrack = homeViewModel::toggleFavoriteTrack,
                onAddTrackToPlaylist = homeViewModel::addTrackToLibraryPlaylist,
                onCreatePlaylistAndAddTrack = homeViewModel::createCustomPlaylistAndAddTrack,
                onRemoveTrackFromCurrentPlaylist = if (isLibraryOwned && openPlaylistId != null) {
                    { trackId -> homeViewModel.removeTrackFromLibraryPlaylist(openPlaylistId, trackId) }
                } else null,
                onReorderPlaylistTracks = { orderedTracks ->
                    if (openPlaylistId != null) {
                        homeViewModel.reorderPlaylistTracks(openPlaylistId, orderedTracks)
                    }
                },
                onDownloadSingleTrack = { track ->
                    ensureNotificationPermissionAndRun {
                        homeViewModel.downloadSingleTrack(track)
                    }
                },
                onRemoveDownload = homeViewModel::deleteDownloadedTrack,
                onDownloadWholePlaylist = { detail ->
                    ensureNotificationPermissionAndRun {
                        homeViewModel.downloadPlaylistOrAlbum(detail)
                    }
                },
                onToggleSavePlaylistToLibrary = homeViewModel::toggleSavePlaylistDetailToLibrary,
                onRenamePlaylist = if (isLibraryOwned && openPlaylistId != null) {
                    { newTitle -> homeViewModel.renameLibraryPlaylist(openPlaylistId, newTitle) }
                } else null,
                onTogglePinPlaylist = if (isLibraryOwned && openPlaylistId != null) {
                    { homeViewModel.togglePinLibraryPlaylist(openPlaylistId) }
                } else null,
                onDeletePlaylist = if (isLibraryOwned && openPlaylistId != null) {
                    { homeViewModel.deleteLibraryPlaylist(openPlaylistId) }
                } else null,
                onOpenArtistByName = { artistName ->
                    homeViewModel.openArtistProfileByName(artistName)
                }
            )
        }

        // 4. Settings Screen
        AnimatedVisibility(
            visible = isSettingsVisible,
            enter = fadeIn(tween(200)) + slideInVertically(
                animationSpec = tween(240),
                initialOffsetY = { it / 4 }
            ),
            exit = fadeOut(tween(160)) + slideOutVertically(
                animationSpec = tween(200),
                targetOffsetY = { it / 4 }
            )
        ) {
            SettingsScreen(
                onBack = homeViewModel::closeSettingsScreen,
                onOpenAccountIntegrations = {
                    homeViewModel.closeSettingsScreen()
                    homeViewModel.openAccountIntegrationsScreen()
                },
                spotifyConnected = spotifyAccount.isConnected,
                ytMusicConnected = youtubeMusicAccount.isConnected,
                ytMusicAccount = ytMusicProfile,
                downloadStorageLocation = downloadStorageLocation,
                onSelectDownloadStorageLocation = homeViewModel::setDownloadStorageLocation,
                downloadedTracksCount = downloadedTracks.size,
                listeningStats = listeningStats,
                recentTracks = recentTracks,
                lyricsSettings = lyricsSettings,
                onToggleLyricsSourceEnabled = homeViewModel::toggleLyricsSourceEnabled,
                onMoveLyricsSourceOrder = homeViewModel::moveLyricsSourceOrder,
                onTogglePrioritizeSyllable = homeViewModel::setPrioritizeSyllableSync,
                onSavePaxSenixApiKey = homeViewModel::savePaxSenixApiKey,
                onResetLyricsSourcesDefault = homeViewModel::resetLyricsSourcesToDefault,
                onChangeLyricsOffsetMs = homeViewModel::setCurrentTrackLyricsOffsetMs,
                onSelectTranslationLanguage = homeViewModel::setLyricsTranslationLanguage,
                onSetSyncedLyricsEnabled = homeViewModel::setSyncedLyricsEnabled,
                onSetLyricsBlurEnabled = homeViewModel::setLyricsBlurEnabled,
                onSetReduceLyricsAnimation = homeViewModel::setReduceLyricsAnimation,
                onDisconnectYtMusic = {
                    homeViewModel.disconnectIntegration(com.example.data.model.IntegrationPlatform.YOUTUBE_MUSIC)
                },
                onConnectSpotifyToken = { token ->
                    homeViewModel.connectSpotifyAccount("Spotify Account", token)
                }
            )
        }
        } // end layerBackdrop container (Login and WebView overlays rendered at Root Level outside backdrop to avoid flicker)

        // 5. Account Integrations Screen (YouTube Music Google & Spotify)
        AnimatedVisibility(
            visible = isAccountIntegrationsVisible,
            enter = fadeIn(tween(200)) + slideInVertically(
                animationSpec = tween(240),
                initialOffsetY = { it / 4 }
            ),
            exit = fadeOut(tween(160)) + slideOutVertically(
                animationSpec = tween(200),
                targetOffsetY = { it / 4 }
            )
        ) {
            AccountIntegrationsScreen(
                spotifyAccount = spotifyAccount,
                ytMusicAccount = youtubeMusicAccount,
                ytMusicProfile = ytMusicProfile,
                ytMusicChannelName = ytMusicChannelName,
                ytMusicCookie = ytMusicCookie,
                isSyncing = isIntegrationSyncing,
                spotifyImportProgress = spotifyImportProgress,
                spotifyCollections = spotifyCollections,
                selectedSpotifyCollectionIds = selectedSpotifyCollectionIds,
                isLoadingSpotifyCollections = isLoadingSpotifyCollections,
                statusMessage = integrationStatusMessage,
                onBack = homeViewModel::closeAccountIntegrationsScreen,
                onConnectSpotify = homeViewModel::connectSpotifyAccount,
                onConnectYtMusic = homeViewModel::connectYouTubeMusicAccount,
                onCapturedYtMusicSession = homeViewModel::onCapturedYtMusicSession,
                onSelectYtMusicChannel = homeViewModel::onSelectYtMusicChannel,
                onToggleSpotifyCollection = homeViewModel::toggleSpotifyCollectionSelection,
                onSelectAllSpotifyCollections = homeViewModel::selectAllSpotifyCollections,
                onSelectNoneSpotifyCollections = homeViewModel::selectNoneSpotifyCollections,
                onRefreshSpotifyCollections = homeViewModel::refreshSpotifyAccountCollections,
                onImportSelectedSpotifyCollections = {
                    homeViewModel.importSelectedSpotifyCollections {
                        homeViewModel.closeAccountIntegrationsScreen()
                        homeViewModel.selectTab(2)
                    }
                },
                onDisconnectSpotify = { homeViewModel.disconnectIntegration(com.example.data.model.IntegrationPlatform.SPOTIFY) },
                onDisconnectYtMusic = { homeViewModel.disconnectIntegration(com.example.data.model.IntegrationPlatform.YOUTUBE_MUSIC) },
                onClearStatusMessage = homeViewModel::clearIntegrationStatusMessage,
                onNavigateToLibrary = {
                    homeViewModel.closeAccountIntegrationsScreen()
                    homeViewModel.selectTab(2)
                }
            )
        }

        // 6. Bottom Ambient Scrim
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(176.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.72f),
                            SurfaceObsidian.copy(alpha = 0.96f)
                        )
                    )
                )
        )

        // 7. Floating Mini Player + Floating Pill / Liquid Glass Navigation Dock (4 Tabs: Home, Explore, Library, Search)
        val onSelectTabCommon: (BitChordTab) -> Unit = { tab ->
            if (playlistScreenState.isVisible) {
                homeViewModel.closePlaylistDetail()
            }
            if (artistScreenState.isVisible) {
                homeViewModel.closeArtistProfile()
            }
            if (isSettingsVisible) {
                homeViewModel.closeSettingsScreen()
            }
            if (isAccountIntegrationsVisible) {
                homeViewModel.closeAccountIntegrationsScreen()
            }
            if (tab != BitChordTab.EXPLORE && selectedMoodGenre != null) {
                homeViewModel.closeMoodGenre()
            }
            when (tab) {
                BitChordTab.HOME -> homeViewModel.selectTab(0)
                BitChordTab.EXPLORE -> homeViewModel.selectTab(1)
                BitChordTab.LIBRARY -> homeViewModel.selectTab(2)
                BitChordTab.SEARCH -> homeViewModel.selectTab(3)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = if (liquidGlassEnabled) 0.dp else 12.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Toast / Feedback Message Pill
            AnimatedVisibility(
                visible = uiMessage != null,
                enter = fadeIn(tween(180)) + slideInVertically { it / 2 },
                exit = fadeOut(tween(150)) + slideOutVertically { it / 2 }
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF232734))
                        .border(1.dp, Color(0xFF8AB4F8).copy(alpha = 0.4f), RoundedCornerShape(50))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = uiMessage.orEmpty(),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }

            // Floating "Kenali Musik" Button (ONLY on Home & Search tabs when enabled, auto-hides on scroll down)
            val isRecognitionFabVisible = showMusicRecognitionButton &&
                (selectedTab == BitChordTab.HOME || selectedTab == BitChordTab.SEARCH) &&
                !playlistScreenState.isVisible &&
                !artistScreenState.isVisible &&
                !isSettingsVisible &&
                !isAccountIntegrationsVisible &&
                !isPlayerExpanded &&
                !isRecognitionScreenVisible &&
                !tabBarScrollConnection.isInline

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, bottom = 10.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                com.example.ui.screens.FloatingMusicRecognitionButton(
                    visible = isRecognitionFabVisible,
                    liquidGlassEnabled = liquidGlassEnabled,
                    onClick = { isRecognitionScreenVisible = true },
                    dynamicAccentColor = if (dynamicArtworkColor != Color.Transparent) dynamicArtworkColor else Color(0xFFE2B963)
                )
            }

            if (liquidGlassEnabled) {
                if (!isPlayerExpanded) {
                    IsolatedGlassNavBarHost(
                        selectedTab = selectedTab,
                        onSelectTab = onSelectTabCommon,
                        scrollConnection = tabBarScrollConnection,
                        playerStateFlow = homeViewModel.playerState,
                        customCoverOverrides = customCoverOverrides,
                        dock = playerDock,
                        dynamicAccentColor = dynamicArtworkColor,
                        onExpandPlayer = { homeViewModel.openFullPlayer(PlayerSubView.MAIN) },
                        onTogglePlayPause = homeViewModel::onTogglePlayPause,
                        onNextTrack = homeViewModel::onNextTrack,
                        onPreviousTrack = homeViewModel::onPreviousTrack
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = hasActiveTrack && !isPlayerExpanded,
                    enter = slideInVertically(
                        animationSpec = spring(
                            stiffness = 195f,
                            dampingRatio = Spring.DampingRatioNoBouncy
                        ),
                        initialOffsetY = { it }
                    ) + fadeIn(animationSpec = spring(stiffness = 195f, dampingRatio = Spring.DampingRatioNoBouncy)),
                    exit = slideOutVertically(
                        animationSpec = spring(
                            stiffness = 195f,
                            dampingRatio = Spring.DampingRatioNoBouncy
                        ),
                        targetOffsetY = { it }
                    ) + fadeOut(animationSpec = spring(stiffness = 195f, dampingRatio = Spring.DampingRatioNoBouncy))
                ) {
                    IsolatedFloatingMiniPlayerHost(
                        playerStateFlow = homeViewModel.playerState,
                        customCoverOverrides = customCoverOverrides,
                        dock = playerDock,
                        dynamicAccentColor = dynamicArtworkColor,
                        onExpandPlayer = { homeViewModel.openFullPlayer(PlayerSubView.MAIN) },
                        onTogglePlayPause = homeViewModel::onTogglePlayPause,
                        onNextTrack = homeViewModel::onNextTrack,
                        onPreviousTrack = homeViewModel::onPreviousTrack
                    )
                }

                if (hasActiveTrack && !isPlayerExpanded) {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                FloatingPillBottomBar(
                    selectedTab = selectedTab,
                    onSelectTab = onSelectTabCommon,
                    dynamicAccentColor = dynamicArtworkColor
                )
            }
        }

        // 8. Full Screen Expandable Music Player (Isolated State Host so scrolling behind never lags)
        AnimatedVisibility(
            visible = isPlayerExpanded && hasActiveTrack,
            enter = slideInVertically(
                animationSpec = spring(
                    stiffness = 195f,
                    dampingRatio = Spring.DampingRatioNoBouncy
                ),
                initialOffsetY = { it }
            ) + fadeIn(animationSpec = spring(stiffness = 195f, dampingRatio = Spring.DampingRatioNoBouncy)),
            exit = slideOutVertically(
                animationSpec = spring(
                    stiffness = 195f,
                    dampingRatio = Spring.DampingRatioNoBouncy
                ),
                targetOffsetY = { it }
            ) + fadeOut(animationSpec = spring(stiffness = 195f, dampingRatio = Spring.DampingRatioNoBouncy))
        ) {
            IsolatedFullMusicPlayerHost(
                isVisible = isPlayerExpanded,
                playerStateFlow = homeViewModel.playerState,
                activeSubView = activePlayerSubView,
                lyricsData = currentLyrics,
                selectedLyricsProvider = selectedLyricsProvider,
                favoriteIds = favoriteIds,
                downloadedTrackIds = downloadedTrackIds,
                activeDownloads = activeDownloads,
                libraryPlaylists = libraryPlaylists,
                customCoverOverrides = customCoverOverrides,
                isVideoModeActive = isVideoModeActive,
                activeVideoStreamUrl = activeVideoStreamUrl,
                isResolvingVideoStream = isResolvingVideoStream,
                onToggleVideoMode = homeViewModel::toggleVideoPlayerMode,
                onClosePlayer = homeViewModel::closeFullPlayer,
                onChangeSubView = homeViewModel::setPlayerSubView,
                onSelectLyricsProvider = homeViewModel::selectLyricsProvider,
                onApplyLrcHdCover = homeViewModel::applyLrcHdCover,
                onRestoreDefaultCover = homeViewModel::restoreDefaultCover,
                onToggleFavorite = homeViewModel::toggleFavoriteTrack,
                onDownloadTrack = { track ->
                    ensureNotificationPermissionAndRun {
                        homeViewModel.downloadSingleTrack(track)
                    }
                },
                onRemoveDownload = homeViewModel::deleteDownloadedTrack,
                onAddTrackToPlaylist = homeViewModel::addTrackToLibraryPlaylist,
                onCreatePlaylistAndAddTrack = homeViewModel::createCustomPlaylistAndAddTrack,
                onTogglePlayPause = homeViewModel::onTogglePlayPause,
                onNextTrack = homeViewModel::onNextTrack,
                onPreviousTrack = homeViewModel::onPreviousTrack,
                onSeekTo = homeViewModel::onSeekTo,
                onVolumeChange = homeViewModel::onVolumeChange,
                onVolumeStep = homeViewModel::onVolumeStep,
                onSyncDeviceVolume = homeViewModel::onSyncDeviceVolume,
                onToggleShuffle = homeViewModel::onToggleShuffle,
                onToggleRepeat = homeViewModel::onToggleRepeat,
                onToggleAutoPlay = homeViewModel::onToggleAutoPlay,
                onSelectQueueTrack = homeViewModel::onSelectTrackFromQueue,
                onRemoveQueueTrack = homeViewModel::onRemoveTrackFromQueue,
                onPlayNext = homeViewModel::playTrackNext,
                onAddToQueue = homeViewModel::addTrackToQueue,
                onSetSleepTimerMinutes = homeViewModel::setSleepTimerMinutes,
                onSetSleepTimerEndOfTrack = homeViewModel::setSleepTimerEndOfTrack,
                onCancelSleepTimer = homeViewModel::cancelSleepTimer,
                onOpenArtistProfile = { artistName, thumbUrl ->
                    homeViewModel.openArtistProfileByName(artistName, thumbUrl)
                },
                bitChordLyricsLines = bitChordLyricsLines,
                translatedLyricsLines = translatedLyrics,
                romanizedLyricsLines = romanizedLyrics,
                isTranslationEnabled = isTranslationEnabled,
                isRomanizationEnabled = isRomanizationEnabled,
                isTranslatingLyrics = isTranslatingLyrics,
                activeLyricsSource = activeLyricsSource,
                isEmbeddedLyrics = isEmbeddedLyrics,
                isLoadingLyrics = isLoadingLyrics,
                lyricsSettings = lyricsSettings,
                lyricsProviderStates = lyricsProviderStates,
                lyricsProviderResults = lyricsProviderResults,
                onSelectBitChordLyricsSource = homeViewModel::selectBitChordLyricsSource,
                onToggleLyricsSourceEnabled = homeViewModel::toggleLyricsSourceEnabled,
                onMoveLyricsSourceOrder = homeViewModel::moveLyricsSourceOrder,
                onTogglePrioritizeSyllable = homeViewModel::setPrioritizeSyllableSync,
                onSavePaxSenixApiKey = homeViewModel::savePaxSenixApiKey,
                onResetLyricsSourcesDefault = homeViewModel::resetLyricsSourcesToDefault,
                onChangeTrackLyricsOffsetMs = homeViewModel::setCurrentTrackLyricsOffsetMs,
                onToggleLyricsTranslation = homeViewModel::toggleLyricsTranslation,
                onToggleLyricsRomanization = homeViewModel::toggleLyricsRomanization,
                onSelectTranslationLanguage = homeViewModel::setLyricsTranslationLanguage
            )
        }

        // 8.5 Full Screen "Kenali Musik" (ShazamKit Metrolist) Overlay
        AnimatedVisibility(
            visible = isRecognitionScreenVisible,
            enter = fadeIn(tween(220)) + slideInVertically(
                animationSpec = tween(260),
                initialOffsetY = { it / 4 }
            ),
            exit = fadeOut(tween(180)) + slideOutVertically(
                animationSpec = tween(220),
                targetOffsetY = { it / 4 }
            )
        ) {
            com.example.ui.screens.RecognitionScreen(
                onBack = { isRecognitionScreenVisible = false },
                onPlayInLyra = { title, artist ->
                    isRecognitionScreenVisible = false
                    val searchQuery = "$title $artist".trim()
                    if (playlistScreenState.isVisible) homeViewModel.closePlaylistDetail()
                    if (artistScreenState.isVisible) homeViewModel.closeArtistProfile()
                    if (isSettingsVisible) homeViewModel.closeSettingsScreen()
                    if (isAccountIntegrationsVisible) homeViewModel.closeAccountIntegrationsScreen()
                    homeViewModel.selectTab(3)
                    homeViewModel.onFillQueryFromSuggestion(searchQuery)
                    homeViewModel.onSubmitSearch(searchQuery)
                },
                dynamicAccentColor = if (dynamicArtworkColor != Color.Transparent) dynamicArtworkColor else Color(0xFFE2B963)
            )
        }

        // 9. GitHub Auto-Update & README.md Changelog Dialog
        val activeUpdateInfo = updateInfo
        if (showUpdateDialog && activeUpdateInfo != null) {
            com.example.ui.components.UpdateAvailableDialog(
                updateInfo = activeUpdateInfo,
                onDismiss = {
                    com.example.data.remote.AppUpdateChecker.dismissUpdateDialog()
                }
            )
        }
    }
    } // end CompositionLocalProvider
}

@Composable
private fun IsolatedGlassNavBarHost(
    selectedTab: BitChordTab,
    onSelectTab: (BitChordTab) -> Unit,
    scrollConnection: com.example.ui.components.floatingtabbar.FloatingTabBarScrollConnection,
    playerStateFlow: StateFlow<PlayerUiState>,
    customCoverOverrides: Map<String, String>,
    dock: com.example.ui.player.PlayerDock? = null,
    dynamicAccentColor: Color = Color.Transparent,
    onExpandPlayer: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit
) {
    val playerState by playerStateFlow.collectAsStateWithLifecycle()
    val currentTrack = playerState.currentTrack?.let { track ->
        val customCover = customCoverOverrides[track.id]
        if (!customCover.isNullOrBlank()) track.copy(thumbnailUrl = customCover) else track
    }
    val bottomTabs = remember {
        listOf(
            com.example.ui.components.BottomTab(
                label = "Home",
                icon = com.example.ui.icons.BitChordIcons.Home
            ),
            com.example.ui.components.BottomTab(
                label = "Explore",
                icon = com.example.ui.icons.BitChordIcons.TabExplore
            ),
            com.example.ui.components.BottomTab(
                label = "Library",
                icon = com.example.ui.icons.BitChordIcons.TabLibrary
            ),
            com.example.ui.components.BottomTab(
                label = "Search",
                icon = com.example.ui.icons.BitChordIcons.TabSearch
            )
        )
    }
    val selectedIndex = when (selectedTab) {
        BitChordTab.HOME -> 0
        BitChordTab.EXPLORE -> 1
        BitChordTab.LIBRARY -> 2
        BitChordTab.SEARCH -> 3
    }
    com.example.ui.components.GlassNavBar(
        tabs = bottomTabs,
        selectedIndex = selectedIndex,
        onTabSelected = { idx ->
            val target = when (idx) {
                0 -> BitChordTab.HOME
                1 -> BitChordTab.EXPLORE
                2 -> BitChordTab.LIBRARY
                else -> BitChordTab.SEARCH
            }
            onSelectTab(target)
        },
        scrollConnection = scrollConnection,
        song = currentTrack,
        isPlaying = playerState.isPlaying,
        isLoading = playerState.isLoading,
        onPlayPause = onTogglePlayPause,
        onNext = onNextTrack,
        onPrevious = onPreviousTrack,
        onExpand = onExpandPlayer,
        dock = dock,
        dynamicAccentColor = dynamicAccentColor
    )
}

@Composable
private fun IsolatedFloatingMiniPlayerHost(
    playerStateFlow: StateFlow<PlayerUiState>,
    customCoverOverrides: Map<String, String>,
    dock: com.example.ui.player.PlayerDock? = null,
    dynamicAccentColor: Color = Color.Transparent,
    onExpandPlayer: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit
) {
    val playerState by playerStateFlow.collectAsStateWithLifecycle()
    val customCoverUrl = playerState.currentTrack?.id?.let { customCoverOverrides[it] }
    FloatingMiniPlayer(
        playerState = playerState,
        customCoverUrl = customCoverUrl,
        dock = dock,
        dynamicAccentColor = dynamicAccentColor,
        onExpandPlayer = onExpandPlayer,
        onTogglePlayPause = onTogglePlayPause,
        onNextTrack = onNextTrack
    )
}

@Composable
private fun IsolatedFullMusicPlayerHost(
    isVisible: Boolean,
    playerStateFlow: StateFlow<PlayerUiState>,
    activeSubView: PlayerSubView,
    lyricsData: LyricsData?,
    selectedLyricsProvider: LyricsProvider,
    favoriteIds: Set<String>,
    downloadedTrackIds: Set<String>,
    activeDownloads: Map<String, ActiveDownloadProgress>,
    libraryPlaylists: List<UserLibraryPlaylist>,
    customCoverOverrides: Map<String, String>,
    isVideoModeActive: Boolean,
    activeVideoStreamUrl: String?,
    isResolvingVideoStream: Boolean,
    onToggleVideoMode: () -> Unit,
    onClosePlayer: () -> Unit,
    onChangeSubView: (PlayerSubView) -> Unit,
    onSelectLyricsProvider: (LyricsProvider) -> Unit,
    onApplyLrcHdCover: (MusicItem, String) -> Unit,
    onRestoreDefaultCover: (MusicItem) -> Unit,
    onToggleFavorite: (MusicItem) -> Unit,
    onDownloadTrack: (MusicItem) -> Unit,
    onRemoveDownload: (String) -> Unit,
    onAddTrackToPlaylist: (playlistId: String, track: MusicItem) -> Unit,
    onCreatePlaylistAndAddTrack: (title: String, privacy: PlaylistPrivacy, track: MusicItem) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onVolumeStep: (Boolean) -> Unit,
    onSyncDeviceVolume: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    onSelectQueueTrack: (MusicItem) -> Unit,
    onRemoveQueueTrack: (MusicItem) -> Unit,
    onPlayNext: (MusicItem) -> Unit,
    onAddToQueue: (MusicItem) -> Unit,
    onSetSleepTimerMinutes: (Int, String) -> Unit,
    onSetSleepTimerEndOfTrack: () -> Unit,
    onCancelSleepTimer: () -> Unit,
    onOpenArtistProfile: (String, String) -> Unit,
    bitChordLyricsLines: List<BitChordLyricLine>,
    translatedLyricsLines: List<BitChordLyricLine>?,
    romanizedLyricsLines: List<BitChordLyricLine>?,
    isTranslationEnabled: Boolean,
    isRomanizationEnabled: Boolean,
    isTranslatingLyrics: Boolean,
    activeLyricsSource: LyricsSource?,
    isEmbeddedLyrics: Boolean,
    isLoadingLyrics: Boolean,
    lyricsSettings: LyricsSettingsState,
    lyricsProviderStates: Map<LyricsSource, LyricsProviderState>,
    lyricsProviderResults: Map<LyricsSource, List<BitChordLyricLine>>,
    onSelectBitChordLyricsSource: (LyricsSource) -> Unit,
    onToggleLyricsSourceEnabled: (LyricsSource) -> Unit,
    onMoveLyricsSourceOrder: (Int, Int) -> Unit,
    onTogglePrioritizeSyllable: (Boolean) -> Unit,
    onSavePaxSenixApiKey: (String) -> Unit,
    onResetLyricsSourcesDefault: () -> Unit,
    onChangeTrackLyricsOffsetMs: (Int) -> Unit,
    onToggleLyricsTranslation: () -> Unit,
    onToggleLyricsRomanization: () -> Unit,
    onSelectTranslationLanguage: (String) -> Unit
) {
    val playerState by playerStateFlow.collectAsStateWithLifecycle()
    val currentId = playerState.currentTrack?.id
    val isDownloaded = currentId != null && downloadedTrackIds.contains(currentId)
    val activeDl = if (currentId != null) activeDownloads[currentId] else null

    FullMusicPlayerScreen(
        isVisible = isVisible,
        playerState = playerState,
        activeSubView = activeSubView,
        lyricsData = lyricsData,
        selectedLyricsProvider = selectedLyricsProvider,
        favoriteIds = favoriteIds,
        customCoverOverrides = customCoverOverrides,
        isDownloaded = isDownloaded,
        activeDownloadProgress = activeDl,
        libraryPlaylists = libraryPlaylists,
        isVideoModeActive = isVideoModeActive,
        activeVideoStreamUrl = activeVideoStreamUrl,
        isResolvingVideoStream = isResolvingVideoStream,
        onToggleVideoMode = onToggleVideoMode,
        onClosePlayer = onClosePlayer,
        onChangeSubView = onChangeSubView,
        onSelectLyricsProvider = onSelectLyricsProvider,
        onApplyLrcHdCover = onApplyLrcHdCover,
        onRestoreDefaultCover = onRestoreDefaultCover,
        onToggleFavorite = onToggleFavorite,
        onDownloadTrack = onDownloadTrack,
        onRemoveDownload = onRemoveDownload,
        onAddTrackToPlaylist = onAddTrackToPlaylist,
        onCreatePlaylistAndAddTrack = onCreatePlaylistAndAddTrack,
        onTogglePlayPause = onTogglePlayPause,
        onNextTrack = onNextTrack,
        onPreviousTrack = onPreviousTrack,
        onSeekTo = onSeekTo,
        onVolumeChange = onVolumeChange,
        onVolumeStep = onVolumeStep,
        onSyncDeviceVolume = onSyncDeviceVolume,
        onToggleShuffle = onToggleShuffle,
        onToggleRepeat = onToggleRepeat,
        onToggleAutoPlay = onToggleAutoPlay,
        onSelectQueueTrack = onSelectQueueTrack,
        onRemoveQueueTrack = onRemoveQueueTrack,
        onPlayNext = onPlayNext,
        onAddToQueue = onAddToQueue,
        onSetSleepTimerMinutes = onSetSleepTimerMinutes,
        onSetSleepTimerEndOfTrack = onSetSleepTimerEndOfTrack,
        onCancelSleepTimer = onCancelSleepTimer,
        onOpenArtistProfile = onOpenArtistProfile,
        bitChordLyricsLines = bitChordLyricsLines,
        translatedLyricsLines = translatedLyricsLines,
        romanizedLyricsLines = romanizedLyricsLines,
        isTranslationEnabled = isTranslationEnabled,
        isRomanizationEnabled = isRomanizationEnabled,
        isTranslatingLyrics = isTranslatingLyrics,
        activeLyricsSource = activeLyricsSource,
        isEmbeddedLyrics = isEmbeddedLyrics,
        isLoadingLyrics = isLoadingLyrics,
        lyricsSettings = lyricsSettings,
        lyricsProviderStates = lyricsProviderStates,
        lyricsProviderResults = lyricsProviderResults,
        onSelectBitChordLyricsSource = onSelectBitChordLyricsSource,
        onToggleLyricsSourceEnabled = onToggleLyricsSourceEnabled,
        onMoveLyricsSourceOrder = onMoveLyricsSourceOrder,
        onTogglePrioritizeSyllable = onTogglePrioritizeSyllable,
        onSavePaxSenixApiKey = onSavePaxSenixApiKey,
        onResetLyricsSourcesDefault = onResetLyricsSourcesDefault,
        onChangeTrackLyricsOffsetMs = onChangeTrackLyricsOffsetMs,
        onToggleLyricsTranslation = onToggleLyricsTranslation,
        onToggleLyricsRomanization = onToggleLyricsRomanization,
        onSelectTranslationLanguage = onSelectTranslationLanguage
    )
}

@Composable
private fun rememberDynamicArtworkColor(
    artworkUrl: String?,
    enabled: Boolean
): Color {
    var extractedColor by remember { mutableStateOf(Color.Transparent) }
    val context = LocalContext.current

    LaunchedEffect(artworkUrl, enabled) {
        if (!enabled || artworkUrl.isNullOrBlank()) {
            extractedColor = Color.Transparent
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val loader = coil.Coil.imageLoader(context)
                val req = coil.request.ImageRequest.Builder(context)
                    .data(artworkUrl)
                    .size(64, 64)
                    .allowHardware(false)
                    .build()
                val result = loader.execute(req)
                val drawable = result.drawable
                if (drawable is android.graphics.drawable.BitmapDrawable) {
                    val bitmap = drawable.bitmap
                    if (bitmap != null && !bitmap.isRecycled) {
                        val width = bitmap.width
                        val height = bitmap.height
                        var rAcc = 0L
                        var gAcc = 0L
                        var bAcc = 0L
                        var count = 0
                        val stepX = (width / 8).coerceAtLeast(1)
                        val stepY = (height / 8).coerceAtLeast(1)
                        for (x in 0 until width step stepX) {
                            for (y in 0 until height step stepY) {
                                val pixel = bitmap.getPixel(x, y)
                                val r = android.graphics.Color.red(pixel)
                                val g = android.graphics.Color.green(pixel)
                                val b = android.graphics.Color.blue(pixel)
                                val brightness = (r + g + b) / 3
                                if (brightness in 25..230) {
                                    rAcc += r
                                    gAcc += g
                                    bAcc += b
                                    count++
                                }
                            }
                        }
                        if (count > 0) {
                            val avgR = (rAcc / count).toInt()
                            val avgG = (gAcc / count).toInt()
                            val avgB = (bAcc / count).toInt()
                            extractedColor = Color(avgR, avgG, avgB)
                        }
                    }
                }
            } catch (_: Exception) {
                extractedColor = Color.Transparent
            }
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = if (enabled) extractedColor else Color.Transparent,
        animationSpec = tween(durationMillis = 800),
        label = "dynamic_artwork_color"
    )

    return animatedColor
}
