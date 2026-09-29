package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long = 0L,
    val duration: Long, // in milliseconds
    val path: String,
    val contentUri: String,
    val folderName: String,
    val folderPath: String,
    val size: Long = 0L,
    val mimeType: String = "audio/mpeg",
    val dateAdded: Long = 0L,
    val year: Int = 0,
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val lyrics: String? = null,
    val isDemo: Boolean = false
) {
    val formattedDuration: String
        get() {
            val totalSeconds = duration / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val displayArtist: String
        get() = if (artist.isBlank() || artist == "<unknown>") "Unknown Artist" else artist

    val displayAlbum: String
        get() = if (album.isBlank() || album == "<unknown>") "Unknown Album" else album
}
