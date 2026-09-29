package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Mood
import com.example.data.model.Song
import com.example.data.model.SongMoodCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Query("SELECT * FROM moods ORDER BY isCustom ASC, id ASC")
    fun getAllMoods(): Flow<List<Mood>>

    @Query("SELECT * FROM moods WHERE id = :moodId LIMIT 1")
    fun getMoodById(moodId: Long): Flow<Mood?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoods(moods: List<Mood>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: Mood): Long

    @Query("DELETE FROM moods WHERE id = :moodId AND isCustom = 1")
    suspend fun deleteCustomMood(moodId: Long)

    @Query("UPDATE moods SET name = :name, description = :description WHERE id = :moodId")
    suspend fun updateMood(moodId: Long, name: String, description: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToMood(crossRef: SongMoodCrossRef)

    @Query("DELETE FROM song_mood_cross_ref WHERE songId = :songId AND moodId = :moodId")
    suspend fun removeSongFromMood(songId: Long, moodId: Long)

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN song_mood_cross_ref sm ON s.id = sm.songId
        WHERE sm.moodId = :moodId
        ORDER BY s.title ASC
    """)
    fun getSongsForMood(moodId: Long): Flow<List<Song>>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN song_mood_cross_ref sm ON s.id = sm.songId
        WHERE sm.moodId = :moodId
        ORDER BY s.title ASC
    """)
    suspend fun getSongsForMoodDirect(moodId: Long): List<Song>

    @Query("""
        SELECT m.* FROM moods m
        INNER JOIN song_mood_cross_ref sm ON m.id = sm.moodId
        WHERE sm.songId = :songId
    """)
    fun getMoodsForSong(songId: Long): Flow<List<Mood>>

    @Query("""
        SELECT m.* FROM moods m
        INNER JOIN song_mood_cross_ref sm ON m.id = sm.moodId
        WHERE sm.songId = :songId
    """)
    suspend fun getMoodsForSongDirect(songId: Long): List<Mood>

    @Query("SELECT COUNT(*) FROM song_mood_cross_ref WHERE moodId = :moodId")
    fun getSongCountForMood(moodId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM moods")
    suspend fun getMoodCount(): Int
}
