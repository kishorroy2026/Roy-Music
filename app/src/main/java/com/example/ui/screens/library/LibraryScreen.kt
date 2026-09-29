package com.example.ui.screens.library

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ArtworkShape
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.ui.components.ArtworkImage
import com.example.ui.components.SongListItem

enum class FavoriteSortOrder(val label: String) {
    RECENT("Recently Added"),
    MOST_PLAYED("Most Played"),
    TITLE("Song Title (A-Z)"),
    ARTIST("Artist Name (A-Z)")
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun LibraryScreen(
    allSongs: List<Song>,
    favoriteSongs: List<Song>,
    playlists: List<Playlist>,
    albums: List<Pair<String, List<Song>>>,
    artists: List<Pair<String, List<Song>>>,
    currentSong: Song?,
    isPlaying: Boolean,
    artworkShape: ArtworkShape,
    onSongClick: (Song, List<Song>) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onCreatePlaylist: (String, String) -> Unit,
    onRenamePlaylist: (Long, String, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onPlayPlaylist: (Playlist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSongOption: (Song, String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Songs", "Playlists", "Favorites", "Albums", "Artists")

    var selectedAlbum by remember { mutableStateOf<Pair<String, List<Song>>?>(null) }
    var selectedArtist by remember { mutableStateOf<Pair<String, List<Song>>?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var favoriteSortOrder by remember { mutableStateOf(FavoriteSortOrder.RECENT) }
    var showFavSortMenu by remember { mutableStateOf(false) }

    // If an album is opened
    if (selectedAlbum != null) {
        val (albumName, albumTracks) = selectedAlbum!!
        BackHandler { selectedAlbum = null }
        DetailGroupView(
            title = albumName,
            subtitle = "${albumTracks.size} tracks",
            songs = albumTracks,
            favoriteSongs = favoriteSongs,
            currentSong = currentSong,
            artworkShape = artworkShape,
            onBack = { selectedAlbum = null },
            onSongClick = { s -> onSongClick(s, albumTracks) },
            onPlayAll = { if (albumTracks.isNotEmpty()) onSongClick(albumTracks.first(), albumTracks) },
            onFavoriteToggle = onFavoriteToggle,
            onSongOption = onSongOption
        )
        return
    }

    // If an artist is opened
    if (selectedArtist != null) {
        val (artistName, artistTracks) = selectedArtist!!
        BackHandler { selectedArtist = null }
        DetailGroupView(
            title = artistName,
            subtitle = "${artistTracks.size} songs across ${artistTracks.groupBy { it.album }.size} albums",
            songs = artistTracks,
            favoriteSongs = favoriteSongs,
            currentSong = currentSong,
            artworkShape = artworkShape,
            onBack = { selectedArtist = null },
            onSongClick = { s -> onSongClick(s, artistTracks) },
            onPlayAll = { if (artistTracks.isNotEmpty()) onSongClick(artistTracks.first(), artistTracks) },
            onFavoriteToggle = onFavoriteToggle,
            onSongOption = onSongOption
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        // Library Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Music Library",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row {
                if (selectedTab == 1) { // Playlists tab
                    IconButton(
                        onClick = { showCreatePlaylistDialog = true },
                        modifier = Modifier.testTag("add_playlist_top_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Create playlist")
                    }
                }

                if (selectedTab == 2) { // Favorites tab
                    Box {
                        IconButton(onClick = { showFavSortMenu = true }) {
                            Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort favorites")
                        }
                        DropdownMenu(
                            expanded = showFavSortMenu,
                            onDismissRequest = { showFavSortMenu = false }
                        ) {
                            FavoriteSortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    onClick = {
                                        favoriteSortOrder = order
                                        showFavSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("library_search_btn")
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search library")
                }
            }
        }

        // Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // All Songs
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 110.dp)
                ) {
                    items(allSongs) { song ->
                        SongListItem(
                            song = song,
                            isPlayingThis = currentSong?.id == song.id,
                            isFavorite = favoriteSongs.any { it.id == song.id },
                            artworkShape = artworkShape,
                            onSongClick = { onSongClick(song, allSongs) },
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

            1 -> {
                // Playlists
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (playlists.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "No playlists yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(onClick = { showCreatePlaylistDialog = true }) {
                                        Text("Create Playlist")
                                    }
                                }
                            }
                        }
                    } else {
                        items(playlists) { playlist ->
                            var menuExpanded by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPlaylistClick(playlist) }
                                    .testTag("playlist_item_${playlist.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlaylistPlay,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = playlist.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (playlist.description.isNotBlank()) {
                                            Text(
                                                text = playlist.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(onClick = { onPlayPlaylist(playlist) }) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play playlist",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Box {
                                        IconButton(onClick = { menuExpanded = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Playlist options"
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = menuExpanded,
                                            onDismissRequest = { menuExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Delete Playlist") },
                                                onClick = {
                                                    menuExpanded = false
                                                    onDeletePlaylist(playlist.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Favorites
                val sortedFavorites = remember(favoriteSongs, favoriteSortOrder) {
                    when (favoriteSortOrder) {
                        FavoriteSortOrder.RECENT -> favoriteSongs
                        FavoriteSortOrder.MOST_PLAYED -> favoriteSongs // or by plays
                        FavoriteSortOrder.TITLE -> favoriteSongs.sortedBy { it.title.lowercase() }
                        FavoriteSortOrder.ARTIST -> favoriteSongs.sortedBy { it.displayArtist.lowercase() }
                    }
                }

                if (sortedFavorites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No favorite songs yet.\nSongs you listen to repeatedly (5+ times) will automatically appear here!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 110.dp)
                    ) {
                        items(sortedFavorites) { song ->
                            SongListItem(
                                song = song,
                                isPlayingThis = currentSong?.id == song.id,
                                isFavorite = true,
                                artworkShape = artworkShape,
                                onSongClick = { onSongClick(song, sortedFavorites) },
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

            3 -> {
                // Albums Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(albums) { (albumName, albumTracks) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAlbum = Pair(albumName, albumTracks) }
                                .testTag("album_card_$albumName"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                ArtworkImage(
                                    song = albumTracks.firstOrNull(),
                                    size = 140.dp,
                                    shape = artworkShape
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = albumName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${albumTracks.size} tracks",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            4 -> {
                // Artists List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(artists) { (artistName, artistTracks) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedArtist = Pair(artistName, artistTracks) }
                                .testTag("artist_card_$artistName"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = artistName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${artistTracks.size} songs",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onCreate = { name, desc ->
                onCreatePlaylist(name, desc)
                showCreatePlaylistDialog = false
            },
            onDismiss = { showCreatePlaylistDialog = false }
        )
    }
}

@Composable
private fun DetailGroupView(
    title: String,
    subtitle: String,
    songs: List<Song>,
    favoriteSongs: List<Song>,
    currentSong: Song?,
    artworkShape: ArtworkShape,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onSongOption: (Song, String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = onPlayAll) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play All")
                        }
                    }
                }
            }

            items(songs) { song ->
                SongListItem(
                    song = song,
                    isPlayingThis = currentSong?.id == song.id,
                    isFavorite = favoriteSongs.any { it.id == song.id },
                    artworkShape = artworkShape,
                    onSongClick = { onSongClick(song) },
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

@Composable
private fun CreatePlaylistDialog(
    onCreate: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Playlist", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    placeholder = { Text("e.g. Chill Beats, Gym Pump") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
