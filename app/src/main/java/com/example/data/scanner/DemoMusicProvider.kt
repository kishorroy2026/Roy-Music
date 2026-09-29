package com.example.data.scanner

import android.content.Context
import android.net.Uri
import com.example.data.model.Song
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object DemoMusicProvider {

    /**
     * Generates small valid PCM WAV audio files for demo tracks so ExoPlayer
     * can play real sound seamlessly offline even when device media store is empty.
     */
    fun ensureDemoAudioFiles(context: Context): List<Song> {
        val demoDir = File(context.filesDir, "RoyMusicDemo")
        if (!demoDir.exists()) {
            demoDir.mkdirs()
        }

        val demoTracks = listOf(
            DemoTrackSpec(
                id = -101L,
                title = "Midnight Horizon",
                artist = "Roy Chillout",
                album = "Neon Dreamscapes",
                subFolder = "Ambient",
                durationSeconds = 120,
                baseFreq = 220.0, // A3
                moods = listOf("Calm", "Sleep", "Focus")
            ),
            DemoTrackSpec(
                id = -102L,
                title = "Electric Velocity",
                artist = "Pulse Syndicate",
                album = "Overdrive",
                subFolder = "Electronic",
                durationSeconds = 95,
                baseFreq = 330.0, // E4
                moods = listOf("Energetic", "Workout", "Party")
            ),
            DemoTrackSpec(
                id = -103L,
                title = "Sunlit Symphony",
                artist = "Acoustic Aura",
                album = "Golden Days",
                subFolder = "Acoustic",
                durationSeconds = 140,
                baseFreq = 261.63, // C4
                moods = listOf("Happy", "Romantic", "Calm")
            ),
            DemoTrackSpec(
                id = -104L,
                title = "Deep Flow State",
                artist = "Mind Waves",
                album = "Cognition",
                subFolder = "Focus",
                durationSeconds = 180,
                baseFreq = 174.61, // F3
                moods = listOf("Focus", "Calm")
            ),
            DemoTrackSpec(
                id = -105L,
                title = "Eternal Serenity",
                artist = "Sacred Echoes",
                album = "Inner Temple",
                subFolder = "Meditative",
                durationSeconds = 160,
                baseFreq = 196.0, // G3
                moods = listOf("Devotional", "Sleep")
            ),
            DemoTrackSpec(
                id = -106L,
                title = "Heartbeat Romance",
                artist = "Velvet Strings",
                album = "Midnight Whispers",
                subFolder = "Acoustic",
                durationSeconds = 110,
                baseFreq = 293.66, // D4
                moods = listOf("Romantic", "Sad")
            )
        )

        val resultSongs = mutableListOf<Song>()

        for (spec in demoTracks) {
            val folderDir = File(demoDir, spec.subFolder)
            if (!folderDir.exists()) folderDir.mkdirs()

            val audioFile = File(folderDir, "${spec.title.replace(" ", "_")}.wav")
            if (!audioFile.exists() || audioFile.length() < 1000) {
                createSimpleWav(audioFile, spec.baseFreq, spec.durationSeconds)
            }

            resultSongs.add(
                Song(
                    id = spec.id,
                    title = spec.title,
                    artist = spec.artist,
                    album = spec.album,
                    albumId = spec.id * 10,
                    duration = spec.durationSeconds * 1000L,
                    path = audioFile.absolutePath,
                    contentUri = Uri.fromFile(audioFile).toString(),
                    folderName = spec.subFolder,
                    folderPath = folderDir.absolutePath,
                    size = audioFile.length(),
                    mimeType = "audio/wav",
                    dateAdded = System.currentTimeMillis() - (spec.id * -1000000L),
                    year = 2026,
                    bitrate = 128,
                    sampleRate = 22050,
                    lyrics = """
                        [Verse 1]
                        Walking through the neon glow of night
                        Listening to the rhythm of the city lights
                        Every note a journey, every chord a dream
                        Living inside a melodious stream.

                        [Chorus]
                        Roy Music in my ears, feeling so alive
                        Let the frequencies uplift and arrive!
                    """.trimIndent(),
                    isDemo = true
                )
            )
        }

        return resultSongs
    }

    private data class DemoTrackSpec(
        val id: Long,
        val title: String,
        val artist: String,
        val album: String,
        val subFolder: String,
        val durationSeconds: Int,
        val baseFreq: Double,
        val moods: List<String>
    )

    /**
     * Synthesizes a gentle, harmonious musical tone WAV file using pure Kotlin,
     * writing proper 44-byte RIFF WAV header and 16-bit PCM samples.
     */
    private fun createSimpleWav(file: File, freq: Double, durationSec: Int) {
        val sampleRate = 22050
        val numSamples = sampleRate * durationSec
        val numChannels = 1
        val bitsPerSample = 16
        val dataSize = numSamples * numChannels * (bitsPerSample / 8)

        runCatching {
            FileOutputStream(file).use { out ->
                // RIFF Header
                out.write("RIFF".toByteArray())
                out.write(intToBytes(36 + dataSize))
                out.write("WAVE".toByteArray())

                // fmt chunk
                out.write("fmt ".toByteArray())
                out.write(intToBytes(16)) // Subchunk1Size (16 for PCM)
                out.write(shortToBytes(1)) // AudioFormat (1 = PCM)
                out.write(shortToBytes(numChannels.toShort()))
                out.write(intToBytes(sampleRate))
                out.write(intToBytes(sampleRate * numChannels * (bitsPerSample / 8))) // ByteRate
                out.write(shortToBytes((numChannels * (bitsPerSample / 8)).toShort())) // BlockAlign
                out.write(shortToBytes(bitsPerSample.toShort()))

                // data chunk
                out.write("data".toByteArray())
                out.write(intToBytes(dataSize))

                // PCM Data (warm chord with harmonic overtone and gentle fade in/out)
                val buffer = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN)
                val harmonic = freq * 1.5 // 5th harmonic for musical fullness
                val octave = freq * 2.0

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Envelope: soft tremolo and subtle wave
                    val envelope = 0.8 + 0.2 * Math.sin(2.0 * Math.PI * 0.5 * time)
                    val s1 = Math.sin(2.0 * Math.PI * freq * time) * 0.5
                    val s2 = Math.sin(2.0 * Math.PI * harmonic * time) * 0.3
                    val s3 = Math.sin(2.0 * Math.PI * octave * time) * 0.2
                    val sampleValue = ((s1 + s2 + s3) * envelope * 16000.0).toInt().coerceIn(-32767, 32767)

                    if (buffer.remaining() < 2) {
                        out.write(buffer.array(), 0, buffer.position())
                        buffer.clear()
                    }
                    buffer.putShort(sampleValue.toShort())
                }

                if (buffer.position() > 0) {
                    out.write(buffer.array(), 0, buffer.position())
                }
            }
        }
    }

    private fun intToBytes(value: Int): ByteArray {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    private fun shortToBytes(value: Short): ByteArray {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()
    }
}
