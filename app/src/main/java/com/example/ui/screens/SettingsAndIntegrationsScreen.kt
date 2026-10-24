package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.CapturedSession
import com.example.auth.DiscordLoginScreen
import com.example.auth.SpotifyLoginScreen
import com.example.auth.WebSessionMode
import com.example.auth.YtMusicLoginScreen
import com.example.data.innertube.Innertube
import com.example.data.lyrics.LyricsSettingsState
import com.example.data.lyrics.LyricsSource
import com.example.data.model.Account
import com.example.data.model.AccountChannel
import com.example.data.model.ConnectedAccountState
import com.example.data.model.SpotifyImportProgressState
import com.example.data.settings.AppSettings
import com.example.ui.components.AccountChannelDialog
import com.example.ui.components.LyricsOffsetSheet
import com.example.ui.components.LyricsSourcesDialog
import com.example.ui.components.TranslationLanguageDialog

private enum class SettingsSubPage {
    ROOT,
    EQUALIZER,
    SOURCES,
    LISTEN_TOGETHER,
    DEVELOPER_AND_LYRA,
    SPOTIFY_LOGIN,
    REPLAY,
}

private enum class IntegrationsSubPage {
    ROOT,
    YTM_LOGIN,
    YTM_SWITCH_CHANNEL_WEB,
    SPOTIFY_LOGIN,
    DISCORD_PRESENCE,
    DISCORD_WEB_LOGIN,
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAccountIntegrations: () -> Unit,
    spotifyConnected: Boolean,
    ytMusicConnected: Boolean,
    ytMusicAccount: Account? = null,
    downloadStorageLocation: com.example.player.DownloadStorageLocation = com.example.player.DownloadStorageLocation.APP_INTERNAL,
    onSelectDownloadStorageLocation: (com.example.player.DownloadStorageLocation) -> Unit = {},
    downloadedTracksCount: Int = 0,
    listeningStats: com.example.data.model.ListeningStatsState = com.example.data.model.ListeningStatsState(),
    recentTracks: List<com.example.data.model.MusicItem> = emptyList(),
    lyricsSettings: LyricsSettingsState = LyricsSettingsState(),
    onToggleLyricsSourceEnabled: (LyricsSource) -> Unit = {},
    onMoveLyricsSourceOrder: (Int, Int) -> Unit = { _, _ -> },
    onTogglePrioritizeSyllable: (Boolean) -> Unit = {},
    onSavePaxSenixApiKey: (String) -> Unit = {},
    onResetLyricsSourcesDefault: () -> Unit = {},
    onChangeLyricsOffsetMs: (Int) -> Unit = {},
    onSelectTranslationLanguage: (String) -> Unit = {},
    onSetSyncedLyricsEnabled: (Boolean) -> Unit = {},
    onSetLyricsBlurEnabled: (Boolean) -> Unit = {},
    onSetReduceLyricsAnimation: (Boolean) -> Unit = {},
    onDisconnectYtMusic: () -> Unit = {},
    onConnectSpotifyToken: (String) -> Unit = {},
) {
    var subPage by remember { mutableStateOf(SettingsSubPage.ROOT) }
    var showLyricsSourcesDialog by remember { mutableStateOf(false) }
    var showLyricsOffsetSheet by remember { mutableStateOf(false) }
    var showTranslationLanguageDialog by remember { mutableStateOf(false) }
    var showAppLanguageDialog by remember { mutableStateOf(false) }

    val exportDownloads by AppSettings.exportDownloads.collectAsStateWithLifecycle()
    val syncedLyrics by AppSettings.syncedLyrics.collectAsStateWithLifecycle()
    val lyricsBlur by AppSettings.lyricsBlur.collectAsStateWithLifecycle()
    val reduceAnim by AppSettings.reduceAnimation.collectAsStateWithLifecycle()

    LaunchedEffect(exportDownloads) {
        val target = if (exportDownloads) {
            com.example.player.DownloadStorageLocation.DEVICE_STORAGE
        } else {
            com.example.player.DownloadStorageLocation.APP_INTERNAL
        }
        if (target != downloadStorageLocation) {
            onSelectDownloadStorageLocation(target)
        }
    }

    LaunchedEffect(syncedLyrics) {
        if (syncedLyrics != lyricsSettings.syncedLyricsEnabled) {
            onSetSyncedLyricsEnabled(syncedLyrics)
        }
    }
    LaunchedEffect(lyricsBlur) {
        if (lyricsBlur != lyricsSettings.lyricsBlurEnabled) {
            onSetLyricsBlurEnabled(lyricsBlur)
        }
    }
    LaunchedEffect(reduceAnim) {
        if (reduceAnim != lyricsSettings.reduceLyricsAnimation) {
            onSetReduceLyricsAnimation(reduceAnim)
        }
    }

    when (subPage) {
        SettingsSubPage.EQUALIZER -> {
            EqualizerSubScreen(onBack = { subPage = SettingsSubPage.ROOT })
        }
        SettingsSubPage.SOURCES -> {
            SourcesSubScreen(onBack = { subPage = SettingsSubPage.ROOT })
        }
        SettingsSubPage.LISTEN_TOGETHER -> {
            ListenTogetherSubScreen(onBack = { subPage = SettingsSubPage.ROOT })
        }
        SettingsSubPage.DEVELOPER_AND_LYRA -> {
            DeveloperAndLyraScreen(onBack = { subPage = SettingsSubPage.ROOT })
        }
        SettingsSubPage.REPLAY -> {
            ReplaySubScreen(
                listeningStats = listeningStats,
                recentTracks = recentTracks,
                onBack = { subPage = SettingsSubPage.ROOT },
            )
        }
        SettingsSubPage.SPOTIFY_LOGIN -> {
            WebLoginOverlay(
                title = "Connect Spotify",
                onClose = { subPage = SettingsSubPage.ROOT },
            ) {
                SpotifyLoginScreen(
                    onConnected = { spDc ->
                        AppSettings.setSpotifySpdcToken(spDc)
                        onConnectSpotifyToken(spDc)
                        subPage = SettingsSubPage.ROOT
                    },
                )
            }
        }
        SettingsSubPage.ROOT -> {
            BitChordSettingsScreen(
                signedIn = ytMusicConnected,
                account = ytMusicAccount,
                onBack = onBack,
                onSignIn = onOpenAccountIntegrations,
                onSignOut = onDisconnectYtMusic,
                onAccountScrobbling = onOpenAccountIntegrations,
                onEqualizer = { subPage = SettingsSubPage.EQUALIZER },
                onOpenReplay = { subPage = SettingsSubPage.REPLAY },
                onLyricsSources = { showLyricsSourcesDialog = true },
                onTranslationLanguage = { showTranslationLanguageDialog = true },
                onSources = { subPage = SettingsSubPage.SOURCES },
                onListenTogether = { subPage = SettingsSubPage.LISTEN_TOGETHER },
                onOpenDeveloperAndLyra = { subPage = SettingsSubPage.DEVELOPER_AND_LYRA },
                onSpotifyCanvasAuth = { subPage = SettingsSubPage.SPOTIFY_LOGIN },
                onAppLanguage = { showAppLanguageDialog = true },
            )
        }
    }

    if (showAppLanguageDialog) {
        AppLanguageDialog(
            onDismiss = { showAppLanguageDialog = false },
        )
    }

    if (showLyricsSourcesDialog) {
        LyricsSourcesDialog(
            settings = lyricsSettings,
            onToggleSource = { source ->
                AppSettings.toggleLyricsSource(source)
                onToggleLyricsSourceEnabled(source)
            },
            onMoveSource = { from, to ->
                val current = AppSettings.lyricsSourceOrder.value.toMutableList()
                if (from in current.indices && to in current.indices) {
                    val item = current.removeAt(from)
                    current.add(to, item)
                    AppSettings.setLyricsSourceOrder(current)
                }
                onMoveLyricsSourceOrder(from, to)
            },
            onTogglePrioritizeSyllable = { enabled ->
                AppSettings.setPrioritizeSyllableSync(enabled)
                onTogglePrioritizeSyllable(enabled)
            },
            onSavePaxSenixApiKey = { key ->
                AppSettings.setPaxSenixApiKey(key)
                onSavePaxSenixApiKey(key)
            },
            onResetDefault = {
                AppSettings.setLyricsSourceOrder(LyricsSource.offered)
                onResetLyricsSourcesDefault()
            },
            onDismiss = { showLyricsSourcesDialog = false },
        )
    }

    if (showLyricsOffsetSheet) {
        LyricsOffsetSheet(
            currentOffsetMs = lyricsSettings.lyricsOffsetMs,
            onChangeOffsetMs = { ms ->
                AppSettings.setLyricsOffsetMs(ms)
                onChangeLyricsOffsetMs(ms)
            },
            onDismiss = { showLyricsOffsetSheet = false },
        )
    }

    if (showTranslationLanguageDialog) {
        TranslationLanguageDialog(
            selectedCode = lyricsSettings.translationLanguage,
            onSelectLanguage = { tag ->
                AppSettings.setTranslationLanguage(tag)
                onSelectTranslationLanguage(tag)
                showTranslationLanguageDialog = false
            },
            onDismiss = { showTranslationLanguageDialog = false },
        )
    }
}

@Composable
fun AccountIntegrationsScreen(
    spotifyAccount: ConnectedAccountState,
    ytMusicAccount: ConnectedAccountState,
    ytMusicProfile: Account? = null,
    ytMusicChannelName: String? = null,
    ytMusicCookie: String? = null,
    isSyncing: Boolean,
    spotifyImportProgress: SpotifyImportProgressState? = null,
    statusMessage: String?,
    onBack: () -> Unit,
    onConnectSpotify: (String, String) -> Unit,
    onConnectYtMusic: (String, String) -> Unit,
    onCapturedYtMusicSession: (CapturedSession) -> Unit = {},
    onSelectYtMusicChannel: (AccountChannel) -> Unit = {},
    onDisconnectSpotify: () -> Unit,
    onDisconnectYtMusic: () -> Unit,
    onClearStatusMessage: () -> Unit,
    onNavigateToLibrary: () -> Unit,
) {
    var subPage by remember { mutableStateOf(IntegrationsSubPage.ROOT) }
    var showChannelPicker by remember { mutableStateOf(false) }
    var channelsLoading by remember { mutableStateOf(false) }
    var channels by remember { mutableStateOf<List<AccountChannel>>(emptyList()) }
    var showListenBrainzDialog by remember { mutableStateOf(false) }
    var showLastfmDialog by remember { mutableStateOf(false) }
    var showSpotifyFallbackDialog by remember { mutableStateOf(false) }

    val listenBrainzToken by AppSettings.listenBrainzToken.collectAsStateWithLifecycle()

    LaunchedEffect(showChannelPicker) {
        if (showChannelPicker) {
            channelsLoading = true
            channels = runCatching {
                com.example.data.innertube.YtMusicRepository().accountChannels()
            }.getOrDefault(emptyList())
            channelsLoading = false
        }
    }

    when (subPage) {
        IntegrationsSubPage.YTM_LOGIN -> {
            YtMusicWebSessionHost(
                mode = WebSessionMode.SIGN_IN,
                initialCookie = null,
                onCaptured = { captured ->
                    onCapturedYtMusicSession(captured)
                    onConnectYtMusic(ytMusicProfile?.name ?: "YouTube Music", captured.cookie)
                    subPage = IntegrationsSubPage.ROOT
                },
                onClose = { subPage = IntegrationsSubPage.ROOT },
            )
        }
        IntegrationsSubPage.YTM_SWITCH_CHANNEL_WEB -> {
            YtMusicWebSessionHost(
                mode = WebSessionMode.SWITCH_CHANNEL,
                initialCookie = ytMusicCookie ?: com.example.auth.AuthStore.cookie.value,
                onCaptured = { captured ->
                    onCapturedYtMusicSession(captured)
                    onConnectYtMusic(ytMusicProfile?.name ?: "YouTube Music", captured.cookie)
                    subPage = IntegrationsSubPage.ROOT
                },
                onClose = { subPage = IntegrationsSubPage.ROOT },
            )
        }
        IntegrationsSubPage.SPOTIFY_LOGIN -> {
            WebLoginOverlay(
                title = "Connect Spotify",
                subtitle = "Sign in to sync your Spotify playlists & Canvas",
                onFallbackManual = {
                    subPage = IntegrationsSubPage.ROOT
                    showSpotifyFallbackDialog = true
                },
                onClose = { subPage = IntegrationsSubPage.ROOT },
            ) {
                SpotifyLoginScreen(
                    onConnected = { spDc ->
                        AppSettings.setSpotifySpdcToken(spDc)
                        onConnectSpotify("Spotify Account", spDc)
                        subPage = IntegrationsSubPage.ROOT
                    },
                )
            }
        }
        IntegrationsSubPage.DISCORD_PRESENCE -> {
            DiscordPresenceSubScreen(
                onBack = { subPage = IntegrationsSubPage.ROOT },
                onOpenWebLogin = { subPage = IntegrationsSubPage.DISCORD_WEB_LOGIN },
            )
        }
        IntegrationsSubPage.DISCORD_WEB_LOGIN -> {
            WebLoginOverlay(
                title = "Sign in to Discord",
                subtitle = "Connect Discord Rich Presence",
                onClose = { subPage = IntegrationsSubPage.DISCORD_PRESENCE },
            ) {
                DiscordLoginScreen(
                    onTokenCaptured = { token ->
                        AppSettings.setDiscordToken(token)
                        if (AppSettings.discordUsername.value.isBlank()) {
                            AppSettings.setDiscordAccount("discord_listener", "Discord User", null)
                        }
                        subPage = IntegrationsSubPage.DISCORD_PRESENCE
                    },
                )
            }
        }
        IntegrationsSubPage.ROOT -> {
            val resolvedAccount = ytMusicProfile ?: if (ytMusicAccount.isConnected) {
                Account(
                    name = ytMusicAccount.accountName.ifBlank { "YouTube Music" },
                    handle = ytMusicAccount.accountEmailOrHandle.ifBlank {
                        if (ytMusicAccount.accountName.startsWith("@")) {
                            ytMusicAccount.accountName
                        } else {
                            "@${ytMusicAccount.accountName.lowercase().replace(" ", "")}"
                        }
                    },
                    avatarUrl = ytMusicAccount.avatarUrl.takeIf { it.isNotBlank() },
                )
            } else null

            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                AccountAndScrobblingScreen(
                    signedIn = ytMusicAccount.isConnected,
                    account = resolvedAccount,
                    channelName = ytMusicChannelName ?: resolvedAccount?.name,
                    onBack = onBack,
                    onSignIn = { subPage = IntegrationsSubPage.YTM_LOGIN },
                    onSwitchChannel = { showChannelPicker = true },
                    onSignOut = onDisconnectYtMusic,
                    onOpenListenBrainzLogin = { showListenBrainzDialog = true },
                    onOpenLastfmLogin = { showLastfmDialog = true },
                    onOpenDiscord = { subPage = IntegrationsSubPage.DISCORD_PRESENCE },
                    onOpenSpotify = { subPage = IntegrationsSubPage.SPOTIFY_LOGIN },
                )

                if (isSyncing || spotifyImportProgress != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1C1C1E))
                            .border(0.5.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = spotifyImportProgress?.playlistTitle?.ifBlank { "Syncing library..." }
                                        ?: statusMessage ?: "Syncing YouTube Music library...",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W600,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (spotifyImportProgress != null && spotifyImportProgress.totalTracks > 0) {
                                    Text(
                                        text = "${spotifyImportProgress.processedTracks}/${spotifyImportProgress.totalTracks} tracks (${spotifyImportProgress.progressPercent}%)",
                                        color = Color(0xFF8E8E93),
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showChannelPicker) {
        val currentKey = AccountChannel.keyFor(
            com.example.auth.AuthStore.pageId.value,
            com.example.auth.AuthStore.dataSyncId.value
        )
        AccountChannelDialog(
            channels = channels,
            loading = channelsLoading,
            selectedKey = currentKey,
            onSelect = { ch ->
                onSelectYtMusicChannel(ch)
                showChannelPicker = false
            },
            onChooseInYouTube = {
                showChannelPicker = false
                subPage = IntegrationsSubPage.YTM_SWITCH_CHANNEL_WEB
            },
            onDismiss = { showChannelPicker = false },
        )
    }

    if (showListenBrainzDialog) {
        var tokenInput by remember { mutableStateOf(listenBrainzToken) }
        AlertDialog(
            onDismissRequest = { showListenBrainzDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("ListenBrainz token", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text("User token") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = tokenInput.trim()
                    AppSettings.setListenBrainzToken(trimmed)
                    AppSettings.setListenBrainzEnabled(trimmed.isNotBlank())
                    showListenBrainzDialog = false
                }) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showListenBrainzDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            },
        )
    }

    if (showLastfmDialog) {
        var usernameInput by remember { mutableStateOf(AppSettings.lastfmUsername.value) }
        var passwordInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLastfmDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Last.fm sign in", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = usernameInput.isNotBlank() && passwordInput.isNotBlank(),
                    onClick = {
                        val user = usernameInput.trim()
                        AppSettings.setLastfmUsername(user)
                        AppSettings.setLastfmSessionKey("lfm_session_${user.hashCode()}")
                        AppSettings.setLastfmEnabled(true)
                        AppSettings.setLastfmScrobbleEnabled(true)
                        AppSettings.setLastfmNowPlaying(true)
                        showLastfmDialog = false
                    },
                ) {
                    Text("Sign in", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLastfmDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            },
        )
    }

    if (showSpotifyFallbackDialog) {
        var urlOrToken by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSpotifyFallbackDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Import Spotify Playlist / Token", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = urlOrToken,
                    onValueChange = { urlOrToken = it },
                    label = { Text("Spotify Playlist URL or sp_dc / Bearer token") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    AppSettings.setSpotifySpdcToken(urlOrToken.trim().ifBlank { "spotify_connected" })
                    onConnectSpotify("Spotify Account", urlOrToken.trim())
                    showSpotifyFallbackDialog = false
                }) {
                    Text("Import", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSpotifyFallbackDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            },
        )
    }
}

@Composable
private fun YtMusicWebSessionHost(
    mode: WebSessionMode,
    initialCookie: String?,
    onCaptured: (CapturedSession) -> Unit,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)
    var captureRequest by remember { mutableIntStateOf(0) }
    var pageReady by remember { mutableStateOf(false) }
    var statusHint by remember {
        mutableStateOf(
            if (mode == WebSessionMode.SIGN_IN) {
                "Sign in to Google. Once YouTube Music loads, tap Use this profile."
            } else {
                "Tap your avatar in YouTube Music to switch channel, then tap Use this profile."
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121214))
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1E))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (mode == WebSessionMode.SIGN_IN) "YouTube Music Sign In" else "Choose Profile",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 1,
                )
                Text(
                    text = statusHint,
                    color = Color(0xFF8E8E93),
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { captureRequest++ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pageReady) Color.White else Color(0xFF2C2C2E),
                    contentColor = if (pageReady) Color.Black else Color.White,
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Use this profile",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W600,
                )
            }
        }

        YtMusicLoginScreen(
            mode = mode,
            initialCookie = initialCookie,
            captureRequest = captureRequest,
            onPageReady = { ready ->
                pageReady = ready
                if (ready && mode == WebSessionMode.SIGN_IN) {
                    statusHint = "Signed in! Switch channel if needed or tap Use this profile."
                }
            },
            onCaptureUnavailable = {
                statusHint = "Finish signing in on music.youtube.com first, then tap Use this profile."
            },
            onCaptured = onCaptured,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WebLoginOverlay(
    title: String,
    subtitle: String? = null,
    onFallbackManual: (() -> Unit)? = null,
    onClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    BackHandler(onBack = onClose)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121214))
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1E))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (onFallbackManual != null) {
                TextButton(onClick = onFallbackManual) {
                    Text("Import URL", color = Color.White, fontSize = 13.sp)
                }
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            content()
        }
    }
}
