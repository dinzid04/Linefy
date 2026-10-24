package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.SwitchAccount
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.clearSpotifyWebSession
import com.example.data.model.Account
import com.example.data.settings.AppSettings
import kotlin.math.roundToInt

@Composable
fun AccountAndScrobblingScreen(
    signedIn: Boolean,
    account: Account?,
    channelName: String?,
    onBack: (() -> Unit)? = null,
    onSignIn: () -> Unit,
    onSwitchChannel: () -> Unit,
    onSignOut: () -> Unit,
    onOpenListenBrainzLogin: () -> Unit,
    onOpenLastfmLogin: () -> Unit,
    onOpenDiscord: () -> Unit,
    onOpenSpotify: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(bottom = 140.dp),
    modifier: Modifier = Modifier,
) {
    if (onBack != null) {
        BackHandler(onBack = onBack)
    }

    val lastfmEnabled by AppSettings.lastfmEnabled.collectAsStateWithLifecycle()
    val lastfmUsername by AppSettings.lastfmUsername.collectAsStateWithLifecycle()
    val lastfmSessionKey by AppSettings.lastfmSessionKey.collectAsStateWithLifecycle()
    val lastfmScrobbleEnabled by AppSettings.lastfmScrobbleEnabled.collectAsStateWithLifecycle()
    val lastfmNowPlayingEnabled by AppSettings.lastfmNowPlaying.collectAsStateWithLifecycle()
    val lastfmPrimaryArtistOnly by AppSettings.lastfmPrimaryArtistOnly.collectAsStateWithLifecycle()
    val scrobbleMinDuration by AppSettings.scrobbleMinDuration.collectAsStateWithLifecycle()
    val scrobbleDelayPercent by AppSettings.scrobbleDelayPercent.collectAsStateWithLifecycle()
    val scrobbleDelaySeconds by AppSettings.scrobbleDelaySeconds.collectAsStateWithLifecycle()
    val listenBrainzEnabled by AppSettings.listenBrainzEnabled.collectAsStateWithLifecycle()
    val listenBrainzToken by AppSettings.listenBrainzToken.collectAsStateWithLifecycle()
    val listenBrainzPrimaryArtistOnly by AppSettings.listenBrainzPrimaryArtistOnly.collectAsStateWithLifecycle()
    val discordToken by AppSettings.discordToken.collectAsStateWithLifecycle()
    val discordUsername by AppSettings.discordUsername.collectAsStateWithLifecycle()
    val discordRpcEnabled by AppSettings.discordRpcEnabled.collectAsStateWithLifecycle()
    val spotifyConnected by AppSettings.spotifySpdcToken.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Text(
            text = "Account & integrations",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.W800,
                fontSize = 34.sp,
                letterSpacing = (-0.8).sp,
            ),
            color = Color.White,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 14.dp),
        )

        // One card: who is signed in, which profile is listening, and the way
        // out, with the explanation of profiles underneath.
        SettingsGroup(
            footer = if (signedIn) {
                "Your library, playlists, albums and liked songs sync with the YouTube Music profile selected above."
            } else null,
            topSpacing = 0.dp,
        ) {
            AccountCard(
                signedIn = signedIn,
                account = account,
                onSignIn = onSignIn,
                onClick = onSwitchChannel,
                grouped = true,
            )
            if (signedIn) {
                FullWidthDivider()
                SettingsRow(
                    icon = Icons.Rounded.SwitchAccount,
                    title = "Listen as",
                    subtitle = channelName ?: account?.name ?: "Default YouTube profile",
                    onClick = onSwitchChannel,
                )
                FullWidthDivider()
                DestructiveRow(label = "Sign out", onClick = onSignOut)
            }
        }

        SettingsGroup(
            header = "SPOTIFY",
            footer = "Signing in makes your Spotify playlists available in Library and enables Spotify Canvas motion artwork.",
        ) {
            SettingsRow(
                leadingContent = { SpotifyGlyph(modifier = Modifier.size(22.dp)) },
                title = "Spotify",
                subtitle = if (spotifyConnected.isNotBlank()) "Connected" else "Tap to connect",
                onClick = onOpenSpotify,
            )
            if (spotifyConnected.isNotBlank()) {
                FullWidthDivider()
                DestructiveRow(
                    label = "Disconnect Spotify",
                    onClick = {
                        clearSpotifyWebSession()
                        AppSettings.setSpotifySpdcToken("")
                    },
                )
            }
        }

        SettingsGroup(
            header = "RICH PRESENCE",
            footer = "Shows the song you're listening to on your Discord profile.",
        ) {
            SettingsRow(
                leadingContent = { DiscordGlyph(modifier = Modifier.size(22.dp)) },
                title = "Discord",
                subtitle = when {
                    discordToken.isEmpty() -> "Tap to connect"
                    !discordRpcEnabled -> "Connected · Presence off"
                    discordUsername.isNotEmpty() -> "Sharing as @$discordUsername"
                    else -> "Sharing listens"
                },
                onClick = onOpenDiscord,
            )
        }

        if (AppSettings.scrobblingAvailable) {
            SettingsGroup(
                header = "SCROBBLING",
                footer = "Automatically logs the tracks you play to ListenBrainz and Last.fm.",
            ) {
                SettingsRow(
                    icon = Icons.Rounded.Cloud,
                    title = "ListenBrainz",
                    subtitle = if (listenBrainzEnabled && listenBrainzToken.isNotBlank()) {
                        "Connected"
                    } else {
                        "Enter token to enable"
                    },
                    trailing = {
                        Switch(
                            checked = listenBrainzEnabled,
                            onCheckedChange = { checked ->
                                if (checked && listenBrainzToken.isBlank()) {
                                    onOpenListenBrainzLogin()
                                } else {
                                    AppSettings.setListenBrainzEnabled(checked)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Color.White,
                                checkedThumbColor = Color.Black,
                                checkedBorderColor = Color.White,
                            ),
                        )
                    },
                    onClick = onOpenListenBrainzLogin,
                )
                if (listenBrainzEnabled && listenBrainzToken.isNotBlank()) {
                    RowDivider()
                    SettingsRow(
                        icon = Icons.Rounded.GraphicEq,
                        title = "Scrobble primary artist only",
                        subtitle = "Strip featured artists before submitting listens",
                        trailing = {
                            Switch(
                                checked = listenBrainzPrimaryArtistOnly,
                                onCheckedChange = AppSettings::setListenBrainzPrimaryArtistOnly,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color.White,
                                    checkedThumbColor = Color.Black,
                                    checkedBorderColor = Color.White,
                                ),
                            )
                        },
                        onClick = { AppSettings.setListenBrainzPrimaryArtistOnly(!listenBrainzPrimaryArtistOnly) },
                    )
                }
                RowDivider()
                SettingsRow(
                    icon = Icons.Rounded.History,
                    title = "Last.fm",
                    subtitle = if (lastfmSessionKey.isNotBlank()) {
                        "Signed in as $lastfmUsername"
                    } else {
                        "Tap to sign in"
                    },
                    trailing = {
                        Switch(
                            checked = lastfmEnabled,
                            onCheckedChange = { checked ->
                                if (checked && lastfmSessionKey.isBlank()) {
                                    onOpenLastfmLogin()
                                } else {
                                    AppSettings.setLastfmEnabled(checked)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Color.White,
                                checkedThumbColor = Color.Black,
                                checkedBorderColor = Color.White,
                            ),
                        )
                    },
                    onClick = {
                        if (lastfmSessionKey.isNotBlank()) {
                            AppSettings.setLastfmSessionKey("")
                            AppSettings.setLastfmUsername("")
                            AppSettings.setLastfmEnabled(false)
                            AppSettings.setLastfmScrobbleEnabled(false)
                            AppSettings.setLastfmNowPlaying(false)
                        } else {
                            onOpenLastfmLogin()
                        }
                    },
                )
                if (lastfmEnabled && lastfmSessionKey.isNotBlank()) {
                    RowDivider()
                    SettingsRow(
                        icon = Icons.Rounded.GraphicEq,
                        title = "Scrobble tracks",
                        subtitle = "Submit completed listens to your Last.fm profile",
                        trailing = {
                            Switch(
                                checked = lastfmScrobbleEnabled,
                                onCheckedChange = AppSettings::setLastfmScrobbleEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color.White,
                                    checkedThumbColor = Color.Black,
                                    checkedBorderColor = Color.White,
                                ),
                            )
                        },
                        onClick = { AppSettings.setLastfmScrobbleEnabled(!lastfmScrobbleEnabled) },
                    )
                    RowDivider()
                    SettingsRow(
                        icon = Icons.Rounded.GraphicEq,
                        title = "Scrobble primary artist only",
                        subtitle = "Keep scrobble artist tags clean",
                        trailing = {
                            Switch(
                                checked = lastfmPrimaryArtistOnly,
                                onCheckedChange = AppSettings::setLastfmPrimaryArtistOnly,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color.White,
                                    checkedThumbColor = Color.Black,
                                    checkedBorderColor = Color.White,
                                ),
                            )
                        },
                        onClick = { AppSettings.setLastfmPrimaryArtistOnly(!lastfmPrimaryArtistOnly) },
                    )
                    RowDivider()
                    SettingsRow(
                        icon = Icons.Rounded.GraphicEq,
                        title = "Now Playing",
                        subtitle = "Show the currently playing track on Last.fm",
                        trailing = {
                            Switch(
                                checked = lastfmNowPlayingEnabled,
                                onCheckedChange = AppSettings::setLastfmNowPlaying,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color.White,
                                    checkedThumbColor = Color.Black,
                                    checkedBorderColor = Color.White,
                                ),
                            )
                        },
                        onClick = { AppSettings.setLastfmNowPlaying(!lastfmNowPlayingEnabled) },
                    )
                }
            }

            if (lastfmEnabled && lastfmSessionKey.isNotBlank()) {
                SettingsGroup(header = "SCROBBLE TIMING") {
                    SliderRow(
                        icon = Icons.Rounded.Tune,
                        title = "Minimum song duration",
                        subtitle = "Skip tracks shorter than this",
                        value = "${scrobbleMinDuration}s",
                        sliderValue = scrobbleMinDuration.toFloat(),
                        onSliderValue = { AppSettings.setScrobbleMinDuration(it.roundToInt()) },
                        valueRange = 15f..120f,
                        steps = 20,
                    )
                    RowDivider()
                    SliderRow(
                        icon = Icons.Rounded.Tune,
                        title = "Scrobble delay",
                        subtitle = "Fraction of track that must play before scrobbling",
                        value = "${(scrobbleDelayPercent * 100).roundToInt()}%",
                        sliderValue = scrobbleDelayPercent,
                        onSliderValue = { AppSettings.setScrobbleDelayPercent(it) },
                        valueRange = 0.1f..1.0f,
                        steps = 8,
                    )
                    RowDivider()
                    SliderRow(
                        icon = Icons.Rounded.Tune,
                        title = "Maximum delay",
                        subtitle = "Cap the wait on long tracks",
                        value = "${scrobbleDelaySeconds}s",
                        sliderValue = scrobbleDelaySeconds.toFloat(),
                        onSliderValue = { AppSettings.setScrobbleDelaySeconds(it.roundToInt()) },
                        valueRange = 30f..300f,
                        steps = 26,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** A hairline across the whole card, for setting a destructive row apart from the rows above it. */
@Composable
internal fun FullWidthDivider() {
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF2C2C2E))
}

@Composable
internal fun SpotifyGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        drawCircle(color = Color.White, radius = r)
        val stroke1 = r * 0.18f
        val stroke2 = r * 0.15f
        val stroke3 = r * 0.13f
        val path1 = Path().apply {
            moveTo(size.width * 0.24f, size.height * 0.36f)
            quadraticTo(size.width * 0.50f, size.height * 0.26f, size.width * 0.76f, size.height * 0.40f)
        }
        val path2 = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.52f)
            quadraticTo(size.width * 0.50f, size.height * 0.44f, size.width * 0.72f, size.height * 0.55f)
        }
        val path3 = Path().apply {
            moveTo(size.width * 0.32f, size.height * 0.67f)
            quadraticTo(size.width * 0.50f, size.height * 0.60f, size.width * 0.68f, size.height * 0.69f)
        }
        drawPath(path1, color = Color.Black, style = Stroke(width = stroke1, cap = StrokeCap.Round))
        drawPath(path2, color = Color.Black, style = Stroke(width = stroke2, cap = StrokeCap.Round))
        drawPath(path3, color = Color.Black, style = Stroke(width = stroke3, cap = StrokeCap.Round))
    }
}

@Composable
internal fun DiscordGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val body = Path().apply {
            moveTo(w * 0.20f, h * 0.24f)
            quadraticTo(w * 0.50f, h * 0.16f, w * 0.80f, h * 0.24f)
            quadraticTo(w * 0.94f, h * 0.48f, w * 0.84f, h * 0.76f)
            lineTo(w * 0.66f, h * 0.82f)
            lineTo(w * 0.61f, h * 0.72f)
            quadraticTo(w * 0.50f, h * 0.75f, w * 0.39f, h * 0.72f)
            lineTo(w * 0.34f, h * 0.82f)
            lineTo(w * 0.16f, h * 0.76f)
            quadraticTo(w * 0.06f, h * 0.48f, w * 0.20f, h * 0.24f)
            close()
        }
        drawPath(body, color = Color.White)
        drawCircle(color = Color.Black, radius = w * 0.085f, center = Offset(w * 0.37f, h * 0.50f))
        drawCircle(color = Color.Black, radius = w * 0.085f, center = Offset(w * 0.63f, h * 0.50f))
    }
}
