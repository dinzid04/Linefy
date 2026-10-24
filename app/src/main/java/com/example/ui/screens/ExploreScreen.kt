package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.innertube.YtMusicRepository
import com.example.data.model.BitChordSong
import com.example.data.model.CARD_ART_PX
import com.example.data.model.HomeShelf
import com.example.data.model.MoodGenre
import com.example.data.model.MoodGenreSection
import com.example.data.model.ShelfItem
import com.example.data.model.UiState
import com.example.data.model.artworkAt
import com.example.data.settings.AppSettings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LyrafyLogoLoadingIndicator
import com.example.ui.components.ShelfBlock
import com.example.ui.components.floatingtabbar.FloatingTabBarScrollConnection
import com.example.ui.components.shimmerEffect
import com.example.ui.icons.BitChordIcons
import com.example.ui.theme.rememberDominantAccentColor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

@Composable
fun ExploreScreen(
    repo: YtMusicRepository,
    scrollConnection: FloatingTabBarScrollConnection? = null,
    activeSongArtworkUrl: String? = null,
    onOpenMoodGenre: (MoodGenre) -> Unit,
    onPlaySong: (BitChordSong, List<BitChordSong>) -> Unit,
    onOpenCard: (ShelfItem.Card) -> Unit
) {
    var state by remember { mutableStateOf<UiState<List<MoodGenreSection>>>(UiState.Loading) }
    var exploreShelves by remember { mutableStateOf<List<HomeShelf>>(emptyList()) }
    val artworkByKey = remember { mutableStateMapOf<String, String>() }
    val country by AppSettings.contentCountry.collectAsState()
    val language by AppSettings.contentLanguage.collectAsState()
    val useDynamicMusicTheme by AppSettings.useDynamicMusicTheme.collectAsStateWithLifecycle()

    val dominantColor = rememberDominantAccentColor(
        imageUrl = if (useDynamicMusicTheme) activeSongArtworkUrl else null,
        defaultColor = Color.Black,
    )
    val animatedTopAmbient by animateColorAsState(
        targetValue = if (useDynamicMusicTheme && !activeSongArtworkUrl.isNullOrBlank()) {
            dominantColor.copy(alpha = 0.34f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 600),
        label = "explore_dynamic_top",
    )

    LaunchedEffect(country, language) {
        state = UiState.Loading
        artworkByKey.clear()
        val sectionsResult = runCatching { repo.moodsAndGenres() }
        val shelvesResult = runCatching { repo.exploreShelves() }
        exploreShelves = shelvesResult.getOrDefault(emptyList())
        val sections = sectionsResult.getOrNull().orEmpty()
        state = if (sections.isNotEmpty() || exploreShelves.isNotEmpty()) {
            UiState.Content(sections)
        } else {
            UiState.Error(sectionsResult.exceptionOrNull()?.message ?: "Failed to load Explore")
        }

        val allItems = sections.flatMap { it.items }
        val gate = Semaphore(4)
        coroutineScope {
            allItems.map { item ->
                async {
                    val key = "${item.browseId}:${item.params.orEmpty()}"
                    if (!item.artworkUrl.isNullOrBlank()) {
                        artworkByKey[key] = item.artworkUrl
                    } else {
                        gate.withPermit {
                            val art = repo.moodGenreArtwork(item.browseId, item.params)
                            if (!art.isNullOrBlank()) {
                                artworkByKey[key] = art
                            }
                        }
                    }
                }
            }.awaitAll()
        }
    }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .background(
                Brush.verticalGradient(
                    colors = listOf(animatedTopAmbient, Color.Black, Color.Black),
                ),
            )
            .testTag("explore_screen")
    ) {
        when (val s = state) {
            UiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topInset + 28.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LyrafyLogoLoadingIndicator(size = 56.dp)
                    Spacer(modifier = Modifier.height(20.dp))
                    repeat(4) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.72f)
                                    .shimmerEffect(RoundedCornerShape(18.dp)),
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.72f)
                                    .shimmerEffect(RoundedCornerShape(18.dp)),
                            )
                        }
                    }
                }
            }
            is UiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = s.message,
                        color = Color(0xFF98989F),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is UiState.Content -> {
                val allItems = remember(s.data) { s.data.flatMap { it.items } }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = topInset + 12.dp,
                        bottom = 180.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (scrollConnection != null) Modifier.nestedScroll(scrollConnection) else Modifier)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = if (language.startsWith("id", ignoreCase = true)) "Jelajahi" else "Explore",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        )
                    }

                    itemsIndexed(
                        items = allItems,
                        key = { idx, item -> "$idx:${item.browseId}:${item.params}:${item.title}" }
                    ) { index, item ->
                        val key = "${item.browseId}:${item.params.orEmpty()}"
                        MoodGenreSleeve(
                            item = item,
                            index = index,
                            artworkUrl = artworkByKey[key] ?: item.artworkUrl,
                            onClick = { onOpenMoodGenre(item) }
                        )
                    }

                    if (exploreShelves.isNotEmpty()) {
                        items(
                            count = exploreShelves.size,
                            span = { GridItemSpan(maxLineSpan) },
                            key = { i -> "shelf:$i:${exploreShelves[i].title}" }
                        ) { idx ->
                            Box(modifier = Modifier.padding(horizontal = 0.dp)) {
                                ShelfBlock(
                                    shelf = exploreShelves[idx],
                                    onPlayTrack = onPlaySong,
                                    onOpenCard = onOpenCard
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class SleevePalette(val band: Color, val accent: Color)

private val SLEEVE_PALETTES = listOf(
    SleevePalette(Color(0xFF2878C8), Color(0xFFFF7A3D)),
    SleevePalette(Color(0xFFD63B2F), Color(0xFFF6C445)),
    SleevePalette(Color(0xFF1F9D6A), Color(0xFFF2D16B)),
    SleevePalette(Color(0xFF7B4AE2), Color(0xFFFF6B9A)),
    SleevePalette(Color(0xFFE07A1F), Color(0xFF2EC4B6)),
    SleevePalette(Color(0xFFBA2D7B), Color(0xFFFFB703)),
    SleevePalette(Color(0xFF2A9D8F), Color(0xFFE76F51)),
    SleevePalette(Color(0xFF5A67D8), Color(0xFFF6AD55)),
    SleevePalette(Color(0xFFC84B31), Color(0xFF90BE6D)),
    SleevePalette(Color(0xFF3A86FF), Color(0xFFFFBE0B))
)

private fun paletteFor(item: MoodGenre, index: Int): SleevePalette {
    val base = SLEEVE_PALETTES[index % SLEEVE_PALETTES.size]
    val stripe = item.stripeColor?.let { Color(it or 0xFF000000L) }
    return if (stripe != null) SleevePalette(band = stripe, accent = base.accent) else base
}

private fun duotoneFilter(shadow: Color, highlight: Color): ColorFilter {
    val sr = shadow.red
    val sg = shadow.green
    val sb = shadow.blue
    val dr = highlight.red - sr
    val dg = highlight.green - sg
    val db = highlight.blue - sb
    val m = floatArrayOf(
        0.299f * dr, 0.587f * dr, 0.114f * dr, 0f, sr * 255f,
        0.299f * dg, 0.587f * dg, 0.114f * dg, 0f, sg * 255f,
        0.299f * db, 0.587f * db, 0.114f * db, 0f, sb * 255f,
        0f, 0f, 0f, 1f, 0f
    )
    return ColorFilter.colorMatrix(ColorMatrix(m))
}

@Composable
private fun MoodGenreSleeve(
    item: MoodGenre,
    index: Int,
    artworkUrl: String?,
    onClick: () -> Unit
) {
    val palette = remember(item.stripeColor, index) { paletteFor(item, index) }
    val duotone = remember(palette) {
        val shadow = Color(
            red = palette.band.red * 0.35f,
            green = palette.band.green * 0.35f,
            blue = palette.band.blue * 0.35f,
            alpha = 1f
        )
        duotoneFilter(shadow = shadow, highlight = palette.accent)
    }

    val fullLiquidGlassUi by AppSettings.fullLiquidGlassUi.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.72f)
            .clip(RoundedCornerShape(18.dp))
            .background(palette.band)
            .then(
                if (fullLiquidGlassUi) {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.24f),
                        shape = RoundedCornerShape(18.dp),
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .testTag("mood_genre_card_${index}")
    ) {
        // Right-aligned duotone artwork (0.72f width) blended seamlessly into palette.band
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.72f)
                .fillMaxHeight()
                .background(
                    Brush.linearGradient(
                        colors = listOf(palette.band, palette.accent)
                    )
                )
        ) {
            if (!artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = artworkUrl.artworkAt(CARD_ART_PX),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = duotone,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Horizontal fade mask from 0.0f (solid palette.band) to 0.50f (transparent) eliminating the vertical seam
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0.0f to palette.band,
                            0.18f to palette.band.copy(alpha = 0.88f),
                            0.36f to palette.band.copy(alpha = 0.35f),
                            0.52f to Color.Transparent
                        )
                    )
            )
        }

        // Top-left text protection scrim (0.65f opacity) so white category titles remain crisp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        0.0f to palette.band.copy(alpha = 0.65f),
                        0.45f to palette.band.copy(alpha = 0.25f),
                        0.75f to Color.Transparent,
                        start = Offset(0f, 0f),
                        end = Offset(420f, 260f)
                    )
                )
        )

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.45f),
                    offset = Offset(0f, 2f),
                    blurRadius = 6f
                )
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 14.dp, top = 12.dp, end = 12.dp)
        )
    }
}

@Composable
fun MoodGenrePlaylistsScreen(
    title: String,
    browseId: String,
    params: String?,
    repo: YtMusicRepository,
    onBack: () -> Unit,
    onPlaySong: (BitChordSong, List<BitChordSong>) -> Unit,
    onOpenCard: (ShelfItem.Card) -> Unit
) {
    var state by remember(browseId, params) {
        mutableStateOf<UiState<List<HomeShelf>>>(UiState.Loading)
    }

    LaunchedEffect(browseId, params) {
        state = UiState.Loading
        state = runCatching { repo.moodGenreShelves(browseId, params) }
            .fold(
                onSuccess = { UiState.Content(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load category") }
            )
    }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = topInset + 8.dp,
                    start = 12.dp,
                    end = 16.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1E))
                    .testTag("mood_genre_back_button")
            ) {
                Icon(
                    imageVector = BitChordIcons.Back,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        when (val s = state) {
            UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LyrafyLogoLoadingIndicator(size = 56.dp)
                }
            }
            is UiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = s.message,
                        color = Color(0xFF98989F),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is UiState.Content -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 180.dp)
                ) {
                    items(
                        items = s.data,
                        key = { "${it.title}:${it.browseId}" }
                    ) { shelf ->
                        ShelfBlock(
                            shelf = shelf,
                            onPlayTrack = onPlaySong,
                            onOpenCard = onOpenCard
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}
