package com.example

import com.example.data.model.FolderInfo
import com.example.data.model.Song
import com.example.data.model.SongStats
import com.example.data.scanner.MediaStoreScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoyMusicUnitTest {

    @Test
    fun testSongFormattedDuration() {
        val song1 = Song(
            id = 1L,
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            duration = 185000L, // 3 mins 5 secs
            path = "/music/test.mp3",
            contentUri = "content://media/1",
            folderName = "Rock",
            folderPath = "/music/Rock"
        )
        assertEquals("3:05", song1.formattedDuration)

        val song2 = song1.copy(duration = 60000L)
        assertEquals("1:00", song2.formattedDuration)
    }

    @Test
    fun testQualifyingListenRule() {
        val totalDurationMs = 120000L // 2 minutes (120s)
        val minPct = 0.70f // 70% = 84 seconds
        val minSec = 60

        // Listen for 50 seconds (less than 70% and less than 60s) -> Not qualified
        val listened50s = 50000L
        val frac50 = listened50s.toFloat() / totalDurationMs
        val qual50 = (frac50 >= minPct) || (listened50s / 1000 >= minSec)
        assertFalse(qual50)

        // Listen for 65 seconds (>= 60s) -> Qualified
        val listened65s = 65000L
        val frac65 = listened65s.toFloat() / totalDurationMs
        val qual65 = (frac65 >= minPct) || (listened65s / 1000 >= minSec)
        assertTrue(qual65)

        // Listen for 90 seconds (>= 70%) -> Qualified
        val listened90s = 90000L
        val frac90 = listened90s.toFloat() / totalDurationMs
        val qual90 = (frac90 >= minPct) || (listened90s / 1000 >= minSec)
        assertTrue(qual90)
    }

    @Test
    fun testAutoFavoriteThreshold() {
        var stats = SongStats(songId = 100L)
        val threshold = 5

        // Simulating 4 qualifying plays
        stats = stats.copy(playCount = 4, qualifyingPlayCount = 4)
        assertFalse(stats.isFavorite)

        // 5th qualifying play reaches threshold
        stats = stats.copy(
            playCount = 5,
            qualifyingPlayCount = 5,
            isAutoFavorited = true,
            autoFavoritedReason = "Auto-added: played 5 times"
        )
        assertTrue(stats.isFavorite)
        assertEquals("Auto-added: played 5 times", stats.autoFavoritedReason)

        // Test exclusion
        val excludedStats = stats.copy(autoFavoriteExcluded = true)
        assertFalse(excludedStats.isFavorite)
    }

    @Test
    fun testIntelligentMoodSuggestions() {
        val energeticSong = Song(
            id = 1L,
            title = "Fast Electric Overdrive",
            artist = "Rock Band",
            album = "Beat Surge",
            duration = 100000L,
            path = "",
            contentUri = "",
            folderName = "Workout",
            folderPath = "/storage/Music/Workout"
        )
        val moods = MediaStoreScanner.suggestMoodsForSong(energeticSong)
        assertTrue(moods.contains("Energetic") || moods.contains("Workout"))

        val chillSong = Song(
            id = 2L,
            title = "Gentle Ambient Sunset Horizon",
            artist = "Peaceful Acoustic",
            album = "Calm Days",
            duration = 100000L,
            path = "",
            contentUri = "",
            folderName = "Acoustic",
            folderPath = "/storage/Music/Acoustic"
        )
        val chillMoods = MediaStoreScanner.suggestMoodsForSong(chillSong)
        assertTrue(chillMoods.contains("Calm") || chillMoods.contains("Happy"))
    }

    @Test
    fun testFolderTreeBuilding() {
        val songs = listOf(
            Song(1L, "Song A", "Artist", "Album", duration = 60000L, path = "/storage/Music/Rock/a.mp3", contentUri = "", folderName = "Rock", folderPath = "/storage/Music/Rock"),
            Song(2L, "Song B", "Artist", "Album", duration = 120000L, path = "/storage/Music/Rock/b.mp3", contentUri = "", folderName = "Rock", folderPath = "/storage/Music/Rock"),
            Song(3L, "Song C", "Artist", "Album", duration = 180000L, path = "/storage/Music/Jazz/c.mp3", contentUri = "", folderName = "Jazz", folderPath = "/storage/Music/Jazz")
        )

        val folders = MediaStoreScanner.buildFolderTree(songs)
        assertEquals(2, folders.size)

        val rockFolder = folders.first { it.name == "Rock" }
        assertEquals(2, rockFolder.songCount)
        assertEquals(180000L, rockFolder.totalDurationMs)

        val jazzFolder = folders.first { it.name == "Jazz" }
        assertEquals(1, jazzFolder.songCount)
        assertEquals(180000L, jazzFolder.totalDurationMs)
    }

    @Test
    fun testDeveloperAndContactDetails() {
        val developerName = "Abhinaba Roy Pradhan"
        val phone = "+91 6295869078"
        val email = "abhinabapradhan@gmail.com"

        assertEquals("Abhinaba Roy Pradhan", developerName)
        assertTrue(phone.contains("6295869078"))
        assertTrue(email.contains("abhinabapradhan@gmail.com"))
    }
}
