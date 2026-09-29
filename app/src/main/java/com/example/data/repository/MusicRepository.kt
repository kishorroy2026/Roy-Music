package com.example.data.repository

import android.content.Context
import com.example.data.local.HistoryDao
import com.example.data.local.MoodDao
import com.example.data.local.PlaylistDao
import com.example.data.local.SongDao
import com.example.data.local.SongStatsDao
import com.example.data.model.FolderInfo
import com.example.data.model.ListeningHistory
import com.example.data.model.Mood
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import com.example.data.model.SongMoodCrossRef
import com.example.data.model.SongStats
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.scanner.MediaStoreScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(
    private val songDao: SongDao,
    private val moodDao: MoodDao,
    private val playlistDao: PlaylistDao,
    private val songStatsDao: SongStatsDao,
    private val historyDao: HistoryDao,
    private val preferencesRepository: UserPreferencesRepository
) {
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val allMoods: Flow<List<Mood>> = moodDao.getAllMoods()
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    val favoriteSongs: Flow<List<Song>> = songStatsDao.getFavoriteSongs()
    val mostPlayedSongs: Flow<List<Song>> = songStatsDao.getMostPlayedSongs()
    val recentlyPlayedSongs: Flow<List<Song>> = songStatsDao.getRecentlyPlayedSongs()
    val recentlyAddedSongs: Flow<List<Song>> = songDao.getRecentlyAdded(30)

    val albums: Flow<List<Pair<String, List<Song>>>> = allSongs.map { songs ->
        songs.groupBy { it.album }.toList().sortedBy { it.first }
    }

    val artists: Flow<List<Pair<String, List<Song>>>> = allSongs.map { songs ->
        songs.groupBy { it.artist }.toList().sortedBy { it.first }
    }

    val folders: Flow<List<FolderInfo>> = allSongs.map { songs ->
        MediaStoreScanner.buildFolderTree(songs)
    }

    suspend fun rescanLibrary(context: Context) = withContext(Dispatchers.IO) {
        val scanned = MediaStoreScanner.scanDeviceSongs(context)
        songDao.insertSongs(scanned)

        // Seed default moods if not yet created
        val existingMoodsCount = moodDao.getMoodCount()
        if (existingMoodsCount == 0) {
            moodDao.insertMoods(Mood.DEFAULT_MOODS)
        }

        // Auto-assign suggested moods for songs without mood associations
        val currentMoods = moodDao.getAllMoods()
        val moodList = Mood.DEFAULT_MOODS

        for (song in scanned) {
            val existingSongMoods = moodDao.getMoodsForSongDirect(song.id)
            if (existingSongMoods.isEmpty()) {
                val suggestions = MediaStoreScanner.suggestMoodsForSong(song)
                for (suggestedName in suggestions) {
                    val matchingMood = moodList.firstOrNull { it.name.equals(suggestedName, ignoreCase = true) }
                    if (matchingMood != null) {
                        moodDao.addSongToMood(
                            SongMoodCrossRef(
                                songId = song.id,
                                moodId = matchingMood.id,
                                isAutoSuggested = true
                            )
                        )
                    }
                }
            }
        }
    }

    fun getSongsForMood(moodId: Long): Flow<List<Song>> = moodDao.getSongsForMood(moodId)

    suspend fun addSongToMood(songId: Long, moodId: Long) {
        moodDao.addSongToMood(SongMoodCrossRef(songId = songId, moodId = moodId))
    }

    suspend fun removeSongFromMood(songId: Long, moodId: Long) {
        moodDao.removeSongFromMood(songId, moodId)
    }

    fun getMoodsForSong(songId: Long): Flow<List<Mood>> = moodDao.getMoodsForSong(songId)

    suspend fun createCustomMood(name: String, description: String, iconKey: String, startHex: String, endHex: String): Long {
        return moodDao.insertMood(
            Mood(
                name = name,
                description = description,
                iconKey = iconKey,
                gradientStartHex = startHex,
                gradientEndHex = endHex,
                isCustom = true
            )
        )
    }

    suspend fun deleteCustomMood(moodId: Long) {
        moodDao.deleteCustomMood(moodId)
    }

    suspend fun assignAlbumToMood(album: String, moodId: Long) = withContext(Dispatchers.IO) {
        val songs = songDao.getAllSongsDirect().filter { it.album == album }
        for (song in songs) {
            moodDao.addSongToMood(SongMoodCrossRef(songId = song.id, moodId = moodId))
        }
    }

    suspend fun assignFolderToMood(folderPath: String, moodId: Long) = withContext(Dispatchers.IO) {
        val songs = songDao.getAllSongsDirect().filter { it.folderPath == folderPath }
        for (song in songs) {
            moodDao.addSongToMood(SongMoodCrossRef(songId = song.id, moodId = moodId))
        }
    }

    // Playlists
    suspend fun createPlaylist(name: String, description: String = ""): Long {
        return playlistDao.insertPlaylist(Playlist(name = name, description = description))
    }

    suspend fun renamePlaylist(id: Long, name: String, description: String) {
        playlistDao.updatePlaylist(id, name, description)
    }

    suspend fun deletePlaylist(id: Long) {
        playlistDao.deletePlaylist(id)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> = playlistDao.getSongsForPlaylist(playlistId)

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val nextOrder = playlistDao.getNextSortOrder(playlistId)
        playlistDao.addSongToPlaylist(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                sortOrder = nextOrder
            )
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun addFolderToPlaylist(playlistId: Long, folderPath: String) = withContext(Dispatchers.IO) {
        val songs = songDao.getAllSongsDirect().filter { it.folderPath == folderPath }
        for (song in songs) {
            addSongToPlaylist(playlistId, song.id)
        }
    }

    suspend fun addMoodToPlaylist(playlistId: Long, moodId: Long) = withContext(Dispatchers.IO) {
        val songs = moodDao.getSongsForMoodDirect(moodId)
        for (song in songs) {
            addSongToPlaylist(playlistId, song.id)
        }
    }

    // Automatic Favorites and Playback Tracking
    suspend fun recordPlayback(songId: Long, durationPlayedMs: Long, totalDurationMs: Long) = withContext(Dispatchers.IO) {
        if (totalDurationMs <= 0) return@withContext

        val prefs = preferencesRepository.userPreferencesFlow.first()
        val fraction = durationPlayedMs.toFloat() / totalDurationMs.toFloat()
        val durationPlayedSeconds = (durationPlayedMs / 1000).toInt()

        // Rule: listen >= 70% or >= 60 seconds
        val isQualified = (fraction >= prefs.autoFavoriteMinPercentage) || (durationPlayedSeconds >= prefs.autoFavoriteMinSeconds)

        // Save history entry
        historyDao.insertHistory(
            ListeningHistory(
                songId = songId,
                playedAt = System.currentTimeMillis(),
                durationPlayedMs = durationPlayedMs,
                completionFraction = fraction,
                qualified = isQualified
            )
        )

        val currentStats = songStatsDao.getStatsForSongDirect(songId) ?: SongStats(songId = songId)
        val newPlayCount = currentStats.playCount + 1
        val newQualifyingCount = if (isQualified) currentStats.qualifyingPlayCount + 1 else currentStats.qualifyingPlayCount

        var autoFav = currentStats.isAutoFavorited
        var autoReason = currentStats.autoFavoritedReason
        var favTimestamp = currentStats.favoriteTimestamp

        // Auto Favorite Threshold logic
        if (prefs.isAutoFavoriteEnabled && !currentStats.autoFavoriteExcluded) {
            if (newQualifyingCount >= prefs.autoFavoriteThreshold && !currentStats.isAutoFavorited) {
                // If it was manually un-favorited before, check if re-favoriting is allowed
                if (!currentStats.isManuallyFavorited || prefs.autoRefavoriteAllowed) {
                    autoFav = true
                    autoReason = "Auto-added: played $newQualifyingCount times"
                    favTimestamp = System.currentTimeMillis()
                }
            }
        }

        val updatedStats = currentStats.copy(
            playCount = newPlayCount,
            qualifyingPlayCount = newQualifyingCount,
            lastPlayedTimestamp = System.currentTimeMillis(),
            isAutoFavorited = autoFav,
            autoFavoritedReason = autoReason,
            favoriteTimestamp = if (favTimestamp == 0L && autoFav) System.currentTimeMillis() else favTimestamp
        )

        songStatsDao.upsertStats(updatedStats)
    }

    fun getStatsForSong(songId: Long): Flow<SongStats?> = songStatsDao.getStatsForSong(songId)

    suspend fun toggleManualFavorite(songId: Long) = withContext(Dispatchers.IO) {
        val currentStats = songStatsDao.getStatsForSongDirect(songId) ?: SongStats(songId = songId)
        val newFav = !currentStats.isFavorite
        val updated = currentStats.copy(
            isManuallyFavorited = newFav,
            // If turning off, clear auto-favorite as well to respect manual unfavorite
            isAutoFavorited = if (!newFav) false else currentStats.isAutoFavorited,
            favoriteTimestamp = if (newFav) System.currentTimeMillis() else 0L
        )
        songStatsDao.upsertStats(updated)
    }

    suspend fun undoAutoFavorite(songId: Long) = withContext(Dispatchers.IO) {
        songStatsDao.undoAutoFavorite(songId)
    }

    suspend fun excludeFromAutoFavorite(songId: Long, excluded: Boolean) = withContext(Dispatchers.IO) {
        songStatsDao.setAutoFavoriteExcluded(songId, excluded)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }

    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)

    suspend fun getSongByIdDirect(id: Long): Song? = songDao.getSongByIdDirect(id)
}
