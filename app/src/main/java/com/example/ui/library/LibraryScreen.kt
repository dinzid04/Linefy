package com.example.ui.library

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.R
import com.example.data.model.ActiveDownloadProgress
import com.example.data.model.DownloadedTrackItem
import com.example.data.model.ListeningStatsState
import com.example.data.model.MusicItem
import com.example.data.model.PlaylistPrivacy
import com.example.data.model.SpotifyImportProgressState
import com.example.data.model.SubscribedArtistItem
import com.example.data.model.UserLibraryPlaylist
import com.example.data.remote.CuratedHomeFallback
import com.example.data.remote.PlaylistIntegrationService
import com.example.data.settings.AppSettings
import com.example.data.settings.LibraryFolderItem
import com.example.data.settings.LibrarySortOrder
import com.example.data.settings.LibraryViewMode
import com.example.ui.components.floatingtabbar.FloatingTabBarScrollConnection
import com.example.ui.components.lyraGlassCard
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.rememberDominantAccentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class LibraryPrimaryTab(val label: String) {
    ALL("All"),
    LOCAL("Local"),
    CACHED("Cached"),
    IMPORTED("Imported")
}

private enum class LibrarySmartFilter(val label: String) {
    ALL("Semua"),
    PLAYLISTS("Playlist"),
    FOLDERS("Folder"),
    FAVORITES("Favorit"),
    STATISTICS("Statistik"),
    MOST_PLAYED("Sering Diputar")
}

@Immutable
private data class LibraryArtistCardModel(
    val name: String,
    val subtitle: String,
    val avatarUrl: String,
    val isSubscribed: Boolean = false
)

private val DefaultLibraryArtists = listOf(
    LibraryArtistCardModel(
        name = "Rossa",
        subtitle = "Artis",
        avatarUrl = CuratedHomeFallback.getArtistAvatarForName("Rossa")
    ),
    LibraryArtistCardModel(
        name = "Tulus",
        subtitle = "Artis",
        avatarUrl = CuratedHomeFallback.getArtistAvatarForName("Tulus")
    ),
    LibraryArtistCardModel(
        name = "Mahalini",
        subtitle = "Artis",
        avatarUrl = CuratedHomeFallback.getArtistAvatarForName("Mahalini")
    ),
    LibraryArtistCardModel(
        name = "Hindia",
        subtitle = "Artis",
        avatarUrl = CuratedHomeFallback.getArtistAvatarForName("Hindia")
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    listeningStats: ListeningStatsState,
    favoriteTracks: List<MusicItem>,
    libraryPlaylists: List<UserLibraryPlaylist>,
    downloadedTracks: List<DownloadedTrackItem>,
    localDeviceTracks: List<MusicItem>,
    activeDownloads: Map<String, ActiveDownloadProgress>,
    candidateTracksForAdd: List<MusicItem>,
    scrollConnection: FloatingTabBarScrollConnection? = null,
    subscribedArtists: List<SubscribedArtistItem> = emptyList(),
    recentTracks: List<MusicItem> = emptyList(),
    artistAvatarMap: Map<String, String> = emptyMap(),
    hasAudioPermission: Boolean = true,
    isYtMusicSignedIn: Boolean = false,
    ytMusicUserName: String? = null,
    ytMusicAvatarUrl: String? = null,
    activeSongArtworkUrl: String? = null,
    importProgress: SpotifyImportProgressState = SpotifyImportProgressState(),
    importSourceLabel: String = "Spotify",
    onPreviewPlaylistUrl: suspend (String) -> PlaylistIntegrationService.PlaylistUrlPreview? = { null },
    onImportPlaylistUrl: (String) -> Unit = {},
    onImportPlaylistFile: (String, String) -> Unit = { _, _ -> },
    onAudioPermissionResult: (Boolean) -> Unit = {},
    onResolveArtistAvatar: (String, String?) -> Unit = { _, _ -> },
    onUpdateUsername: (String) -> Unit = {},
    onOpenLikedSongs: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenLocalMusic: () -> Unit,
    onOpenPlaylist: (UserLibraryPlaylist) -> Unit,
    onCreatePlaylist: (title: String, privacy: PlaylistPrivacy) -> Unit,
    onRenamePlaylist: (playlistId: String, newTitle: String) -> Unit,
    onDeletePlaylist: (playlistId: String) -> Unit,
    onTogglePinPlaylist: (playlistId: String) -> Unit,
    onAddTrackToPlaylist: (playlistId: String, track: MusicItem) -> Unit,
    onDownloadPlaylist: (UserLibraryPlaylist) -> Unit,
    onOpenImportIntegrations: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenArtistByName: (String) -> Unit,
    onPlayTopTrack: (MusicItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val useDynamicMusicTheme by AppSettings.useDynamicMusicTheme.collectAsStateWithLifecycle()
    val fullLiquidGlassUi by AppSettings.fullLiquidGlassUi.collectAsStateWithLifecycle()
    val pureBlackDarkMode by AppSettings.pureBlackDarkMode.collectAsStateWithLifecycle()
    val libraryViewMode by AppSettings.libraryViewMode.collectAsStateWithLifecycle()
    val librarySortOrder by AppSettings.librarySortOrder.collectAsStateWithLifecycle()
    val libraryFolders by AppSettings.libraryFolders.collectAsStateWithLifecycle()

    val dominantLibColor = rememberDominantAccentColor(
        imageUrl = if (useDynamicMusicTheme) activeSongArtworkUrl else null,
        defaultColor = Color.Black,
    )
    val animatedLibAmbient by animateColorAsState(
        targetValue = if (useDynamicMusicTheme && !activeSongArtworkUrl.isNullOrBlank() && !pureBlackDarkMode) {
            dominantLibColor.copy(alpha = 0.34f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 600),
        label = "library_dynamic_top",
    )

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedPrimaryTab by rememberSaveable { mutableStateOf(LibraryPrimaryTab.ALL) }
    var selectedSmartFilter by rememberSaveable { mutableStateOf(LibrarySmartFilter.ALL) }
    var showSortDropdown by remember { mutableStateOf(false) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showUniversalImportDialog by remember { mutableStateOf(false) }
    var activeFolderForSheet by remember { mutableStateOf<LibraryFolderItem?>(null) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    var showAllPlaylistsSheet by remember { mutableStateOf(false) }
    var showLocalMusicPermissionDialog by remember { mutableStateOf(false) }
    var playlistForMenu by remember { mutableStateOf<UserLibraryPlaylist?>(null) }
    var playlistForRename by remember { mutableStateOf<UserLibraryPlaylist?>(null) }
    var playlistForAddSongs by remember { mutableStateOf<UserLibraryPlaylist?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUsername = if (isYtMusicSignedIn && !ytMusicUserName.isNullOrBlank()) {
        ytMusicUserName
    } else {
        listeningStats.userHandle.ifBlank { "user" }
    }

    val m3uFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val pair = withContext(Dispatchers.IO) {
                    runCatching {
                        var displayName = "Imported Playlist"
                        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                displayName = cursor.getString(0) ?: displayName
                            }
                        }
                        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
                        displayName to text
                    }.getOrNull()
                }
                if (pair != null && pair.second.isNotBlank()) {
                    onImportPlaylistFile(pair.first, pair.second)
                    showUniversalImportDialog = false
                }
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onAudioPermissionResult(isGranted)
    }

    if (showLocalMusicPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showLocalMusicPermissionDialog = false },
            containerColor = Color(0xFF18181F),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFB8B8C4),
            title = {
                Text(
                    text = "Konfirmasi Akses Penyimpanan",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Izinkan aplikasi mengakses penyimpanan musik di HP Anda untuk memindai dan menampilkan semua lagu lokal secara otomatis di Local Music?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFFB8B8C4)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLocalMusicPermissionDialog = false
                        val perm = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            android.Manifest.permission.READ_MEDIA_AUDIO
                        } else {
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        audioPermissionLauncher.launch(perm)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D6D)),
                    modifier = Modifier.testTag("confirm_local_music_permission_button")
                ) {
                    Text(
                        text = "Konfirmasi & Izinkan",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLocalMusicPermissionDialog = false }
                ) {
                    Text(
                        text = "Batal",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9E9EA8)
                    )
                }
            }
        )
    }

    val libraryArtists = remember(subscribedArtists, recentTracks, favoriteTracks, libraryPlaylists, downloadedTracks, artistAvatarMap) {
        val subscribedModels = subscribedArtists.map { sub ->
            val key = sub.name.trim().lowercase()
            val cleanSubAvatar = sub.avatarUrl.trim().takeIf {
                it.isNotBlank() && !it.contains("FRX282637095.webp")
            }
            val resolvedAvatar = artistAvatarMap[key]?.takeIf { it.isNotBlank() }
                ?: cleanSubAvatar
                ?: CuratedHomeFallback.getArtistAvatarForName(sub.name)
            LibraryArtistCardModel(
                name = sub.name,
                subtitle = "Disubscribe",
                avatarUrl = resolvedAvatar,
                isSubscribed = true
            )
        }

        val subscribedNamesLower = subscribedModels.map { it.name.trim().lowercase() }.toSet()

        val userLibraryTracks = buildList {
            addAll(recentTracks)
            addAll(favoriteTracks)
            libraryPlaylists.forEach { addAll(it.tracks) }
            downloadedTracks.forEach { add(it.toMusicItem()) }
        }

        val trackArtistModels = userLibraryTracks
            .mapNotNull { track ->
                val primaryArtist = track.cleanArtist
                    .substringBefore(",")
                    .substringBefore("&")
                    .trim()
                if (primaryArtist.isBlank() ||
                    primaryArtist.equals("Artis", ignoreCase = true) ||
                    primaryArtist.equals("user", ignoreCase = true) ||
                    primaryArtist.equals("Spotify", ignoreCase = true) ||
                    primaryArtist.equals("YouTube Music", ignoreCase = true) ||
                    primaryArtist.lowercase() in subscribedNamesLower
                ) {
                    null
                } else {
                    primaryArtist to track
                }
            }
            .groupBy { it.first.lowercase() }
            .map { (key, pairs) ->
                val displayName = pairs.first().first
                val distinctTracks = pairs.map { it.second }.distinctBy { it.videoId ?: it.id }
                val count = distinctTracks.size.coerceAtLeast(1)
                val ownTrackCover = distinctTracks.firstOrNull {
                    it.highResThumbnail.isNotBlank() && !it.highResThumbnail.contains("FRX282637095.webp")
                }?.highResThumbnail.orEmpty()
                val resolvedAvatar = artistAvatarMap[key]?.takeIf { it.isNotBlank() }
                    ?: ownTrackCover.ifBlank { CuratedHomeFallback.getArtistAvatarForName(displayName) }

                LibraryArtistCardModel(
                    name = displayName,
                    subtitle = "$count lagu",
                    avatarUrl = resolvedAvatar,
                    isSubscribed = false
                )
            }

        val combinedUser = subscribedModels + trackArtistModels
        if (combinedUser.isNotEmpty()) {
            combinedUser
        } else {
            DefaultLibraryArtists.map { def ->
                val key = def.name.trim().lowercase()
                def.copy(avatarUrl = artistAvatarMap[key]?.takeIf { it.isNotBlank() } ?: def.avatarUrl)
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(libraryArtists) {
        libraryArtists.forEach { artist ->
            val key = artist.name.trim().lowercase()
            val cached = artistAvatarMap[key]
            if (cached.isNullOrBlank() || (!cached.contains("googleusercontent.com") && !cached.contains("ggpht.com"))) {
                onResolveArtistAvatar(artist.name, null)
            }
        }
    }

    // Filtered & Sorted Playlists
    val filteredSortedPlaylists = remember(
        libraryPlaylists,
        selectedPrimaryTab,
        searchQuery,
        librarySortOrder
    ) {
        val base = libraryPlaylists.filter { !it.isAlbum }.filter { playlist ->
            val matchesTab = when (selectedPrimaryTab) {
                LibraryPrimaryTab.ALL -> true
                LibraryPrimaryTab.LOCAL -> playlist.platformBadge.isBlank() || playlist.platformBadge.equals("Local", ignoreCase = true)
                LibraryPrimaryTab.CACHED -> playlist.tracks.isNotEmpty()
                LibraryPrimaryTab.IMPORTED -> playlist.platformBadge.isNotBlank() ||
                    playlist.id.startsWith("imported_") ||
                    playlist.subtitle.contains("Spotify", ignoreCase = true) ||
                    playlist.subtitle.contains("Apple", ignoreCase = true) ||
                    playlist.subtitle.contains("Deezer", ignoreCase = true) ||
                    playlist.subtitle.contains("YouTube", ignoreCase = true)
            }
            val matchesQuery = searchQuery.isBlank() ||
                playlist.title.contains(searchQuery, ignoreCase = true) ||
                playlist.subtitle.contains(searchQuery, ignoreCase = true) ||
                playlist.tracks.any {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                        it.cleanArtist.contains(searchQuery, ignoreCase = true)
                }
            matchesTab && matchesQuery
        }

        val sorted = when (librarySortOrder) {
            LibrarySortOrder.NAME_ASC -> base.sortedBy { it.title.lowercase() }
            LibrarySortOrder.NAME_DESC -> base.sortedByDescending { it.title.lowercase() }
            LibrarySortOrder.DATE_ADDED -> base.sortedByDescending { it.createdAt }
            LibrarySortOrder.SONG_COUNT -> base.sortedByDescending { it.tracks.size }
            LibrarySortOrder.MOST_PLAYED -> base.sortedByDescending { it.tracks.size }
        }
        sorted.sortedByDescending { it.isPinned }
    }

    val filteredArtists = remember(libraryArtists, searchQuery) {
        if (searchQuery.isBlank()) {
            libraryArtists
        } else {
            libraryArtists.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    val totalUniqueSongsCount = remember(favoriteTracks, downloadedTracks, localDeviceTracks, recentTracks, libraryPlaylists) {
        buildList {
            addAll(favoriteTracks)
            addAll(localDeviceTracks)
            addAll(recentTracks)
            downloadedTracks.forEach { add(it.toMusicItem()) }
            libraryPlaylists.forEach { addAll(it.tracks) }
        }.distinctBy { it.videoId ?: it.id }.size
    }

    val allCoverThumbnails = remember(favoriteTracks, recentTracks, libraryPlaylists) {
        buildList {
            favoriteTracks.forEach { if (it.highResThumbnail.isNotBlank()) add(it.highResThumbnail) }
            recentTracks.forEach { if (it.highResThumbnail.isNotBlank()) add(it.highResThumbnail) }
            libraryPlaylists.forEach {
                if (it.thumbnailUrl.isNotBlank()) add(it.thumbnailUrl)
                it.tracks.firstOrNull()?.highResThumbnail?.takeIf { u -> u.isNotBlank() }?.let(::add)
            }
        }.distinct().take(6)
    }

    if (showEditUsernameDialog) {
        EditUsernameDialog(
            currentUsername = currentUsername,
            onDismiss = { showEditUsernameDialog = false },
            onSave = { updated ->
                onUpdateUsername(updated)
                showEditUsernameDialog = false
            }
        )
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, privacy ->
                onCreatePlaylist(title, privacy)
            }
        )
    }

    if (showCreateFolderDialog) {
        CreateLibraryFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreateFolder = { folderName ->
                AppSettings.createLibraryFolder(folderName)
                showCreateFolderDialog = false
            }
        )
    }

    if (showUniversalImportDialog) {
        UniversalPlaylistImportDialog(
            onDismiss = { showUniversalImportDialog = false },
            onPreviewUrl = onPreviewPlaylistUrl,
            onImportUrl = { url ->
                onImportPlaylistUrl(url)
                showUniversalImportDialog = false
            },
            onPickLocalFile = {
                m3uFilePickerLauncher.launch(arrayOf("*/*"))
            },
            onOpenCloudIntegrations = {
                showUniversalImportDialog = false
                onOpenImportIntegrations()
            }
        )
    }

    activeFolderForSheet?.let { currentFolder ->
        val latestFolder = libraryFolders.find { it.id == currentFolder.id } ?: currentFolder
        LibraryFolderDetailBottomSheet(
            folder = latestFolder,
            allPlaylists = libraryPlaylists.filter { !it.isAlbum },
            onDismiss = { activeFolderForSheet = null },
            onOpenPlaylist = { pl ->
                activeFolderForSheet = null
                onOpenPlaylist(pl)
            },
            onTogglePlaylistInFolder = { playlistId ->
                AppSettings.togglePlaylistInFolder(latestFolder.id, playlistId)
            },
            onDeleteFolder = {
                AppSettings.deleteLibraryFolder(latestFolder.id)
                activeFolderForSheet = null
            }
        )
    }

    playlistForRename?.let { target ->
        RenamePlaylistDialog(
            currentTitle = target.title,
            onDismiss = { playlistForRename = null },
            onRename = { newTitle ->
                onRenamePlaylist(target.id, newTitle)
                playlistForRename = null
            }
        )
    }

    playlistForMenu?.let { target ->
        PlaylistManagementBottomSheet(
            playlist = target,
            onDismiss = { playlistForMenu = null },
            onTogglePin = {
                onTogglePinPlaylist(target.id)
                playlistForMenu = null
            },
            onRename = {
                playlistForMenu = null
                playlistForRename = target
            },
            onAddSongs = {
                playlistForMenu = null
                playlistForAddSongs = target
            },
            onDownloadPlaylist = {
                onDownloadPlaylist(target)
                playlistForMenu = null
            },
            onDelete = {
                onDeletePlaylist(target.id)
                playlistForMenu = null
            }
        )
    }

    playlistForAddSongs?.let { target ->
        AddSongsToSpecificPlaylistSheet(
            playlist = target,
            candidateTracks = candidateTracksForAdd,
            onDismiss = { playlistForAddSongs = null },
            onAddTrack = { track ->
                onAddTrackToPlaylist(target.id, track)
            }
        )
    }

    if (showAllPlaylistsSheet) {
        AllLibraryPlaylistsBottomSheet(
            username = currentUsername,
            favoriteTracksCount = favoriteTracks.size,
            libraryPlaylists = libraryPlaylists,
            onDismiss = { showAllPlaylistsSheet = false },
            onCreateNew = {
                showAllPlaylistsSheet = false
                showCreateDialog = true
            },
            onImportSpotify = {
                showAllPlaylistsSheet = false
                showUniversalImportDialog = true
            },
            onOpenLikedSongs = {
                showAllPlaylistsSheet = false
                onOpenLikedSongs()
            },
            onOpenPlaylist = { playlist ->
                showAllPlaylistsSheet = false
                onOpenPlaylist(playlist)
            },
            onOpenPlaylistMenu = { playlist ->
                showAllPlaylistsSheet = false
                playlistForMenu = playlist
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .then(if (scrollConnection != null) Modifier.nestedScroll(scrollConnection) else Modifier)
            .background(Color(0xFF000000))
            .background(
                Brush.verticalGradient(
                    colors = listOf(animatedLibAmbient, Color(0xFF000000), Color(0xFF000000)),
                ),
            )
            .testTag("library_screen_list"),
        contentPadding = PaddingValues(bottom = 190.dp)
    ) {
        // 1. HEADER + SEARCH BAR + PRIMARY TABS (All / Local / Cached / Imported)
        item(key = "library_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 12.dp, bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Library",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp,
                        color = Color(0xFFEAEAEF),
                        letterSpacing = (-1.0).sp
                    )

                    Row(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF0C0C0F))
                            .border(1.dp, Color(0xFF232329), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("library_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = "Riwayat & Pengaturan",
                                tint = Color(0xFFDCDCE2),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onOpenImportIntegrations)
                                .testTag("library_avatar_button")
                        ) {
                            if (isYtMusicSignedIn && !ytMusicAvatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(ytMusicAvatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profil & Integrasi",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_cat_avatar),
                                    contentDescription = "Profil & Integrasi",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search bar matching IMG-20261027-WA0007.jpg ("Search anything on your library")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF161618))
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Cari",
                            tint = Color(0xFF8E8E95),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search anything on your library",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = Color(0xFF7D7D85)
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                ),
                                cursorBrush = SolidColor(Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("library_search_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Hapus Pencarian",
                                    tint = Color(0xFFB0B0B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Filter Tabs: All / Local / Cached / Imported (pill track matching IMG-20261027-WA0007.jpg)
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF141416))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LibraryPrimaryTab.entries.forEach { tab ->
                        val isSelected = selectedPrimaryTab == tab
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) Color(0xFF3D3D41) else Color(0xFF202024),
                            animationSpec = tween(200),
                            label = "lib_primary_tab_bg"
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(chipBg)
                                .clickable { selectedPrimaryTab = tab }
                                .padding(horizontal = 16.dp, vertical = 7.dp)
                                .testTag("library_tab_${tab.name.lowercase()}")
                        ) {
                            Text(
                                text = tab.label,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.72f)
                            )
                        }
                    }
                }
            }
        }

        // 1B. ACTIVE IMPORT PROGRESS BANNER OR QUICK ACTION BAR (matching IMG-20261027-WA0007.jpg)
        item(key = "library_import_progress_or_actions") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                if (importProgress.isImporting) {
                    val total = importProgress.totalTracks.coerceAtLeast(1)
                    val done = importProgress.processedTracks.coerceIn(0, total)
                    val fraction = (done.toFloat() / total.toFloat()).coerceIn(0.03f, 1f)
                    val badgeShort = when {
                        importSourceLabel.contains("Spotify", ignoreCase = true) -> "sptfy"
                        importSourceLabel.contains("Apple", ignoreCase = true) -> "apple"
                        importSourceLabel.contains("Deezer", ignoreCase = true) -> "deezer"
                        importSourceLabel.contains("YouTube", ignoreCase = true) -> "ytm"
                        else -> importSourceLabel.lowercase()
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF141416))
                            .clickable(onClick = onOpenImportIntegrations)
                            .padding(horizontal = 22.dp, vertical = 22.dp)
                            .testTag("library_active_import_banner")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.14f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Adding $done of ${importProgress.totalTracks} tracks from",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = Color.White,
                                letterSpacing = (-0.4).sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = badgeShort,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Smart Category Chips + Create Folder + Import Playlist + Sort & View Mode Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // + Buat Folder Action Pill
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF161618))
                            .border(1.dp, Color(0xFF25252B), RoundedCornerShape(14.dp))
                            .clickable { showCreateFolderDialog = true }
                            .padding(horizontal = 12.dp)
                            .testTag("library_create_folder_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CreateNewFolder,
                            contentDescription = "Buat Folder",
                            tint = Color(0xFFE6E6EA),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buat Folder",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    // Impor Playlist (URL / M3U) Action Pill
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF161618))
                            .border(1.dp, Color(0xFF25252B), RoundedCornerShape(14.dp))
                            .clickable { showUniversalImportDialog = true }
                            .padding(horizontal = 12.dp)
                            .testTag("library_universal_import_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = "Impor Playlist",
                            tint = Color(0xFF38EF7D),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Impor Playlist",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 1C. BENTO SUMMARY CARDS (Songs, Playlist, Albums, Artist, Recently added, Liked) matching IMG-20261027-WA0007.jpg
        item(key = "library_bento_summary_grid") {
            val nonAlbumPlaylists = libraryPlaylists.filter { !it.isAlbum }
            val albumPlaylists = libraryPlaylists.filter { it.isAlbum }
            val playlistCover = nonAlbumPlaylists.firstOrNull()?.let {
                it.thumbnailUrl.ifBlank { it.tracks.firstOrNull()?.highResThumbnail.orEmpty() }
            }.orEmpty()
            val albumCover = albumPlaylists.firstOrNull()?.let {
                it.thumbnailUrl.ifBlank { it.tracks.firstOrNull()?.highResThumbnail.orEmpty() }
            }.orEmpty()
            val artistCover = filteredArtists.firstOrNull()?.avatarUrl.orEmpty()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LibraryBentoCard(
                        title = "Songs",
                        count = totalUniqueSongsCount,
                        coverUrl = allCoverThumbnails.firstOrNull(),
                        onClick = onOpenLikedSongs,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("library_bento_songs")
                    )
                    LibraryBentoCard(
                        title = "Playlist",
                        count = nonAlbumPlaylists.size,
                        coverUrl = playlistCover,
                        onClick = { showAllPlaylistsSheet = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("library_bento_playlists")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LibraryBentoCard(
                        title = "Albums",
                        count = albumPlaylists.size,
                        coverUrl = albumCover,
                        onClick = {
                            albumPlaylists.firstOrNull()?.let(onOpenPlaylist) ?: run { showAllPlaylistsSheet = true }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("library_bento_albums")
                    )
                    LibraryBentoCard(
                        title = "Artist",
                        count = filteredArtists.size,
                        coverUrl = artistCover,
                        isRoundCover = true,
                        onClick = {
                            filteredArtists.firstOrNull()?.let { onOpenArtistByName(it.name) }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("library_bento_artists")
                    )
                }

                // Wide Card: Recently added
                LibraryWideBentoCard(
                    title = "Recently added",
                    count = totalUniqueSongsCount,
                    coverUrls = allCoverThumbnails.take(2),
                    isLikedCard = false,
                    onClick = {
                        if (downloadedTracks.isNotEmpty()) onOpenDownloads() else onOpenLikedSongs()
                    },
                    modifier = Modifier.testTag("library_bento_recently_added")
                )

                // Wide Card: Liked
                LibraryWideBentoCard(
                    title = "Liked",
                    count = favoriteTracks.size,
                    coverUrls = favoriteTracks.mapNotNull { it.highResThumbnail.takeIf { u -> u.isNotBlank() } }.take(2),
                    isLikedCard = true,
                    onClick = onOpenLikedSongs,
                    modifier = Modifier.testTag("library_bento_liked")
                )
            }
        }

        // 1D. SMART FILTER CHIPS + SORT & VIEW MODE BAR
        item(key = "library_smart_filter_and_sort_bar") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 10.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(LibrarySmartFilter.entries, key = { it.name }) { filter ->
                        val selected = selectedSmartFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) Color.White else Color(0xFF18181C))
                                .clickable { selectedSmartFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("library_smart_chip_${filter.name.lowercase()}")
                        ) {
                            Text(
                                text = filter.label,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (selected) Color.Black else Color(0xFFDCDCE2)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showSortDropdown = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("library_sort_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Sort,
                                contentDescription = "Urutkan",
                                tint = Color(0xFFDCDCE2),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = librarySortOrder.label,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFDCDCE2)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortDropdown,
                            onDismissRequest = { showSortDropdown = false },
                            modifier = Modifier.background(Color(0xFF1C1C22))
                        ) {
                            LibrarySortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = order.label,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = if (librarySortOrder == order) FontWeight.ExtraBold else FontWeight.Medium,
                                                color = Color.White
                                            )
                                            if (librarySortOrder == order) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = Color(0xFF38EF7D),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        AppSettings.setLibrarySortOrder(order)
                                        showSortDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            val nextMode = if (libraryViewMode == LibraryViewMode.GRID) {
                                LibraryViewMode.LIST
                            } else {
                                LibraryViewMode.GRID
                            }
                            AppSettings.setLibraryViewMode(nextMode)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF161618))
                            .testTag("library_view_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (libraryViewMode == LibraryViewMode.GRID) {
                                Icons.AutoMirrored.Rounded.List
                            } else {
                                Icons.Rounded.GridView
                            },
                            contentDescription = "Ubah Tampilan",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 1E. CUSTOM FOLDERS SECTION (Shown when folders exist or Folders filter is active)
        if (libraryFolders.isNotEmpty() || selectedSmartFilter == LibrarySmartFilter.FOLDERS) {
            item(key = "library_folders_section") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Folder Playlist (${libraryFolders.size})",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "+ Folder Baru",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF38EF7D),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showCreateFolderDialog = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    if (libraryFolders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF141416))
                                .clickable { showCreateFolderDialog = true }
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada folder. Ketuk untuk membuat folder playlist pertama Anda.",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color(0xFF8E8E95),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        libraryFolders.forEach { folder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF141416))
                                    .clickable { activeFolderForSheet = folder }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF232329)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Folder,
                                        contentDescription = folder.name,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = folder.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.5.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${folder.playlistIds.size} playlist",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF8E8E95)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF787880)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. PENGALAMAN MENDENGARKAN ANDA (Horizontal Carousel of 2 Large Metallic Charcoal Cards)
        if (selectedSmartFilter == LibrarySmartFilter.ALL ||
            selectedSmartFilter == LibrarySmartFilter.STATISTICS ||
            selectedSmartFilter == LibrarySmartFilter.MOST_PLAYED
        ) {
            item(key = "library_listening_experience") {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "stats_card_minutes") {
                        ListeningExperienceCard(
                            mainValue = "${listeningStats.minutesListened}",
                            mainValueFontSize = 32.sp,
                            metricLabel = "MINUTES LISTENED",
                            userHandle = currentUsername,
                            canEditUsername = !isYtMusicSignedIn,
                            fullLiquidGlassUi = fullLiquidGlassUi,
                            bottomSubtitle = "${listeningStats.totalPlays} Plays • 2026",
                            memberSinceLabel = "ANGGOTA SEJAK",
                            memberSinceValue = listeningStats.memberSince,
                            onClick = onOpenLikedSongs,
                            onUsernameClick = {
                                if (!isYtMusicSignedIn) {
                                    showEditUsernameDialog = true
                                }
                            }
                        )
                    }

                    item(key = "stats_card_top_song") {
                        val topSongTitle = listeningStats.topTrack?.title ?: listeningStats.topSongTitle
                        val topSongArtist = listeningStats.topTrack?.cleanArtist ?: listeningStats.topSongArtist
                        ListeningExperienceCard(
                            mainValue = topSongTitle,
                            mainValueFontSize = 26.sp,
                            metricLabel = "TOP SONG",
                            userHandle = currentUsername,
                            canEditUsername = !isYtMusicSignedIn,
                            fullLiquidGlassUi = fullLiquidGlassUi,
                            bottomSubtitle = "$topSongArtist • ${listeningStats.topSongPlays} Plays",
                            memberSinceLabel = null,
                            memberSinceValue = null,
                            onClick = {
                                listeningStats.topTrack?.let(onPlayTopTrack) ?: onOpenLikedSongs()
                            },
                            onUsernameClick = {
                                if (!isYtMusicSignedIn) {
                                    showEditUsernameDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }

        // 3. DOWNLOADS & LOCAL MUSIC FULL-WIDTH ROWS
        if (selectedPrimaryTab == LibraryPrimaryTab.ALL ||
            selectedPrimaryTab == LibraryPrimaryTab.LOCAL ||
            selectedPrimaryTab == LibraryPrimaryTab.CACHED
        ) {
            item(key = "library_quick_rows") {
                val activeCount = activeDownloads.size
                val firstActive = activeDownloads.values.firstOrNull()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 18.dp)
                ) {
                    // Downloads Row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenDownloads)
                            .padding(horizontal = 16.dp, vertical = 15.dp)
                            .testTag("library_downloads_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = "Downloads",
                                tint = Color(0xFFDCDCE2),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Downloads",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFFE6E6EA)
                                )
                                if (activeCount > 0 || downloadedTracks.isNotEmpty()) {
                                    Text(
                                        text = if (activeCount > 0) {
                                            "Mengunduh ${firstActive?.progressPercent ?: 0}% • ${downloadedTracks.size} lagu"
                                        } else {
                                            "${downloadedTracks.size} lagu tersimpan"
                                        },
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = if (activeCount > 0) Color(0xFF38EF7D) else Color(0xFF8E8E95)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF787880),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        if (firstActive != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (firstActive.progressPercent / 100f).coerceIn(0f, 1f) },
                                color = Color(0xFF38EF7D),
                                trackColor = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 44.dp)
                                    .height(3.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFF16161A),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(start = 56.dp, end = 16.dp)
                    )

                    // Local Music Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (hasAudioPermission) {
                                    onOpenLocalMusic()
                                } else {
                                    showLocalMusicPermissionDialog = true
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 15.dp)
                            .testTag("library_local_music_card"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Folder,
                            contentDescription = "Local Music",
                            tint = Color(0xFFDCDCE2),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Local Music",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFE6E6EA)
                            )
                            Text(
                                text = if (!hasAudioPermission) {
                                    "Ketuk untuk konfirmasi izin penyimpanan HP"
                                } else if (localDeviceTracks.isNotEmpty()) {
                                    "${localDeviceTracks.size} lagu di penyimpanan HP"
                                } else {
                                    "Pindai musik dari penyimpanan HP"
                                },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (!hasAudioPermission) Color(0xFFFFB74D) else Color(0xFF8E8E95)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF787880),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 4. SECTION: PLAYLIST ("Playlist" + "Lihat semua" + Grid / List Modes)
        item(key = "library_playlist_section") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 26.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Playlist",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Lihat semua",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFE6E6EA),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAllPlaylistsSheet = true }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                            .testTag("library_playlist_see_all")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (libraryViewMode == LibraryViewMode.LIST) {
                    // LIST VIEW MODE FOR PLAYLISTS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filteredSortedPlaylists.forEach { playlist ->
                            val customCover = com.example.data.remote.RemoteArtworkStore.getPlaylistCoverOverride(playlist.id)
                                ?: com.example.data.remote.RemoteArtworkStore.getPlaylistCoverOverride(playlist.id.removePrefix("VL"))
                            val rawCover = customCover?.takeIf { it.isNotBlank() }
                                ?: playlist.thumbnailUrl.ifBlank {
                                    playlist.tracks.firstOrNull()?.highResThumbnail
                                        ?: "https://i.ytimg.com/vi/7n27x2A78No/hqdefault.jpg"
                                }
                            val coverUrl = com.example.data.model.MusicItem.upgradeThumbnailUrl(rawCover, null, com.example.data.model.MusicItem.Quality.HIGH)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF141416))
                                    .combinedClickable(
                                        onClick = { onOpenPlaylist(playlist) },
                                        onLongClick = { playlistForMenu = playlist }
                                    )
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = remember(coverUrl) {
                                        ImageRequest.Builder(context)
                                            .data(coverUrl)
                                            .size(Size(180, 180))
                                            .crossfade(false)
                                            .build()
                                    },
                                    contentDescription = playlist.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1E1E24))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.title,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${playlist.tracks.size} lagu • ${playlist.platformBadge.ifBlank { currentUsername }}",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF8E8E95),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { playlistForMenu = playlist }) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = "Opsi Playlist",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // GRID / CAROUSEL VIEW MODE FOR PLAYLISTS
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: Playlist baru (+)
                        item(key = "card_create_playlist") {
                            Column(
                                modifier = Modifier
                                    .width(148.dp)
                                    .clickable { showCreateDialog = true }
                                    .testTag("library_create_playlist_button")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(148.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF161618)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Playlist baru",
                                        tint = Color(0xFFE6E6EA),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Playlist baru",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Disimpan ke Library...",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF8E8E95),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Card 2: Impor Playlist (Spotify, YTM, Apple Music, Deezer, M3U)
                        item(key = "card_import_spotify") {
                            Column(
                                modifier = Modifier
                                    .width(148.dp)
                                    .clickable { showUniversalImportDialog = true }
                                    .testTag("library_import_cloud_button")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(148.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF161618)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SpotifyCircleLogo(
                                        modifier = Modifier.size(46.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Impor Playlist",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Spotify, YTM, Apple, M3U",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF8E8E95),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Card 3: Musik yang Disukai
                        item(key = "card_liked_music") {
                            Column(
                                modifier = Modifier
                                    .width(148.dp)
                                    .clickable(onClick = onOpenLikedSongs)
                                    .testTag("library_liked_songs_row")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(148.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF7B61F8),
                                                    Color(0xFFA961D8),
                                                    Color(0xFFE063B3)
                                                ),
                                                start = Offset(0f, 0f),
                                                end = Offset(420f, 420f)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ThumbUp,
                                        contentDescription = "Musik yang Disukai",
                                        tint = Color(0xFFF2F2F7),
                                        modifier = Modifier.size(68.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Musik yang Disukai",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Playlist otomatis",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF8E8E95),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Card 4+: Filtered & Sorted User Playlists
                        items(
                            items = filteredSortedPlaylists,
                            key = { "lib_playlist_${it.id}" }
                        ) { playlist ->
                            val customCover = com.example.data.remote.RemoteArtworkStore.getPlaylistCoverOverride(playlist.id)
                                ?: com.example.data.remote.RemoteArtworkStore.getPlaylistCoverOverride(playlist.id.removePrefix("VL"))
                            val rawCover = customCover?.takeIf { it.isNotBlank() }
                                ?: playlist.thumbnailUrl.ifBlank {
                                    playlist.tracks.firstOrNull()?.highResThumbnail
                                        ?: "https://i.ytimg.com/vi/7n27x2A78No/hqdefault.jpg"
                                }
                            val coverUrl = com.example.data.model.MusicItem.upgradeThumbnailUrl(rawCover, null, com.example.data.model.MusicItem.Quality.HIGH)
                            Column(
                                modifier = Modifier
                                    .width(148.dp)
                                    .combinedClickable(
                                        onClick = { onOpenPlaylist(playlist) },
                                        onLongClick = { playlistForMenu = playlist }
                                    )
                                    .testTag("library_playlist_item_${playlist.id}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(148.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF18181C))
                                ) {
                                    AsyncImage(
                                        model = remember(coverUrl) {
                                            ImageRequest.Builder(context)
                                                .data(coverUrl)
                                                .size(Size(320, 320))
                                                .crossfade(false)
                                                .build()
                                        },
                                        contentDescription = playlist.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.matchParentSize()
                                    )

                                    if (playlist.isPinned) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(8.dp)
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.65f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PushPin,
                                                contentDescription = "Disematkan",
                                                tint = Color(0xFF38EF7D),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { playlistForMenu = playlist },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.55f))
                                            .testTag("library_playlist_more_${playlist.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MoreVert,
                                            contentDescription = "Opsi Playlist",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = playlist.title,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val ownerLabel = when {
                                    playlist.tracks.isNotEmpty() -> "$currentUsername • ${playlist.tracks.size} lagu"
                                    playlist.subtitle.isNotBlank() && playlist.subtitle != "YouTube Music" -> playlist.subtitle
                                    playlist.platformBadge.isNotBlank() -> "$currentUsername • ${playlist.platformBadge}"
                                    else -> currentUsername
                                }
                                Text(
                                    text = ownerLabel,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF8E8E95),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4B. SECTION: ALBUM (Synced YouTube Music Albums & Saved Albums)
        val libraryAlbums = libraryPlaylists.filter { it.isAlbum }
        if (libraryAlbums.isNotEmpty()) {
            item(key = "library_albums_section") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 26.dp)
                ) {
                    Text(
                        text = "Album",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = libraryAlbums,
                            key = { "lib_album_${it.id}" }
                        ) { album ->
                            val customCover = com.example.data.remote.RemoteArtworkStore.getPlaylistCoverOverride(album.id)
                            val rawCover = customCover?.takeIf { it.isNotBlank() }
                                ?: album.thumbnailUrl.ifBlank {
                                    album.tracks.firstOrNull()?.highResThumbnail
                                        ?: "https://i.ytimg.com/vi/7n27x2A78No/hqdefault.jpg"
                                }
                            val coverUrl = com.example.data.model.MusicItem.upgradeThumbnailUrl(rawCover, null, com.example.data.model.MusicItem.Quality.HIGH)
                            Column(
                                modifier = Modifier
                                    .width(148.dp)
                                    .combinedClickable(
                                        onClick = { onOpenPlaylist(album) },
                                        onLongClick = { playlistForMenu = album }
                                    )
                                    .testTag("library_album_item_${album.id}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(148.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF18181C))
                                ) {
                                    AsyncImage(
                                        model = remember(coverUrl) {
                                            ImageRequest.Builder(context)
                                                .data(coverUrl)
                                                .size(Size(320, 320))
                                                .crossfade(false)
                                                .build()
                                        },
                                        contentDescription = album.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.matchParentSize()
                                    )

                                    IconButton(
                                        onClick = { playlistForMenu = album },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.55f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MoreVert,
                                            contentDescription = "Opsi Album",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = album.title,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = album.subtitle.ifBlank { "Album" },
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF8E8E95),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. SECTION: ARTIS — Circular (CircleShape) Artist Profile Avatars with Authentic Portraits
        item(key = "library_artists_section") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 26.dp)
            ) {
                Text(
                    text = "Artis",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = filteredArtists,
                        key = { "lib_artist_${it.name}" }
                    ) { artist ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(132.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenArtistByName(artist.name) }
                                .padding(vertical = 4.dp)
                                .testTag("library_artist_card_${artist.name}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(124.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF18181C)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = remember(artist.avatarUrl) {
                                        ImageRequest.Builder(context)
                                            .data(artist.avatarUrl)
                                            .size(Size(320, 320))
                                            .crossfade(false)
                                            .build()
                                    },
                                    contentDescription = artist.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = artist.name,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.5.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = artist.subtitle,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (artist.isSubscribed) Color(0xFF8AB4F8) else Color(0xFF8E8E95),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryBentoCard(
    title: String,
    count: Int,
    coverUrl: String?,
    isRoundCover: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(86.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF141416))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = Color(0xFF9E9EA8)
            )
        }

        if (!coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(44.dp)
                    .rotate(-8f)
                    .clip(if (isRoundCover) CircleShape else RoundedCornerShape(10.dp))
                    .background(Color(0xFF232329))
            )
        }
    }
}

@Composable
private fun LibraryWideBentoCard(
    title: String,
    count: Int,
    coverUrls: List<String>,
    isLikedCard: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(94.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF141416))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = Color(0xFF9E9EA8)
            )
        }

        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            verticalAlignment = Alignment.Bottom
        ) {
            coverUrls.firstOrNull()?.let { firstUrl ->
                AsyncImage(
                    model = firstUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .offset(x = if (isLikedCard) 10.dp else 0.dp)
                        .rotate(-10f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF242429))
                )
            }
            if (isLikedCard) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .rotate(6f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFB8A490), Color(0xFFE5584F))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FavoriteBorder,
                        contentDescription = "Liked",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateLibraryFolderDialog(
    onDismiss: () -> Unit,
    onCreateFolder: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF18181F),
        title = {
            Text(
                text = "Buat Folder Baru",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = "Kelompokkan playlist favorit Anda ke dalam satu folder rapi di Library.",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFFB0B0BA)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    singleLine = true,
                    placeholder = { Text("Nama folder (mis. Fokus Kerja, Santai)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38EF7D),
                        unfocusedBorderColor = Color(0xFF34343E)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (folderName.isNotBlank()) onCreateFolder(folderName.trim()) },
                enabled = folderName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38EF7D))
            ) {
                Text(
                    text = "Simpan Folder",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color(0xFF9E9EA8))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryFolderDetailBottomSheet(
    folder: LibraryFolderItem,
    allPlaylists: List<UserLibraryPlaylist>,
    onDismiss: () -> Unit,
    onOpenPlaylist: (UserLibraryPlaylist) -> Unit,
    onTogglePlaylistInFolder: (String) -> Unit,
    onDeleteFolder: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141418),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = folder.name,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${folder.playlistIds.size} playlist di dalam folder",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = Color(0xFF8E8E95)
                        )
                    }
                }
                IconButton(onClick = onDeleteFolder) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Hapus Folder",
                        tint = Color(0xFFFF5252)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pilih playlist untuk dimasukkan ke folder ini:",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = Color(0xFFB8B8C2)
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (allPlaylists.isEmpty()) {
                Text(
                    text = "Belum ada playlist di Library.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = Color(0xFF8E8E95),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allPlaylists, key = { it.id }) { pl ->
                        val isIncluded = pl.id in folder.playlistIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isIncluded) Color(0xFF222824) else Color(0xFF1C1C22))
                                .clickable { onTogglePlaylistInFolder(pl.id) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pl.title,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${pl.tracks.size} lagu",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E8E95)
                                )
                            }
                            if (isIncluded) {
                                TextButton(onClick = { onOpenPlaylist(pl) }) {
                                    Text("Buka", color = Color(0xFF38EF7D), fontWeight = FontWeight.Bold)
                                }
                            }
                            Icon(
                                imageVector = if (isIncluded) Icons.Rounded.Check else Icons.Rounded.Add,
                                contentDescription = null,
                                tint = if (isIncluded) Color(0xFF38EF7D) else Color(0xFF8E8E95)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UniversalPlaylistImportDialog(
    onDismiss: () -> Unit,
    onPreviewUrl: suspend (String) -> PlaylistIntegrationService.PlaylistUrlPreview? = { null },
    onImportUrl: (String) -> Unit,
    onPickLocalFile: () -> Unit,
    onOpenCloudIntegrations: () -> Unit
) {
    var playlistUrl by remember { mutableStateOf("") }
    var isLoadingPreview by remember { mutableStateOf(false) }
    var urlPreview by remember { mutableStateOf<PlaylistIntegrationService.PlaylistUrlPreview?>(null) }

    LaunchedEffect(playlistUrl) {
        val trimmed = playlistUrl.trim()
        if (trimmed.length < 12 || (!trimmed.startsWith("http") && !trimmed.startsWith("spotify:"))) {
            urlPreview = null
            isLoadingPreview = false
            return@LaunchedEffect
        }
        delay(350L)
        isLoadingPreview = true
        urlPreview = runCatching { onPreviewUrl(trimmed) }.getOrNull()
        isLoadingPreview = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF18181F),
        title = {
            Text(
                text = "Impor Playlist",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tempel tautan playlist atau album publik dari Spotify, YouTube Music, Apple Music, atau Deezer, atau impor file playlist lokal (.m3u / .m3u8 / .csv).",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFFB8B8C4)
                )

                OutlinedTextField(
                    value = playlistUrl,
                    onValueChange = { playlistUrl = it },
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "https://open.spotify.com/playlist/...",
                            fontSize = 13.sp,
                            color = Color(0xFF787882)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38EF7D),
                        unfocusedBorderColor = Color(0xFF32323C)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("universal_import_url_input")
                )

                if (isLoadingPreview) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF23232B))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF38EF7D),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Memuat pratinjau playlist...",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.5.sp,
                            color = Color(0xFFB8B8C4)
                        )
                    }
                } else {
                    urlPreview?.let { preview ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF23232B))
                                .border(1.dp, Color(0xFF38EF7D).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .testTag("universal_import_url_preview_card"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1DB954).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (preview.coverUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = preview.coverUrl,
                                        contentDescription = preview.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.MusicNote,
                                        contentDescription = null,
                                        tint = Color(0xFF38EF7D),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preview.title,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${preview.platformLabel} • ${preview.trackCount} lagu",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF38EF7D),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (preview.subtitle.isNotBlank()) {
                                    Text(
                                        text = preview.subtitle,
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF9E9EA8),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // File Picker Button (.m3u / .m3u8 / .csv)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF23232B))
                        .clickable { onPickLocalFile() }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("universal_import_file_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FileOpen,
                        contentDescription = "Pilih File .m3u / .csv",
                        tint = Color(0xFF8AB4F8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pilih File .M3U / .M3U8 / .CSV",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Impor daftar lagu dari penyimpanan perangkat",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.5.sp,
                            color = Color(0xFF9E9EA8)
                        )
                    }
                }

                // Cloud Account Login Option
                TextButton(
                    onClick = onOpenCloudIntegrations,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Buka Halaman Integrasi Akun Lengkap →",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Color(0xFF38EF7D)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (playlistUrl.isNotBlank()) onImportUrl(playlistUrl.trim()) },
                enabled = playlistUrl.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38EF7D)),
                modifier = Modifier.testTag("universal_import_submit_button")
            ) {
                Text(
                    text = if (urlPreview != null) "Impor (${urlPreview!!.trackCount} Lagu)" else "Impor URL",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Color(0xFF9E9EA8))
            }
        }
    )
}


@Composable
private fun ListeningExperienceCard(
    mainValue: String,
    mainValueFontSize: androidx.compose.ui.unit.TextUnit,
    metricLabel: String,
    userHandle: String,
    canEditUsername: Boolean = true,
    fullLiquidGlassUi: Boolean = false,
    bottomSubtitle: String,
    memberSinceLabel: String?,
    memberSinceValue: String?,
    onClick: () -> Unit,
    onUsernameClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(236.dp)
            .height(172.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = if (fullLiquidGlassUi) {
                        listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.06f),
                            Color(0xFF17171A).copy(alpha = 0.75f),
                        )
                    } else {
                        listOf(
                            Color(0xFF343438),
                            Color(0xFF232326),
                            Color(0xFF17171A)
                        )
                    },
                    start = Offset(0f, 0f),
                    end = Offset(500f, 450f)
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (fullLiquidGlassUi) 0.18f else 0.06f),
                shape = RoundedCornerShape(22.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header: PENGALAMAN MENDENGARKAN ANDA + 3 Waves Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "PENGALAMAN MENDENGARKAN\nANDA",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                letterSpacing = 0.4.sp,
                color = Color(0xFFA8A8AE)
            )

            BitChordWavesLogo(
                modifier = Modifier.size(width = 34.dp, height = 22.dp)
            )
        }

        // Middle Metric
        Column {
            Text(
                text = mainValue,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = mainValueFontSize,
                color = Color(0xFFDCDCE2),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = metricLabel,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.5.sp,
                letterSpacing = 0.6.sp,
                color = Color(0xFF9A9AA0)
            )
        }

        // Bottom Footer (Clicking username opens EditUsernameDialog only when not signed into YTM)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .then(if (canEditUsername) Modifier.clickable(onClick = onUsernameClick) else Modifier)
                        .padding(vertical = 1.dp)
                        .testTag("library_edit_username_trigger")
                ) {
                    Text(
                        text = userHandle,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFFDCDCE2)
                    )
                    if (canEditUsername) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Ubah Username",
                            tint = Color(0xFF9A9AA0),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
                Text(
                    text = bottomSubtitle,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    color = Color(0xFF8E8E95),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (memberSinceLabel != null && memberSinceValue != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = memberSinceLabel,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 8.sp,
                        letterSpacing = 0.3.sp,
                        color = Color(0xFF8E8E95)
                    )
                    Text(
                        text = memberSinceValue,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.5.sp,
                        color = Color(0xFFBEBEC4)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditUsernameDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nameInput by remember(currentUsername) { mutableStateOf(currentUsername) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF181820),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = "Ubah Username",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Nama ini ditampilkan pada kartu Pengalaman Mendengarkan dan Playlist Anda.",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFFA0A0AB)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    placeholder = { Text("user") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8AB4F8),
                        unfocusedBorderColor = Color(0xFF383846)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_username_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nameInput.trim().ifBlank { "user" }) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8AB4F8),
                    contentColor = Color(0xFF10131A)
                ),
                modifier = Modifier.testTag("save_username_button")
            ) {
                Text(
                    text = "Simpan",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA0A0AB)
                )
            }
        }
    )
}

@Composable
private fun BitChordWavesLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeColor = Color(0xFFBEBEC4)

        val path1 = Path().apply {
            moveTo(w * 0.18f, h * 0.14f)
            quadraticTo(w * 0.52f, h * 0.30f, w * 0.88f, h * 0.20f)
        }
        drawPath(
            path = path1,
            color = strokeColor,
            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
        )

        val path2 = Path().apply {
            moveTo(w * 0.10f, h * 0.48f)
            quadraticTo(w * 0.50f, h * 0.66f, w * 0.92f, h * 0.54f)
        }
        drawPath(
            path = path2,
            color = strokeColor,
            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
        )

        val path3 = Path().apply {
            moveTo(w * 0.04f, h * 0.82f)
            quadraticTo(w * 0.48f, h * 1.02f, w * 0.96f, h * 0.88f)
        }
        drawPath(
            path = path3,
            color = strokeColor,
            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun SpotifyCircleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        drawCircle(
            color = Color(0xFFDCDCE2),
            radius = radius,
            center = center
        )

        val w = size.width
        val h = size.height
        val waveColor = Color(0xFF161618)

        val topWave = Path().apply {
            moveTo(w * 0.24f, h * 0.37f)
            quadraticTo(w * 0.52f, h * 0.27f, w * 0.77f, h * 0.42f)
        }
        drawPath(
            path = topWave,
            color = waveColor,
            style = Stroke(width = 3.6.dp.toPx(), cap = StrokeCap.Round)
        )

        val midWave = Path().apply {
            moveTo(w * 0.28f, h * 0.52f)
            quadraticTo(w * 0.51f, h * 0.44f, w * 0.73f, h * 0.56f)
        }
        drawPath(
            path = midWave,
            color = waveColor,
            style = Stroke(width = 3.1.dp.toPx(), cap = StrokeCap.Round)
        )

        val botWave = Path().apply {
            moveTo(w * 0.32f, h * 0.66f)
            quadraticTo(w * 0.50f, h * 0.60f, w * 0.68f, h * 0.69f)
        }
        drawPath(
            path = botWave,
            color = waveColor,
            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllLibraryPlaylistsBottomSheet(
    username: String,
    favoriteTracksCount: Int,
    libraryPlaylists: List<UserLibraryPlaylist>,
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit,
    onImportSpotify: () -> Unit,
    onOpenLikedSongs: () -> Unit,
    onOpenPlaylist: (UserLibraryPlaylist) -> Unit,
    onOpenPlaylistMenu: (UserLibraryPlaylist) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0E0E12),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Semua Playlist Library",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    text = "+ Buat Baru",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color(0xFF8AB4F8),
                    modifier = Modifier.clickable(onClick = onCreateNew)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
            ) {
                item(key = "all_sheet_liked") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenLikedSongs)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF7B61F8), Color(0xFFE063B3))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ThumbUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Musik yang Disukai",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Playlist otomatis • $favoriteTracksCount lagu",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF8E8E95)
                            )
                        }
                    }
                }

                item(key = "all_sheet_import") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onImportSpotify)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF18181C)),
                            contentAlignment = Alignment.Center
                        ) {
                            SpotifyCircleLogo(modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Impor dari Spotify / YouTube",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Dari tautan playlist",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF8E8E95)
                            )
                        }
                    }
                }

                items(libraryPlaylists, key = { "all_sheet_${it.id}" }) { playlist ->
                    val coverUrl = playlist.thumbnailUrl.ifBlank {
                        playlist.tracks.firstOrNull()?.thumbnailUrl ?: ""
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPlaylist(playlist) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E26)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (coverUrl.isNotBlank()) {
                                AsyncImage(
                                    model = remember(coverUrl) {
                                        ImageRequest.Builder(context)
                                            .data(coverUrl)
                                            .size(Size(144, 144))
                                            .crossfade(false)
                                            .build()
                                    },
                                    contentDescription = playlist.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.title,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$username • ${playlist.tracks.size} lagu",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF8E8E95)
                            )
                        }
                        IconButton(onClick = { onOpenPlaylistMenu(playlist) }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Kelola Playlist",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistManagementBottomSheet(
    playlist: UserLibraryPlaylist,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: () -> Unit,
    onAddSongs: () -> Unit,
    onDownloadPlaylist: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF16161D),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = playlist.title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${playlist.tracks.size} lagu • Kelola Playlist Library",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // 1. Pin / Unpin Playlist
            SheetActionRow(
                icon = Icons.Rounded.PushPin,
                iconTint = Color(0xFF38EF7D),
                title = if (playlist.isPinned) "Lepas Pin Playlist" else "Pin Playlist di Atas",
                subtitle = if (playlist.isPinned) "Kembalikan ke urutan normal" else "Sematkan di bagian paling depan Library",
                onClick = onTogglePin,
                testTag = "playlist_menu_pin"
            )

            // 2. Rename Playlist
            SheetActionRow(
                icon = Icons.Rounded.Edit,
                iconTint = Color(0xFFB4C5FF),
                title = "Rename Nama Playlist",
                subtitle = "Ubah judul playlist ini",
                onClick = onRename,
                testTag = "playlist_menu_rename"
            )

            // 3. Tambahkan Musik ke Playlist
            SheetActionRow(
                icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                iconTint = Color(0xFFD0BCFF),
                title = "Tambahkan Musik ke Playlist",
                subtitle = "Pilih lagu untuk dimasukkan ke playlist ini",
                onClick = onAddSongs,
                testTag = "playlist_menu_add_music"
            )

            // 4. Download Semua Lagu di Playlist (Offline)
            SheetActionRow(
                icon = Icons.Rounded.Download,
                iconTint = Color(0xFF38EF7D),
                title = "Download Semua Musik (Offline)",
                subtitle = "Unduh semua lagu di playlist ini dengan bar % di Notifikasi",
                onClick = onDownloadPlaylist,
                testTag = "playlist_menu_download_all"
            )

            // 5. Delete Playlist
            SheetActionRow(
                icon = Icons.Rounded.DeleteOutline,
                iconTint = Color(0xFFFF6B6B),
                title = "Delete Playlist",
                subtitle = "Hapus playlist ini secara permanen dari Library",
                onClick = onDelete,
                testTag = "playlist_menu_delete"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSongsToSpecificPlaylistSheet(
    playlist: UserLibraryPlaylist,
    candidateTracks: List<MusicItem>,
    onDismiss: () -> Unit,
    onAddTrack: (MusicItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val existingIds = remember(playlist.tracks) { playlist.tracks.map { it.id }.toSet() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF16161D),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Tambah Musik ke \"${playlist.title}\"",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ketuk tombol (+) pada lagu untuk langsung menambahkannya",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                items(candidateTracks, key = { "add_cand_${it.id}" }) { track ->
                    val alreadyIn = existingIds.contains(track.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !alreadyIn) { onAddTrack(track) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = remember(track.thumbnailUrl) {
                                ImageRequest.Builder(context)
                                    .data(track.thumbnailUrl)
                                    .size(Size(128, 128))
                                    .crossfade(false)
                                    .build()
                            },
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF23232E))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.cleanArtist,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = Color.White.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (alreadyIn) Color.White.copy(alpha = 0.1f)
                                    else Color(0xFF3E4759)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (alreadyIn) "Tersimpan" else "+ Tambah",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (alreadyIn) Color.White.copy(alpha = 0.5f) else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
