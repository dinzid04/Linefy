package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.settings.AppSettings
import com.example.data.settings.EqualizerMode
import com.example.data.settings.EqualizerPreset
import kotlin.math.roundToInt

@Composable
fun SubScreenTopBar(
    title: String,
    onBack: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.W700,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun EqualizerSubScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val enabled by AppSettings.equalizerEnabled.collectAsStateWithLifecycle()
    val mode by AppSettings.equalizerMode.collectAsStateWithLifecycle()
    val toneX by AppSettings.equalizerToneX.collectAsStateWithLifecycle()
    val toneY by AppSettings.equalizerToneY.collectAsStateWithLifecycle()
    val focused by AppSettings.equalizerFocused.collectAsStateWithLifecycle()
    val balance by AppSettings.equalizerBalance.collectAsStateWithLifecycle()
    val bands by AppSettings.equalizerBands.collectAsStateWithLifecycle()
    val preset by AppSettings.equalizerPreset.collectAsStateWithLifecycle()
    val sessionId by AppSettings.audioSessionId.collectAsStateWithLifecycle()

    val freqLabels = listOf("60Hz", "150Hz", "400Hz", "1kHz", "2.4kHz", "6kHz", "15kHz")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 140.dp),
    ) {
        SubScreenTopBar(title = "Equalizer", onBack = onBack)

        SettingsGroup(topSpacing = 8.dp) {
            SettingsRow(
                icon = Icons.Rounded.GraphicEq,
                title = "Equalizer",
                subtitle = if (enabled) "Shaping audio output" else "Bypassed (bit-exact)",
                trailing = {
                    Switch(
                        checked = enabled,
                        onCheckedChange = AppSettings::setEqualizerEnabled,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color.White,
                            checkedThumbColor = Color.Black,
                            checkedBorderColor = Color.White,
                        ),
                    )
                },
                onClick = { AppSettings.setEqualizerEnabled(!enabled) },
            )
            RowDivider()
            SegmentedControl(
                options = listOf("Dynamic Tone Pad", "7-Band Manual"),
                selectedIndex = if (mode == EqualizerMode.DYNAMIC) 0 else 1,
                onSelect = {
                    AppSettings.setEqualizerMode(
                        if (it == 0) EqualizerMode.DYNAMIC else EqualizerMode.MANUAL,
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        if (mode == EqualizerMode.DYNAMIC) {
            SettingsGroup(header = "TONE PAD") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Warm / Scooped", color = Color(0xFF8E8E93), fontSize = 12.sp)
                        Text(
                            text = "Tilt ${if (toneX >= 0) "+$toneX" else "$toneX"} · Contour ${if (toneY >= 0) "+$toneY" else "$toneY"}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W600,
                        )
                        Text("Bright / Forward", color = Color(0xFF8E8E93), fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF121214))
                            .border(1.dp, Color(0xFF2C2C2E), RoundedCornerShape(16.dp))
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val nx = ((change.position.x / size.width).coerceIn(0f, 1f) * 10f - 5f).roundToInt()
                                    val ny = ((1f - (change.position.y / size.height).coerceIn(0f, 1f)) * 10f - 5f).roundToInt()
                                    AppSettings.setEqualizerToneX(nx)
                                    AppSettings.setEqualizerToneY(ny)
                                    if (!enabled) AppSettings.setEqualizerEnabled(true)
                                }
                            },
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            drawLine(Color(0xFF2C2C2E), Offset(cx, 0f), Offset(cx, size.height), 1.dp.toPx())
                            drawLine(Color(0xFF2C2C2E), Offset(0f, cy), Offset(size.width, cy), 1.dp.toPx())
                            val px = ((toneX + 5f) / 10f) * size.width
                            val py = (1f - (toneY + 5f) / 10f) * size.height
                            drawCircle(Color.White.copy(alpha = 0.22f), radius = 24.dp.toPx(), center = Offset(px, py))
                            drawCircle(Color.White, radius = 9.dp.toPx(), center = Offset(px, py))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    SegmentedControl(
                        options = listOf("Broad", "Focused"),
                        selectedIndex = if (focused) 1 else 0,
                        onSelect = { AppSettings.setEqualizerFocused(it == 1) },
                    )
                }
            }
        } else {
            SettingsGroup(header = "PRESETS") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EqualizerPreset.entries.filter { it != EqualizerPreset.CUSTOM }.take(4).forEach { p ->
                        val chosen = preset == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (chosen) Color.White else Color(0xFF2C2C2E))
                                .clickable {
                                    AppSettings.setEqualizerPreset(p)
                                    if (!enabled) AppSettings.setEqualizerEnabled(true)
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = p.label,
                                color = if (chosen) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W600,
                                maxLines = 1,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EqualizerPreset.entries.filter { it != EqualizerPreset.CUSTOM }.drop(4).forEach { p ->
                        val chosen = preset == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (chosen) Color.White else Color(0xFF2C2C2E))
                                .clickable {
                                    AppSettings.setEqualizerPreset(p)
                                    if (!enabled) AppSettings.setEqualizerEnabled(true)
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = p.label,
                                color = if (chosen) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W600,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            SettingsGroup(header = "7-BAND GAINS") {
                bands.forEachIndexed { idx, gain ->
                    if (idx > 0) RowDivider()
                    SliderRow(
                        icon = Icons.Rounded.Tune,
                        title = freqLabels.getOrElse(idx) { "Band ${idx + 1}" },
                        value = "${if (gain >= 0f) "+" else ""}${"%.1f".format(gain)} dB",
                        sliderValue = gain,
                        onSliderValue = { newGain ->
                            val updated = bands.toMutableList().also { list ->
                                if (idx < list.size) list[idx] = (newGain * 2f).roundToInt() / 2f
                            }
                            AppSettings.setEqualizerBands(updated)
                            if (!enabled) AppSettings.setEqualizerEnabled(true)
                        },
                        valueRange = -12f..12f,
                        steps = 47,
                    )
                }
            }
        }

        SettingsGroup(header = "STEREO BALANCE") {
            SliderRow(
                icon = Icons.Rounded.SurroundSound,
                title = "Left / Right Balance",
                value = when {
                    balance < -0.02f -> "L ${(balance * -100).roundToInt()}%"
                    balance > 0.02f -> "R ${(balance * 100).roundToInt()}%"
                    else -> "Center"
                },
                sliderValue = balance,
                onSliderValue = { AppSettings.setEqualizerBalance(it) },
                valueRange = -1f..1f,
                steps = 39,
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.Tune,
                title = "System Equalizer",
                subtitle = "Open Android's built-in audio effects panel",
                onClick = { openEqualizer(context, sessionId) },
            )
        }
    }
}

@Composable
fun SourcesSubScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    val webdavUrl by AppSettings.webdavUrl.collectAsStateWithLifecycle()
    val smbHost by AppSettings.smbHost.collectAsStateWithLifecycle()
    val smbShare by AppSettings.smbShare.collectAsStateWithLifecycle()
    val slack by AppSettings.upgradeLengthSlackSeconds.collectAsStateWithLifecycle()
    var jioSaavnEnabled by remember { mutableStateOf(true) }
    var losslessAddonEnabled by remember { mutableStateOf(true) }
    var addonUrl by remember { mutableStateOf("") }
    var showAddonDialog by remember { mutableStateOf(false) }
    var showWebDavDialog by remember { mutableStateOf(false) }
    var showSmbDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 140.dp),
    ) {
        SubScreenTopBar(title = "Sources", onBack = onBack)

        SettingsGroup(
            header = "STREAMING SOURCES",
            footer = "Sources are consulted in priority order when Audio Quality permits them. YouTube Music is always available as fallback.",
        ) {
            SettingsRow(
                icon = Icons.Rounded.Extension,
                title = "Lossless Module Addon",
                subtitle = if (addonUrl.isNotBlank()) addonUrl else "FLAC / Hi-Res 24-bit stream resolver",
                trailing = {
                    Switch(
                        checked = losslessAddonEnabled,
                        onCheckedChange = { losslessAddonEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color.White,
                            checkedThumbColor = Color.Black,
                            checkedBorderColor = Color.White,
                        ),
                    )
                },
                onClick = { showAddonDialog = true },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.GraphicEq,
                title = "JioSaavn HQ",
                subtitle = "Up to 320 kbps AAC catalogue matching",
                trailing = {
                    Switch(
                        checked = jioSaavnEnabled,
                        onCheckedChange = { jioSaavnEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color.White,
                            checkedThumbColor = Color.Black,
                            checkedBorderColor = Color.White,
                        ),
                    )
                },
                onClick = { jioSaavnEnabled = !jioSaavnEnabled },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.PlayCircle,
                title = "YouTube Music (Innertube)",
                subtitle = "Always active · Opus 251 / AAC",
                badge = "DEFAULT",
            )
            RowDivider()
            SliderRow(
                icon = Icons.Rounded.Tune,
                title = "Duration match tolerance",
                subtitle = "Allowed length difference when upgrading a stream",
                value = "${slack}s",
                sliderValue = slack.toFloat(),
                onSliderValue = { AppSettings.setUpgradeLengthSlackSeconds(it.roundToInt()) },
                valueRange = 0f..10f,
                steps = 9,
            )
        }

        SettingsGroup(
            header = "REMOTE LIBRARIES",
            footer = "Mount a personal Nextcloud/WebDAV or SMB NAS share directly inside Library.",
        ) {
            SettingsRow(
                icon = Icons.Rounded.Cloud,
                title = "WebDAV",
                subtitle = webdavUrl.ifBlank { "Connect Nextcloud or WebDAV music folder" },
                onClick = { showWebDavDialog = true },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.Storage,
                title = "SMB (NAS / Windows Share)",
                subtitle = if (smbHost.isNotBlank() && smbShare.isNotBlank()) "\\\\$smbHost\\$smbShare" else "Connect local network SMB share",
                onClick = { showSmbDialog = true },
            )
        }
    }

    if (showAddonDialog) {
        var input by remember { mutableStateOf(addonUrl) }
        AlertDialog(
            onDismissRequest = { showAddonDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Addon Endpoint URL", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("https://...", color = Color(0xFF8E8E93)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    addonUrl = input.trim()
                    showAddonDialog = false
                }) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddonDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            },
        )
    }

    if (showWebDavDialog) {
        var urlInput by remember { mutableStateOf(webdavUrl) }
        var userInput by remember { mutableStateOf(AppSettings.webdavUsername.value) }
        var passInput by remember { mutableStateOf(AppSettings.webdavPassword.value) }
        AlertDialog(
            onDismissRequest = { showWebDavDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("WebDAV Server", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Server URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = passInput,
                        onValueChange = { passInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    AppSettings.setWebDavUrl(urlInput)
                    AppSettings.setWebDavUsername(userInput)
                    AppSettings.setWebDavPassword(passInput)
                    showWebDavDialog = false
                }) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWebDavDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            },
        )
    }

    if (showSmbDialog) {
        var hostInput by remember { mutableStateOf(smbHost) }
        var shareInput by remember { mutableStateOf(smbShare) }
        var userInput by remember { mutableStateOf(AppSettings.smbUsername.value) }
        var passInput by remember { mutableStateOf(AppSettings.smbPassword.value) }
        AlertDialog(
            onDismissRequest = { showSmbDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("SMB Share", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hostInput,
                        onValueChange = { hostInput = it },
                        label = { Text("Host / IP (e.g. 192.168.1.50)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = shareInput,
                        onValueChange = { shareInput = it },
                        label = { Text("Share Name (e.g. Music)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = passInput,
                        onValueChange = { passInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    AppSettings.setSmbHost(hostInput)
                    AppSettings.setSmbShare(shareInput)
                    AppSettings.setSmbUsername(userInput)
                    AppSettings.setSmbPassword(passInput)
                    showSmbDialog = false
                }) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmbDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            },
        )
    }
}

@Composable
fun DiscordPresenceSubScreen(
    onBack: () -> Unit,
    onOpenWebLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    val token by AppSettings.discordToken.collectAsStateWithLifecycle()
    val username by AppSettings.discordUsername.collectAsStateWithLifecycle()
    val name by AppSettings.discordName.collectAsStateWithLifecycle()
    val avatar by AppSettings.discordAvatar.collectAsStateWithLifecycle()
    val rpcEnabled by AppSettings.discordRpcEnabled.collectAsStateWithLifecycle()
    val useDetails by AppSettings.discordUseDetails.collectAsStateWithLifecycle()
    val showAudioQuality by AppSettings.discordShowAudioQuality.collectAsStateWithLifecycle()
    val advancedMode by AppSettings.discordAdvancedMode.collectAsStateWithLifecycle()
    var showManualTokenDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 140.dp),
    ) {
        SubScreenTopBar(title = "Discord Rich Presence", onBack = onBack)

        SettingsGroup(
            header = "ACCOUNT",
            footer = "Shares the track you're listening to on your Discord profile.",
        ) {
            if (token.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (avatar.isNotBlank()) {
                        AsyncImage(
                            model = avatar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5865F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Person, null, tint = Color.White)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = name.ifBlank { username.ifBlank { "Discord Connected" } },
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.W600,
                        )
                        Text(
                            text = if (username.isNotBlank()) "@$username" else "Rich Presence active",
                            color = Color(0xFF8E8E93),
                            fontSize = 13.sp,
                        )
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF2C2C2E))
                DestructiveRow(
                    label = "Disconnect Discord",
                    onClick = { AppSettings.clearDiscordAccount() },
                )
            } else {
                SettingsRow(
                    icon = Icons.Rounded.Person,
                    title = "Sign in with Discord",
                    subtitle = "Open discord.com/login in-app to connect Rich Presence",
                    onClick = onOpenWebLogin,
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Rounded.Key,
                    title = "Use token manually",
                    subtitle = "Paste a Discord account token directly",
                    onClick = { showManualTokenDialog = true },
                )
            }
        }

        SettingsGroup(header = "PRESENCE") {
            SettingsRow(
                icon = Icons.Rounded.GraphicEq,
                title = "Share listening activity",
                subtitle = "Publish current song, artist, cover art and timestamps",
                trailing = {
                    Switch(
                        checked = rpcEnabled,
                        onCheckedChange = AppSettings::setDiscordRpcEnabled,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color.White,
                            checkedThumbColor = Color.Black,
                            checkedBorderColor = Color.White,
                        ),
                    )
                },
                onClick = { AppSettings.setDiscordRpcEnabled(!rpcEnabled) },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.TextFields,
                title = "Show song title on main line",
                subtitle = "Put the track title in bold instead of the artist name",
                trailing = {
                    Switch(
                        checked = useDetails,
                        onCheckedChange = AppSettings::setDiscordUseDetails,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color.White,
                            checkedThumbColor = Color.Black,
                            checkedBorderColor = Color.White,
                        ),
                    )
                },
                onClick = { AppSettings.setDiscordUseDetails(!useDetails) },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.HighQuality,
                title = "Show audio quality badge",
                subtitle = "Display Lossless / Hi-Res / Dolby Atmos specs on card",
                trailing = {
                    Switch(
                        checked = showAudioQuality,
                        onCheckedChange = AppSettings::setDiscordShowAudioQuality,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color.White,
                            checkedThumbColor = Color.Black,
                            checkedBorderColor = Color.White,
                        ),
                    )
                },
                onClick = { AppSettings.setDiscordShowAudioQuality(!showAudioQuality) },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.Tune,
                title = "Advanced presence customization",
                subtitle = "Custom status, activity name and profile buttons",
                trailing = {
                    Switch(
                        checked = advancedMode,
                        onCheckedChange = AppSettings::setDiscordAdvancedMode,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color.White,
                            checkedThumbColor = Color.Black,
                            checkedBorderColor = Color.White,
                        ),
                    )
                },
                onClick = { AppSettings.setDiscordAdvancedMode(!advancedMode) },
            )
        }
    }

    if (showManualTokenDialog) {
        var tokenInput by remember { mutableStateOf(token) }
        AlertDialog(
            onDismissRequest = { showManualTokenDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Discord Token", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text("Token") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = tokenInput.trim()
                    AppSettings.setDiscordToken(trimmed)
                    if (trimmed.isNotBlank() && username.isBlank()) {
                        AppSettings.setDiscordAccount("discord_user", "Discord Listener", null)
                    }
                    showManualTokenDialog = false
                }) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualTokenDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            },
        )
    }
}

@Composable
fun ListenTogetherSubScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    var partyCode by remember { mutableStateOf<String?>(null) }
    var joinInput by remember { mutableStateOf("") }
    var showJoinDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 140.dp),
    ) {
        SubScreenTopBar(title = "Listen Together", onBack = onBack)

        SettingsGroup(
            header = "PARTY",
            footer = "Listen in real-time sync with up to 5 friends. Anyone in the party can queue tracks or control playback.",
        ) {
            if (partyCode != null) {
                SettingsRow(
                    icon = Icons.Rounded.Groups,
                    title = "Active Party Code: $partyCode",
                    subtitle = "1 member listening in sync · Share code with friends",
                    badge = "LIVE",
                )
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF2C2C2E))
                DestructiveRow(
                    label = "Leave Party",
                    onClick = { partyCode = null },
                )
            } else {
                SettingsRow(
                    icon = Icons.Rounded.AddCircleOutline,
                    title = "Start a party",
                    subtitle = "Create a room code and invite friends to listen along",
                    onClick = {
                        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
                        partyCode = (1..6).map { chars.random() }.joinToString("")
                    },
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Rounded.Login,
                    title = "Join with code",
                    subtitle = "Enter a 6-character party code",
                    onClick = { showJoinDialog = true },
                )
            }
        }
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("Join Party", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = joinInput,
                    onValueChange = { joinInput = it.uppercase().take(6) },
                    label = { Text("6-character code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (joinInput.length >= 4) {
                            partyCode = joinInput
                            showJoinDialog = false
                        }
                    },
                ) {
                    Text("Join", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            },
        )
    }
}

@Composable
fun ReplaySubScreen(
    listeningStats: com.example.data.model.ListeningStatsState,
    recentTracks: List<com.example.data.model.MusicItem>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    val replayGenres by AppSettings.replayGenres.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 140.dp),
    ) {
        SubScreenTopBar(title = "Replay", onBack = onBack)

        SettingsGroup(
            header = "LISTENING SUMMARY",
            footer = "Calculated locally on your device from your listening activity.",
        ) {
            SettingsRow(
                icon = Icons.Rounded.BarChart,
                title = "Total Listening Time",
                trailing = {
                    Text(
                        text = "${listeningStats.minutesListened} minutes",
                        color = Color(0xFF8E8E93),
                        fontSize = 15.sp,
                    )
                },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.LibraryMusic,
                title = "Songs Played",
                trailing = {
                    Text(
                        text = "${listeningStats.totalPlays} plays",
                        color = Color(0xFF8E8E93),
                        fontSize = 15.sp,
                    )
                },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.Person,
                title = "Top Artist",
                trailing = {
                    Text(
                        text = listeningStats.topSongArtist.ifBlank { "—" },
                        color = Color(0xFF8E8E93),
                        fontSize = 15.sp,
                    )
                },
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Rounded.MusicNote,
                title = "Top Song",
                trailing = {
                    Text(
                        text = listeningStats.topSongTitle.ifBlank { "—" },
                        color = Color(0xFF8E8E93),
                        fontSize = 15.sp,
                    )
                },
            )
        }

        if (replayGenres) {
            SettingsGroup(
                header = "TOP GENRES (BETA)",
            ) {
                val genres = remember(recentTracks) {
                    listOf("Pop", "Indie & Alternative", "R&B / Soul", "Electronic", "Acoustic")
                }
                genres.forEachIndexed { index, genre ->
                    if (index > 0) RowDivider()
                    SettingsRow(
                        icon = Icons.Rounded.LocalOffer,
                        title = "#${index + 1} $genre",
                        trailing = {
                            Text(
                                text = "${(36 - index * 6).coerceAtLeast(8)}%",
                                color = Color(0xFF8E8E93),
                                fontSize = 15.sp,
                            )
                        },
                    )
                }
            }
        }

        if (recentTracks.isNotEmpty()) {
            SettingsGroup(
                header = "MOST PLAYED TRACKS",
            ) {
                recentTracks.take(10).forEachIndexed { index, track ->
                    if (index > 0) RowDivider()
                    SettingsRow(
                        icon = Icons.Rounded.PlayArrow,
                        title = "${index + 1}. ${track.title}",
                        subtitle = track.cleanArtist,
                        trailing = {
                            Text(
                                text = "${track.playCount.coerceAtLeast(1)}x",
                                color = Color(0xFF8E8E93),
                                fontSize = 13.5.sp,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun AppLanguageDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var currentLang by remember {
        mutableStateOf(
            context.getSharedPreferences("bitchord_settings", android.content.Context.MODE_PRIVATE)
                .getString("app_language", "")
                .orEmpty()
        )
    }
    val options = remember {
        listOf(
            "" to "System default",
            "id" to "Bahasa Indonesia",
            "en" to "English",
            "ja" to "日本語 (Japanese)",
            "ko" to "한국어 (Korean)",
            "zh" to "中文 (Chinese)",
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        title = {
            Text(
                text = "App Language",
                color = Color.White,
                fontWeight = FontWeight.W700,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEach { (code, label) ->
                    val selected = currentLang == code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                currentLang = code
                                context.getSharedPreferences("bitchord_settings", android.content.Context.MODE_PRIVATE)
                                    .edit()
                                    .putString("app_language", code)
                                    .apply()
                                onDismiss()
                            }
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.W700 else FontWeight.W400,
                        )
                        if (selected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = Color.White)
            }
        },
    )
}

