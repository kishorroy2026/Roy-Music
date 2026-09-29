package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "song_stats")
data class SongStats(
    @PrimaryKey val songId: Long,
    val playCount: Int = 0,
    val qualifyingPlayCount: Int = 0, // >= 70% or >= 60s
    val lastPlayedTimestamp: Long = 0L,
    val isManuallyFavorited: Boolean = false,
    val isAutoFavorited: Boolean = false,
    val autoFavoritedReason: String = "",
    val autoFavoriteExcluded: Boolean = false,
    val favoriteTimestamp: Long = 0L
) {
    val isFavorite: Boolean
        get() = (isManuallyFavorited || isAutoFavorited) && !autoFavoriteExcluded
}

@Entity(
    tableName = "listening_history",
    indices = [Index("songId"), Index("playedAt")]
)
data class ListeningHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val songId: Long,
    val playedAt: Long = System.currentTimeMillis(),
    val durationPlayedMs: Long = 0L,
    val completionFraction: Float = 0f,
    val qualified: Boolean = false
)
