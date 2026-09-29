package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "moods")
data class Mood(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String,
    val iconKey: String, // e.g. "happy", "sad", "romantic", "fire", "spa", "brain", "fitness", "party", "night", "temple", "star"
    val gradientStartHex: String,
    val gradientEndHex: String,
    val isCustom: Boolean = false
) {
    companion object {
        val DEFAULT_MOODS = listOf(
            Mood(
                id = 1L,
                name = "Happy",
                description = "Upbeat melodies, radiant energy and good vibes",
                iconKey = "happy",
                gradientStartHex = "#FFB300",
                gradientEndHex = "#F4511E"
            ),
            Mood(
                id = 2L,
                name = "Sad",
                description = "Melancholy acoustics, reflective and soulful tunes",
                iconKey = "sad",
                gradientStartHex = "#5C6BC0",
                gradientEndHex = "#283593"
            ),
            Mood(
                id = 3L,
                name = "Romantic",
                description = "Passionate rhythms, gentle warmth and love ballads",
                iconKey = "romantic",
                gradientStartHex = "#EC407A",
                gradientEndHex = "#880E4F"
            ),
            Mood(
                id = 4L,
                name = "Energetic",
                description = "Fast tempo, electric beats and unstoppable power",
                iconKey = "fire",
                gradientStartHex = "#FF7043",
                gradientEndHex = "#D84315"
            ),
            Mood(
                id = 5L,
                name = "Calm",
                description = "Peaceful ambient soundscapes and gentle harmony",
                iconKey = "spa",
                gradientStartHex = "#26A69A",
                gradientEndHex = "#004D40"
            ),
            Mood(
                id = 6L,
                name = "Focus",
                description = "Deep flow state, instrumental and study music",
                iconKey = "brain",
                gradientStartHex = "#42A5F5",
                gradientEndHex = "#1565C0"
            ),
            Mood(
                id = 7L,
                name = "Workout",
                description = "Heavy bass, high BPM adrenaline rush",
                iconKey = "fitness",
                gradientStartHex = "#AB47BC",
                gradientEndHex = "#4A148C"
            ),
            Mood(
                id = 8L,
                name = "Party",
                description = "Dance club anthems, synth waves and festival vibes",
                iconKey = "party",
                gradientStartHex = "#7E57C2",
                gradientEndHex = "#E91E63"
            ),
            Mood(
                id = 9L,
                name = "Sleep",
                description = "Dreamy lofi, soft night breezes and restorative rest",
                iconKey = "night",
                gradientStartHex = "#3949AB",
                gradientEndHex = "#1A237E"
            ),
            Mood(
                id = 10L,
                name = "Devotional",
                description = "Spiritual chants, sacred serenity and meditative bliss",
                iconKey = "temple",
                gradientStartHex = "#FFA726",
                gradientEndHex = "#E65100"
            )
        )
    }
}

@Entity(
    tableName = "song_mood_cross_ref",
    primaryKeys = ["songId", "moodId"],
    indices = [Index("moodId"), Index("songId")]
)
data class SongMoodCrossRef(
    val songId: Long,
    val moodId: Long,
    val isAutoSuggested: Boolean = false,
    val assignedAt: Long = System.currentTimeMillis()
)
