package com.example.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.RoyMusicApplication
import com.example.data.model.FolderInfo
import com.example.data.model.Mood
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.model.SongStats
import com.example.data.preferences.UserPreferences
import com.example.ui.components.MiniPlayer
import com.example.ui.components.SongListItem
import com.example.ui.navigation.RoyBottomNav
import com.example.ui.navigation.Screen
import com.example.ui.screens.about.AboutContactScreen
import com.example.ui.screens.equalizer.EqualizerScreen
import com.example.ui.screens.folders.FoldersScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.library.SearchScreen
import com.example.ui.screens.moods.MoodDetailScreen
import com.example.ui.screens.moods.MoodsScreen
import com.example.ui.screens.player.FullPlayerBottomSheet
import com.example.ui.screens.player.MoodTagDialog
import com.example.ui.screens.player.SongDetailsSheet
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.RoyMusicTheme
import com.example.util.NotificationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoyMusicApp() {
    val context = LocalContext.current
    val app = context.applicationContext as RoyMusicApplication
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State collections from Application Container
    val userPreferences by app.userPreferencesRepository.userPreferencesFlow
        .collectAsStateWithLifecycle(initialValue = UserPreferences())

    val allSongs by app.musicRepository.allSongs.collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteSongs by app.musicRepository.favoriteSongs.collectAsStateWithLifecycle(initialValue = emptyList())
    val mostPlayed by app.musicRepository.mostPlayedSongs.collectAsStateWithLifecycle(initialValue = emptyList())
    val recentlyPlayed by app.musicRepository.recentlyPlayedSongs.collectAsStateWithLifecycle(initialValue = emptyList())
    val recentlyAdded by app.musicRepository.recentlyAddedSongs.collectAsStateWithLifecycle(initialValue = emptyList())
    val allMoods by app.musicRepository.allMoods.collectAsStateWithLifecycle(initialValue = emptyList())
    val allPlaylists by app.musicRepository.allPlaylists.collectAsStateWithLifecycle(initialValue = emptyList())
    val albums by app.musicRepository.albums.collectAsStateWithLifecycle(initialValue = emptyList())
    val artists by app.musicRepository.artists.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by app.musicRepository.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val customSoundProfiles by app.userPreferencesRepository.customSoundProfilesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    // Playback state
    val currentSong by app.playbackController.currentSong.collectAsStateWithLifecycle()
    val isPlaying by app.playbackController.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by app.playbackController.currentPosition.collectAsStateWithLifecycle()
    val duration by app.playbackController.duration.collectAsStateWithLifecycle()
    val repeatMode by app.playbackController.repeatMode.collectAsStateWithLifecycle()
    val isShuffle by app.playbackController.isShuffle.collectAsStateWithLifecycle()
    val playbackSpeed by app.playbackController.playbackSpeed.collectAsStateWithLifecycle()
    val sleepTimerSeconds by app.playbackController.sleepTimerSecondsLeft.collectAsStateWithLifecycle()
    val queue by app.playbackController.queue.collectAsStateWithLifecycle()
    val queueIndex by app.playbackController.queueIndex.collectAsStateWithLifecycle()

    // Navigation and sub-screen state
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }
    var selectedMoodForDetail by remember { mutableStateOf<Mood?>(null) }
    var selectedPlaylistForDetail by remember { mutableStateOf<Playlist?>(null) }
    var showSearchScreen by remember { mutableStateOf(false) }
    var showEqualizerScreen by remember { mutableStateOf(false) }
    var showAboutContactScreen by remember { mutableStateOf(false) }
    var isFullPlayerExpanded by remember { mutableStateOf(false) }

    // Dialog state for contextual actions
    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var songForMoodAssignment by remember { mutableStateOf<Song?>(null) }
    var songForDetails by remember { mutableStateOf<Song?>(null) }

    // Dynamic stats and moods for currently playing or selected song
    var activeSongStats by remember { mutableStateOf<SongStats?>(null) }
    var activeSongMoods by remember { mutableStateOf<List<Mood>>(emptyList()) }

    LaunchedEffect(currentSong?.id) {
        val songId = currentSong?.id
        if (songId != null) {
            app.musicRepository.getStatsForSong(songId).collect { activeSongStats = it }
        } else {
            activeSongStats = null
        }
    }

    LaunchedEffect(currentSong?.id) {
        val songId = currentSong?.id
        if (songId != null) {
            app.musicRepository.getMoodsForSong(songId).collect { activeSongMoods = it }
        } else {
            activeSongMoods = emptyList()
        }
    }

    // Permission launcher for audio and notifications
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val audioGranted = results[Manifest.permission.READ_MEDIA_AUDIO] == true ||
                results[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        if (audioGranted) {
            scope.launch {
                app.musicRepository.rescanLibrary(context)
                snackbarHostState.showSnackbar("Library scanned successfully")
            }
        }
        if (results[Manifest.permission.POST_NOTIFICATIONS] == true) {
            NotificationHelper.showWelcomeNotification(context)
        }
    }

    LaunchedEffect(Unit) {
        NotificationHelper.showWelcomeNotification(context)
        permissionLauncher.launch(permissionsToRequest)
    }

    // Song options action dispatcher
    fun handleSongOption(song: Song, option: String) {
        when (option) {
            "PLAY_NEXT" -> {
                app.playbackController.addToQueueNext(song)
                scope.launch { snackbarHostState.showSnackbar("Playing next: ${song.title}") }
            }
            "ADD_QUEUE" -> {
                app.playbackController.addToQueueEnd(song)
                scope.launch { snackbarHostState.showSnackbar("Added to queue: ${song.title}") }
            }
            "ADD_PLAYLIST" -> {
                songForAddToPlaylist = song
            }
            "ASSIGN_MOOD" -> {
                songForMoodAssignment = song
            }
            "INFO" -> {
                songForDetails = song
            }
        }
    }

    RoyMusicTheme(
        selectedTheme = userPreferences.theme,
        isDarkMode = userPreferences.isDarkMode,
        isSystemTheme = userPreferences.isSystemTheme
    ) {
        Scaffold(
            bottomBar = {
                Column {
                    // Persistent Mini Player above bottom bar
                    MiniPlayer(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        style = userPreferences.miniPlayerStyle,
                        artworkShape = userPreferences.artworkShape,
                        onPlayerClick = { isFullPlayerExpanded = true },
                        onPlayPauseClick = { app.playbackController.playPause() },
                        onSkipNextClick = { app.playbackController.skipNext() }
                    )

                    RoyBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            currentRoute = route
                            selectedMoodForDetail = null
                            selectedPlaylistForDetail = null
                            showSearchScreen = false
                            showEqualizerScreen = false
                            showAboutContactScreen = false
                        }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Secondary / Overlay Screens
                when {
                    showAboutContactScreen -> {
                        AboutContactScreen(
                            onBack = { showAboutContactScreen = false }
                        )
                    }

                    showSearchScreen -> {
                        SearchScreen(
                            allSongs = allSongs,
                            allMoods = allMoods,
                            favoriteSongs = favoriteSongs,
                            currentSong = currentSong,
                            artworkShape = userPreferences.artworkShape,
                            onBack = { showSearchScreen = false },
                            onSongClick = { song -> app.playbackController.playSong(song, allSongs) },
                            onFavoriteToggle = { song -> scope.launch { app.musicRepository.toggleManualFavorite(song.id) } },
                            onSongOption = ::handleSongOption
                        )
                    }

                    showEqualizerScreen -> {
                        EqualizerScreen(
                            audioEffectsManager = app.playbackController.audioEffectsManager,
                            initialEnabled = userPreferences.equalizerEnabled,
                            customProfiles = customSoundProfiles,
                            onSaveCustomProfile = { profile ->
                                scope.launch {
                                    app.userPreferencesRepository.saveCustomSoundProfile(profile)
                                    snackbarHostState.showSnackbar("Sound profile \"${profile.name}\" saved!")
                                }
                            },
                            onDeleteCustomProfile = { profileId ->
                                scope.launch {
                                    app.userPreferencesRepository.deleteCustomSoundProfile(profileId)
                                    snackbarHostState.showSnackbar("Profile removed")
                                }
                            },
                            onBack = { showEqualizerScreen = false },
                            onSavePreferences = { enabled, preset, bass, virt ->
                                scope.launch {
                                    app.userPreferencesRepository.setEqualizerEnabled(enabled)
                                    app.userPreferencesRepository.setEqualizerPreset(preset)
                                    app.userPreferencesRepository.setBassBoostStrength(bass)
                                    app.userPreferencesRepository.setVirtualizerStrength(virt)
                                }
                            }
                        )
                    }

                    selectedMoodForDetail != null -> {
                        val mood = selectedMoodForDetail!!
                        val moodSongs by app.musicRepository.getSongsForMood(mood.id).collectAsStateWithLifecycle(initialValue = emptyList())

                        MoodDetailScreen(
                            mood = mood,
                            songs = moodSongs,
                            favoriteSongs = favoriteSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            artworkShape = userPreferences.artworkShape,
                            onBack = { selectedMoodForDetail = null },
                            onPlayAll = { if (moodSongs.isNotEmpty()) app.playbackController.playSong(moodSongs.first(), moodSongs) },
                            onShuffle = {
                                if (moodSongs.isNotEmpty()) {
                                    val shuffled = moodSongs.shuffled()
                                    app.playbackController.playSong(shuffled.first(), shuffled)
                                }
                            },
                            onSongClick = { song -> app.playbackController.playSong(song, moodSongs) },
                            onFavoriteToggle = { song -> scope.launch { app.musicRepository.toggleManualFavorite(song.id) } },
                            onDeleteMood = {
                                scope.launch {
                                    app.musicRepository.deleteCustomMood(mood.id)
                                    selectedMoodForDetail = null
                                }
                            },
                            onSongOption = ::handleSongOption
                        )
                    }

                    selectedPlaylistForDetail != null -> {
                        val playlist = selectedPlaylistForDetail!!
                        val playlistSongs by app.musicRepository.getSongsForPlaylist(playlist.id).collectAsStateWithLifecycle(initialValue = emptyList())

                        PlaylistDetailView(
                            playlist = playlist,
                            songs = playlistSongs,
                            favoriteSongs = favoriteSongs,
                            currentSong = currentSong,
                            artworkShape = userPreferences.artworkShape,
                            onBack = { selectedPlaylistForDetail = null },
                            onPlayAll = { if (playlistSongs.isNotEmpty()) app.playbackController.playSong(playlistSongs.first(), playlistSongs) },
                            onSongClick = { song -> app.playbackController.playSong(song, playlistSongs) },
                            onRemoveSong = { song -> scope.launch { app.musicRepository.removeSongFromPlaylist(playlist.id, song.id) } },
                            onFavoriteToggle = { song -> scope.launch { app.musicRepository.toggleManualFavorite(song.id) } },
                            onSongOption = ::handleSongOption
                        )
                    }

                    else -> {
                        // Main Bottom Tabs
                        Crossfade(targetState = currentRoute, label = "tab_crossfade") { route ->
                            when (route) {
                                Screen.Home.route -> {
                                    HomeScreen(
                                        allSongs = allSongs,
                                        favoriteSongs = favoriteSongs,
                                        recentlyPlayed = recentlyPlayed,
                                        mostPlayed = mostPlayed,
                                        recentlyAdded = recentlyAdded,
                                        allMoods = allMoods,
                                        currentSong = currentSong,
                                        isPlaying = isPlaying,
                                        artworkShape = userPreferences.artworkShape,
                                        onSongClick = { song, queue -> app.playbackController.playSong(song, queue) },
                                        onMoodClick = { mood -> selectedMoodForDetail = mood },
                                        onFavoriteToggle = { song -> scope.launch { app.musicRepository.toggleManualFavorite(song.id) } },
                                        onNavigateSearch = { showSearchScreen = true },
                                        onNavigateEqualizer = { showEqualizerScreen = true },
                                        onRescanClick = {
                                            scope.launch {
                                                app.musicRepository.rescanLibrary(context)
                                                snackbarHostState.showSnackbar("Library rescanned")
                                            }
                                        },
                                        onSeeAllFavorites = { currentRoute = Screen.Library.route },
                                        onSeeAllLibrary = { currentRoute = Screen.Library.route },
                                        onSongOption = ::handleSongOption,
                                        onNavigateAbout = { showAboutContactScreen = true }
                                    )
                                }

                                Screen.Moods.route -> {
                                    MoodsScreen(
                                        moods = allMoods,
                                        onMoodClick = { mood -> selectedMoodForDetail = mood },
                                        onCreateCustomMood = { name, desc, startHex, endHex ->
                                            scope.launch {
                                                app.musicRepository.createCustomMood(name, desc, "star", startHex, endHex)
                                                snackbarHostState.showSnackbar("Mood \"$name\" created!")
                                            }
                                        }
                                    )
                                }

                                Screen.Folders.route -> {
                                    FoldersScreen(
                                        folders = folders,
                                        favoriteSongs = favoriteSongs,
                                        currentSong = currentSong,
                                        isPlaying = isPlaying,
                                        artworkShape = userPreferences.artworkShape,
                                        onPlayFolder = { folder ->
                                            if (folder.songs.isNotEmpty()) app.playbackController.playSong(folder.songs.first(), folder.songs)
                                        },
                                        onShuffleFolder = { folder ->
                                            if (folder.songs.isNotEmpty()) {
                                                val shuffled = folder.songs.shuffled()
                                                app.playbackController.playSong(shuffled.first(), shuffled)
                                            }
                                        },
                                        onAddFolderToPlaylist = { folder ->
                                            if (allPlaylists.isNotEmpty()) {
                                                scope.launch {
                                                    app.musicRepository.addFolderToPlaylist(allPlaylists.first().id, folder.path)
                                                    snackbarHostState.showSnackbar("Added ${folder.name} to ${allPlaylists.first().name}")
                                                }
                                            } else {
                                                scope.launch {
                                                    val newId = app.musicRepository.createPlaylist(folder.name, "Folder Playlist")
                                                    app.musicRepository.addFolderToPlaylist(newId, folder.path)
                                                    snackbarHostState.showSnackbar("Created playlist: ${folder.name}")
                                                }
                                            }
                                        },
                                        onSongClick = { song, q -> app.playbackController.playSong(song, q) },
                                        onFavoriteToggle = { song -> scope.launch { app.musicRepository.toggleManualFavorite(song.id) } },
                                        onSongOption = ::handleSongOption
                                    )
                                }

                                Screen.Library.route -> {
                                    LibraryScreen(
                                        allSongs = allSongs,
                                        favoriteSongs = favoriteSongs,
                                        playlists = allPlaylists,
                                        albums = albums,
                                        artists = artists,
                                        currentSong = currentSong,
                                        isPlaying = isPlaying,
                                        artworkShape = userPreferences.artworkShape,
                                        onSongClick = { song, queue -> app.playbackController.playSong(song, queue) },
                                        onFavoriteToggle = { song -> scope.launch { app.musicRepository.toggleManualFavorite(song.id) } },
                                        onCreatePlaylist = { name, desc ->
                                            scope.launch {
                                                app.musicRepository.createPlaylist(name, desc)
                                                snackbarHostState.showSnackbar("Playlist \"$name\" created!")
                                            }
                                        },
                                        onRenamePlaylist = { id, name, desc ->
                                            scope.launch { app.musicRepository.renamePlaylist(id, name, desc) }
                                        },
                                        onDeletePlaylist = { id ->
                                            scope.launch {
                                                app.musicRepository.deletePlaylist(id)
                                                snackbarHostState.showSnackbar("Playlist removed")
                                            }
                                        },
                                        onPlayPlaylist = { pl ->
                                            scope.launch {
                                                val songs = app.musicRepository.getSongsForPlaylist(pl.id)
                                                // play
                                            }
                                        },
                                        onPlaylistClick = { pl -> selectedPlaylistForDetail = pl },
                                        onSongOption = ::handleSongOption,
                                        onSearchClick = { showSearchScreen = true }
                                    )
                                }

                                Screen.Settings.route -> {
                                    SettingsScreen(
                                        userPreferences = userPreferences,
                                        songCount = allSongs.size,
                                        onThemeChange = { theme -> scope.launch { app.userPreferencesRepository.setTheme(theme) } },
                                        onArtworkShapeChange = { shape -> scope.launch { app.userPreferencesRepository.setArtworkShape(shape) } },
                                        onMiniPlayerStyleChange = { style -> scope.launch { app.userPreferencesRepository.setMiniPlayerStyle(style) } },
                                        onDarkModeToggle = { dark -> scope.launch { app.userPreferencesRepository.setDarkMode(dark) } },
                                        onAutoFavoriteToggle = { enabled -> scope.launch { app.userPreferencesRepository.setAutoFavoriteEnabled(enabled) } },
                                        onAutoFavThresholdChange = { th -> scope.launch { app.userPreferencesRepository.setAutoFavoriteThreshold(th) } },
                                        onAutoRefavoriteToggle = { allow -> scope.launch { app.userPreferencesRepository.setAutoRefavoriteAllowed(allow) } },
                                        onRescanClick = {
                                            scope.launch {
                                                app.musicRepository.rescanLibrary(context)
                                                snackbarHostState.showSnackbar("Scanned ${allSongs.size} tracks")
                                            }
                                        },
                                        onClearHistory = {
                                            scope.launch {
                                                app.musicRepository.clearHistory()
                                                snackbarHostState.showSnackbar("History and play stats cleared")
                                            }
                                        },
                                        onNavigateAbout = { showAboutContactScreen = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Full Player Modal Sheet
        if (isFullPlayerExpanded && currentSong != null) {
            FullPlayerBottomSheet(
                song = currentSong,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                repeatMode = repeatMode,
                isShuffle = isShuffle,
                playbackSpeed = playbackSpeed,
                sleepTimerSeconds = sleepTimerSeconds,
                stats = activeSongStats,
                allMoods = allMoods,
                assignedMoods = activeSongMoods,
                queue = queue,
                queueIndex = queueIndex,
                artworkShape = userPreferences.artworkShape,
                playbackController = app.playbackController,
                onSaveLyrics = { lyrics ->
                    currentSong?.let { s ->
                        scope.launch {
                            app.database.songDao().updateSong(s.copy(lyrics = lyrics))
                            snackbarHostState.showSnackbar("Lyrics updated")
                        }
                    }
                },
                onSaveMoods = { moodIds ->
                    val sId = currentSong?.id ?: return@FullPlayerBottomSheet
                    scope.launch {
                        for (m in allMoods) {
                            if (moodIds.contains(m.id)) {
                                app.musicRepository.addSongToMood(sId, m.id)
                            } else {
                                app.musicRepository.removeSongFromMood(sId, m.id)
                            }
                        }
                        snackbarHostState.showSnackbar("Mood tags updated")
                    }
                },
                onToggleFavorite = {
                    currentSong?.let { s ->
                        scope.launch { app.musicRepository.toggleManualFavorite(s.id) }
                    }
                },
                onDismiss = { isFullPlayerExpanded = false }
            )
        }

        // Add to Playlist Selection Dialog
        if (songForAddToPlaylist != null) {
            val song = songForAddToPlaylist!!
            AlertDialog(
                onDismissRequest = { songForAddToPlaylist = null },
                title = { Text("Add \"${song.title}\" to Playlist", fontWeight = FontWeight.Bold) },
                text = {
                    if (allPlaylists.isEmpty()) {
                        Text("No playlists found. Create one first in Library tab.")
                    } else {
                        LazyColumn {
                            items(allPlaylists) { pl ->
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            app.musicRepository.addSongToPlaylist(pl.id, song.id)
                                            snackbarHostState.showSnackbar("Added to ${pl.name}")
                                            songForAddToPlaylist = null
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(pl.name, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { songForAddToPlaylist = null }) { Text("Cancel") }
                }
            )
        }

        // Contextual Mood Tag Dialog
        if (songForMoodAssignment != null) {
            val song = songForMoodAssignment!!
            var moodsForThisSong by remember { mutableStateOf<List<Mood>>(emptyList()) }
            LaunchedEffect(song.id) {
                app.musicRepository.getMoodsForSong(song.id).collect { moodsForThisSong = it }
            }

            MoodTagDialog(
                song = song,
                allMoods = allMoods,
                assignedMoods = moodsForThisSong,
                onSaveMoods = { moodIds ->
                    scope.launch {
                        for (m in allMoods) {
                            if (moodIds.contains(m.id)) {
                                app.musicRepository.addSongToMood(song.id, m.id)
                            } else {
                                app.musicRepository.removeSongFromMood(song.id, m.id)
                            }
                        }
                        snackbarHostState.showSnackbar("Mood tags updated")
                        songForMoodAssignment = null
                    }
                },
                onDismiss = { songForMoodAssignment = null }
            )
        }

        // Contextual Details Sheet
        if (songForDetails != null) {
            val song = songForDetails!!
            var songStats by remember { mutableStateOf<SongStats?>(null) }
            LaunchedEffect(song.id) {
                app.musicRepository.getStatsForSong(song.id).collect { songStats = it }
            }

            SongDetailsSheet(
                song = song,
                stats = songStats,
                onDismiss = { songForDetails = null }
            )
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlist: Playlist,
    songs: List<Song>,
    favoriteSongs: List<Song>,
    currentSong: Song?,
    artworkShape: com.example.data.model.ArtworkShape,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onSongClick: (Song) -> Unit,
    onRemoveSong: (Song) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onSongOption: (Song, String) -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onBack)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 110.dp)
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
                                text = playlist.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${songs.size} songs",
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

            if (songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "This playlist is empty.\nAdd songs using the more menu (⋮) on any song!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
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
}
