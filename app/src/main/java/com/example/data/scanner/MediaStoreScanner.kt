package com.example.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.model.FolderInfo
import com.example.data.model.Song
import java.io.File

object MediaStoreScanner {

    fun scanDeviceSongs(context: Context): List<Song> {
        val songList = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.YEAR
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val collectionUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        runCatching {
            context.contentResolver.query(
                collectionUri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdCol)
                    val duration = cursor.getLong(durationCol)
                    val path = cursor.getString(dataCol) ?: ""
                    val size = cursor.getLong(sizeCol)
                    val mimeType = cursor.getString(mimeTypeCol) ?: "audio/mpeg"
                    val dateAdded = cursor.getLong(dateAddedCol) * 1000L
                    val year = cursor.getInt(yearCol)

                    val contentUri = ContentUris.withAppendedId(collectionUri, id).toString()

                    val file = if (path.isNotBlank()) File(path) else null
                    val folderName = file?.parentFile?.name ?: "Music"
                    val folderPath = file?.parentFile?.absolutePath ?: "/storage/emulated/0/Music"

                    songList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            duration = duration,
                            path = path,
                            contentUri = contentUri,
                            folderName = folderName,
                            folderPath = folderPath,
                            size = size,
                            mimeType = mimeType,
                            dateAdded = dateAdded,
                            year = year,
                            isDemo = false
                        )
                    )
                }
            }
        }

        // Include curated demo tracks so user has immediate playback offline
        val demoTracks = DemoMusicProvider.ensureDemoAudioFiles(context)
        songList.addAll(demoTracks)

        return songList
    }

    /**
     * Intelligent transparent mood suggestions based on title, artist, album, and folder names.
     */
    fun suggestMoodsForSong(song: Song): List<String> {
        val text = "${song.title} ${song.artist} ${song.album} ${song.folderName}".lowercase()
        val suggestions = mutableListOf<String>()

        if (text.contains("happy") || text.contains("sun") || text.contains("joy") || text.contains("smile") || text.contains("dance") || text.contains("gold")) {
            suggestions.add("Happy")
        }
        if (text.contains("sad") || text.contains("cry") || text.contains("alone") || text.contains("blue") || text.contains("tears") || text.contains("heartbreak")) {
            suggestions.add("Sad")
        }
        if (text.contains("love") || text.contains("heart") || text.contains("romance") || text.contains("kiss") || text.contains("sweet") || text.contains("whisper")) {
            suggestions.add("Romantic")
        }
        if (text.contains("rock") || text.contains("beat") || text.contains("fire") || text.contains("power") || text.contains("electric") || text.contains("velocity") || text.contains("fast")) {
            suggestions.add("Energetic")
        }
        if (text.contains("chill") || text.contains("ambient") || text.contains("calm") || text.contains("peace") || text.contains("horizon") || text.contains("gentle") || text.contains("soft")) {
            suggestions.add("Calm")
        }
        if (text.contains("study") || text.contains("focus") || text.contains("mind") || text.contains("instrumental") || text.contains("piano") || text.contains("deep")) {
            suggestions.add("Focus")
        }
        if (text.contains("gym") || text.contains("workout") || text.contains("fitness") || text.contains("run") || text.contains("overdrive") || text.contains("bass")) {
            suggestions.add("Workout")
        }
        if (text.contains("party") || text.contains("club") || text.contains("night") || text.contains("remix") || text.contains("dj") || text.contains("disco")) {
            suggestions.add("Party")
        }
        if (text.contains("sleep") || text.contains("dream") || text.contains("night") || text.contains("lullaby") || text.contains("rest") || text.contains("rain")) {
            suggestions.add("Sleep")
        }
        if (text.contains("prayer") || text.contains("temple") || text.contains("spiritual") || text.contains("devotional") || text.contains("sacred") || text.contains("meditation")) {
            suggestions.add("Devotional")
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Calm")
        }

        return suggestions.distinct()
    }

    /**
     * Builds structured folder models from songs
     */
    fun buildFolderTree(songs: List<Song>): List<FolderInfo> {
        val groupedByPath = songs.groupBy { it.folderPath }
        return groupedByPath.map { (path, folderSongs) ->
            val folderName = folderSongs.firstOrNull()?.folderName ?: File(path).name.ifBlank { "Music" }
            val totalDuration = folderSongs.sumOf { it.duration }
            FolderInfo(
                name = folderName,
                path = path,
                songCount = folderSongs.size,
                totalDurationMs = totalDuration,
                songs = folderSongs.sortedBy { it.title }
            )
        }.sortedBy { it.name }
    }
}
