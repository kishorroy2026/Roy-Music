package com.example.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ArtworkShape
import com.example.data.model.Mood
import com.example.data.model.Song
import com.example.ui.components.ArtworkImage
import com.example.ui.components.RoyMusicBrandHeader
import com.example.ui.components.SectionHeader
import com.example.ui.components.SongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    allSongs: List<Song>,
    favoriteSongs: List<Song>,
    recentlyPlayed: List<Song>,
    mostPlayed: List<Song>,
    recentlyAdded: List<Song>,
    allMoods: List<Mood>,
    currentSong: Song?,
    isPlaying: Boolean,
    artworkShape: ArtworkShape,
    onSongClick: (Song, List<Song>) -> Unit,
    onMoodClick: (Mood) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onNavigateSearch: () -> Unit,
    onNavigateEqualizer: () -> Unit,
    onRescanClick: () -> Unit,
    onSeeAllFavorites: () -> Unit,
    onSeeAllLibrary: () -> Unit,
    onSongOption: (Song, String) -> Unit,
    onNavigateAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoyMusicBrandHeader()

                Row {
                    IconButton(
                        onClick = onNavigateEqualizer,
                        modifier = Modifier.testTag("home_equalizer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onNavigateSearch,
                        modifier = Modifier.testTag("home_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onNavigateAbout,
                        modifier = Modifier.testTag("home_about_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About & Contact",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onRescanClick,
                        modifier = Modifier.testTag("home_rescan_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rescan library",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Hero Continue Listening Card if a song was played
        if (currentSong != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSongClick(currentSong, allSongs) }
                        .testTag("continue_listening_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ArtworkImage(
                                song = currentSong,
                                size = 64.dp,
                                shape = artworkShape
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CONTINUE LISTENING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentSong.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentSong.displayArtist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            FilledIconButton(
                                onClick = { onSongClick(currentSong, allSongs) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                                    contentDescription = "Play"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Mood Carousel
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                title = "Mood Explorer",
                actionLabel = "All Moods",
                onActionClick = { /* navigated via bottom tab or handler */ }
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allMoods) { mood ->
                    val startColor = runCatching {
                        Color(android.graphics.Color.parseColor(mood.gradientStartHex))
                    }.getOrDefault(MaterialTheme.colorScheme.primary)
                    val endColor = runCatching {
                        Color(android.graphics.Color.parseColor(mood.gradientEndHex))
                    }.getOrDefault(MaterialTheme.colorScheme.secondary)

                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(startColor, endColor)))
                            .clickable { onMoodClick(mood) }
                            .padding(10.dp)
                            .testTag("home_mood_chip_${mood.id}"),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = mood.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Favorites Section
        if (favoriteSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Your Favorites",
                    count = favoriteSongs.size,
                    actionLabel = "See All",
                    onActionClick = onSeeAllFavorites
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteSongs.take(10)) { song ->
                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { onSongClick(song, favoriteSongs) }
                                .testTag("fav_card_${song.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                ArtworkImage(
                                    song = song,
                                    size = 120.dp,
                                    shape = artworkShape
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.displayArtist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Most Played Section
        if (mostPlayed.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Most Played",
                    count = mostPlayed.size
                )
            }
            items(mostPlayed.take(5)) { song ->
                SongListItem(
                    song = song,
                    isPlayingThis = currentSong?.id == song.id,
                    isFavorite = favoriteSongs.any { it.id == song.id },
                    artworkShape = artworkShape,
                    onSongClick = { onSongClick(song, mostPlayed) },
                    onFavoriteClick = { onFavoriteToggle(song) },
                    onPlayNext = { onSongOption(song, "PLAY_NEXT") },
                    onAddToQueue = { onSongOption(song, "ADD_QUEUE") },
                    onAddToPlaylist = { onSongOption(song, "ADD_PLAYLIST") },
                    onAssignMood = { onSongOption(song, "ASSIGN_MOOD") },
                    onSongInfo = { onSongOption(song, "INFO") }
                )
            }
        }

        // Recently Added Section
        if (recentlyAdded.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Recently Added",
                    actionLabel = "See All",
                    onActionClick = onSeeAllLibrary
                )
            }
            items(recentlyAdded.take(5)) { song ->
                SongListItem(
                    song = song,
                    isPlayingThis = currentSong?.id == song.id,
                    isFavorite = favoriteSongs.any { it.id == song.id },
                    artworkShape = artworkShape,
                    onSongClick = { onSongClick(song, recentlyAdded) },
                    onFavoriteClick = { onFavoriteToggle(song) },
                    onPlayNext = { onSongOption(song, "PLAY_NEXT") },
                    onAddToQueue = { onSongOption(song, "ADD_QUEUE") },
                    onAddToPlaylist = { onSongOption(song, "ADD_PLAYLIST") },
                    onAssignMood = { onSongOption(song, "ASSIGN_MOOD") },
                    onSongInfo = { onSongOption(song, "INFO") }
                )
            }
        }
    }
}
