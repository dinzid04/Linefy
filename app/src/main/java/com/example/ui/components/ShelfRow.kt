package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.BitChordSong
import com.example.data.model.CARD_ART_PX
import com.example.data.model.HomeShelf
import com.example.data.model.ROW_ART_PX
import com.example.data.model.ShelfItem
import com.example.data.model.artworkAt
import com.example.data.settings.AppSettings
import com.example.ui.icons.BitChordIcons

private val CardShape = RoundedCornerShape(10.dp)
private val TrackArtShape = RoundedCornerShape(6.dp)

@Composable
fun ShelfBlock(
    shelf: HomeShelf,
    onPlayTrack: (BitChordSong, List<BitChordSong>) -> Unit,
    onOpenCard: (ShelfItem.Card) -> Unit,
    onSeeAll: ((HomeShelf) -> Unit)? = null,
    onSongLongClick: ((BitChordSong) -> Unit)? = null,
    onCardLongClick: ((ShelfItem.Card) -> Unit)? = null
) {
    val tracks = remember(shelf.items) {
        shelf.items.filterIsInstance<ShelfItem.Track>().map { it.song }
    }
    val cards = remember(shelf.items) {
        shelf.items.filterIsInstance<ShelfItem.Card>()
    }

    Column(modifier = Modifier.padding(top = 20.dp)) {
        val canSeeAll = onSeeAll != null && (shelf.browseId != null || shelf.items.size > 4)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (canSeeAll) Modifier.clickable { onSeeAll?.invoke(shelf) }
                    else Modifier
                )
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (!shelf.strapline.isNullOrBlank()) {
                    Text(
                        text = shelf.strapline.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF98989F)
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = shelf.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (canSeeAll) {
                        Icon(
                            imageVector = BitChordIcons.ChevronRight,
                            contentDescription = "See all",
                            tint = Color(0xFF98989F),
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        when {
            shelf.isQuickPicks && tracks.isNotEmpty() -> {
                QuickPicksGrid(
                    tracks = tracks,
                    onPlay = { song -> onPlayTrack(song, tracks) },
                    onMore = onSongLongClick
                )
            }
            cards.isNotEmpty() && tracks.isEmpty() -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(
                        items = cards,
                        key = { idx, card -> "${idx}_${card.key}" },
                        contentType = { _, _ -> "card" }
                    ) { _, card ->
                        MediaCard(
                            card = card,
                            onClick = { onOpenCard(card) },
                            onLongClick = onCardLongClick?.let { cb -> { cb(card) } }
                        )
                    }
                }
            }
            tracks.isNotEmpty() && cards.isEmpty() -> {
                QuickPicksGrid(
                    tracks = tracks,
                    onPlay = { song -> onPlayTrack(song, tracks) },
                    onMore = onSongLongClick
                )
            }
            else -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(
                        items = shelf.items,
                        key = { idx, item -> "${idx}_${item.key}" },
                        contentType = { _, item ->
                            when (item) {
                                is ShelfItem.Card -> "card"
                                is ShelfItem.Track -> "track_tile"
                            }
                        }
                    ) { _, item ->
                        when (item) {
                            is ShelfItem.Card -> MediaCard(
                                card = item,
                                onClick = { onOpenCard(item) },
                                onLongClick = onCardLongClick?.let { cb -> { cb(item) } }
                            )
                            is ShelfItem.Track -> TrackTile(
                                song = item.song,
                                onClick = { onPlayTrack(item.song, tracks) },
                                onLongClick = onSongLongClick?.let { cb -> { cb(item.song) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPicksGrid(
    tracks: List<BitChordSong>,
    onPlay: (BitChordSong) -> Unit,
    onMore: ((BitChordSong) -> Unit)? = null
) {
    val fullGlass by AppSettings.fullLiquidGlassUi.collectAsStateWithLifecycle()
    val columns = remember(tracks) { tracks.chunked(4) }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            count = columns.size,
            key = { i -> columns[i].firstOrNull()?.id ?: i },
            contentType = { "quick_picks_col" }
        ) { colIdx ->
            val col = columns[colIdx]
            Column(
                modifier = Modifier
                    .width(if (fullGlass) 316.dp else 300.dp)
                    .lyraGlassCard(
                        shape = RoundedCornerShape(20.dp),
                        fallbackColor = Color.Transparent,
                    )
                    .then(
                        if (fullGlass) {
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                col.forEachIndexed { idx, song ->
                    QuickPickRow(
                        song = song,
                        onClick = { onPlay(song) },
                        onMore = onMore?.let { cb -> { cb(song) } }
                    )
                    if (fullGlass && idx < col.lastIndex) {
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.16f),
                            thickness = 0.8.dp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickPickRow(
    song: BitChordSong,
    onClick: () -> Unit,
    onMore: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onMore
            )
            .padding(vertical = 5.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(TrackArtShape)
                .background(Color(0xFF232329))
        ) {
            AsyncImage(
                model = song.artworkUrl.artworkAt(ROW_ART_PX),
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = listOfNotNull(song.artist, song.album).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF98989F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onMore != null) {
            IconButton(
                onClick = onMore,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = BitChordIcons.MoreVert,
                    contentDescription = "More options",
                    tint = Color(0xFF98989F),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaCard(
    card: ShelfItem.Card,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val fullGlass by AppSettings.fullLiquidGlassUi.collectAsStateWithLifecycle()
    val shape = if (card.isCircular) CircleShape else CardShape
    Column(
        modifier = Modifier
            .width(160.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        horizontalAlignment = if (card.isCircular) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(Color(0xFF232329))
                .then(
                    if (fullGlass) {
                        Modifier.border(1.dp, Color.White.copy(alpha = 0.24f), shape)
                    } else {
                        Modifier
                    }
                )
        ) {
            AsyncImage(
                model = card.artworkUrl.artworkAt(CARD_ART_PX),
                contentDescription = card.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = card.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (card.subtitle.isNotBlank()) {
            Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF98989F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackTile(
    song: BitChordSong,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val fullGlass by AppSettings.fullLiquidGlassUi.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .width(160.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CardShape)
                .background(Color(0xFF232329))
                .then(
                    if (fullGlass) {
                        Modifier.border(1.dp, Color.White.copy(alpha = 0.24f), CardShape)
                    } else {
                        Modifier
                    }
                )
        ) {
            AsyncImage(
                model = song.artworkUrl.artworkAt(CARD_ART_PX),
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF98989F),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
