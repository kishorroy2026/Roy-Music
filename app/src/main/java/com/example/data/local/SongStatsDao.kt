package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.ListeningHistory
import com.example.data.model.Song
import com.example.data.model.SongStats
import kotlinx.coroutines.flow.Flow

@Dao
interface SongStatsDao {
    @Query("SELECT * FROM song_stats WHERE songId = :songId LIMIT 1")
    fun getStatsForSong(songId: Long): Flow<SongStats?>

    @Query("SELECT * FROM song_stats WHERE songId = :songId LIMIT 1")
    suspend fun getStatsForSongDirect(songId: Long): SongStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: SongStats)

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN song_stats st ON s.id = st.songId
        WHERE (st.isManuallyFavorited = 1 OR st.isAutoFavorited = 1)
          AND st.autoFavoriteExcluded = 0
        ORDER BY st.favoriteTimestamp DESC
    """)
    fun getFavoriteSongs(): Flow<List<Song>>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN song_stats st ON s.id = st.songId
        WHERE (st.isManuallyFavorited = 1 OR st.isAutoFavorited = 1)
          AND st.autoFavoriteExcluded = 0
        ORDER BY st.favoriteTimestamp DESC
    """)
    suspend fun getFavoriteSongsDirect(): List<Song>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN song_stats st ON s.id = st.songId
        WHERE (st.isManuallyFavorited = 1 OR st.isAutoFavorited = 1)
          AND st.autoFavoriteExcluded = 0
        ORDER BY st.playCount DESC
    """)
    fun getFavoritesSortedByPlays(): Flow<List<Song>>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN song_stats st ON s.id = st.songId
        ORDER BY st.playCount DESC
        LIMIT :limit
    """)
    fun getMostPlayedSongs(limit: Int = 30): Flow<List<Song>>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN song_stats st ON s.id = st.songId
        WHERE st.lastPlayedTimestamp > 0
        ORDER BY st.lastPlayedTimestamp DESC
        LIMIT :limit
    """)
    fun getRecentlyPlayedSongs(limit: Int = 30): Flow<List<Song>>

    @Query("UPDATE song_stats SET isManuallyFavorited = :isFav, favoriteTimestamp = :timestamp WHERE songId = :songId")
    suspend fun updateManualFavorite(songId: Long, isFav: Boolean, timestamp: Long)

    @Query("UPDATE song_stats SET isAutoFavorited = 0, autoFavoritedReason = '' WHERE songId = :songId")
    suspend fun undoAutoFavorite(songId: Long)

    @Query("UPDATE song_stats SET autoFavoriteExcluded = :excluded WHERE songId = :songId")
    suspend fun setAutoFavoriteExcluded(songId: Long, excluded: Boolean)

    @Query("SELECT COUNT(*) FROM song_stats WHERE (isManuallyFavorited = 1 OR isAutoFavorited = 1) AND autoFavoriteExcluded = 0")
    fun getFavoriteCount(): Flow<Int>
}

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ListeningHistory)

    @Query("""
        SELECT h.* FROM listening_history h
        ORDER BY h.playedAt DESC
        LIMIT :limit
    """)
    fun getRecentHistory(limit: Int = 50): Flow<List<ListeningHistory>>

    @Query("DELETE FROM listening_history")
    suspend fun clearHistory()
}
