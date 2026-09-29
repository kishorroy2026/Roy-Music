package com.example.data.model

data class SoundProfile(
    val id: String,
    val name: String,
    val isCustom: Boolean = false,
    val bandLevels: List<Float>, // 5 bands in dB (-10f to +10f)
    val bassBoost: Float = 0f,   // 0f to 1f (0% to 100%)
    val virtualizer: Float = 0f  // 0f to 1f (0% to 100%)
) {
    companion object {
        val builtInProfiles = listOf(
            SoundProfile(
                id = "studio_flat",
                name = "Studio Flat",
                isCustom = false,
                bandLevels = listOf(0f, 0f, 0f, 0f, 0f),
                bassBoost = 0f,
                virtualizer = 0f
            ),
            SoundProfile(
                id = "bass_monster",
                name = "Bass Monster",
                isCustom = false,
                bandLevels = listOf(7f, 5f, 0f, -1f, -3f),
                bassBoost = 0.70f,
                virtualizer = 0.20f
            ),
            SoundProfile(
                id = "vocal_clarity",
                name = "Vocal Clarity",
                isCustom = false,
                bandLevels = listOf(-3f, 1f, 6f, 4f, 2f),
                bassBoost = 0.10f,
                virtualizer = 0.15f
            ),
            SoundProfile(
                id = "club_edm",
                name = "Club & EDM",
                isCustom = false,
                bandLevels = listOf(6f, 4f, -1f, 3f, 6f),
                bassBoost = 0.60f,
                virtualizer = 0.40f
            ),
            SoundProfile(
                id = "rock_punch",
                name = "Rock Punch",
                isCustom = false,
                bandLevels = listOf(5f, 3f, -2f, 4f, 5f),
                bassBoost = 0.35f,
                virtualizer = 0.25f
            ),
            SoundProfile(
                id = "acoustic_warmth",
                name = "Acoustic Warmth",
                isCustom = false,
                bandLevels = listOf(3f, 4f, 1f, 2f, 3f),
                bassBoost = 0.20f,
                virtualizer = 0.10f
            ),
            SoundProfile(
                id = "treble_sparkle",
                name = "Treble Sparkle",
                isCustom = false,
                bandLevels = listOf(-2f, -1f, 1f, 6f, 8f),
                bassBoost = 0f,
                virtualizer = 0.30f
            ),
            SoundProfile(
                id = "late_night",
                name = "Late Night Soft",
                isCustom = false,
                bandLevels = listOf(2f, 0f, -2f, -1f, -4f),
                bassBoost = 0.15f,
                virtualizer = 0.10f
            )
        )

        fun serialize(profiles: List<SoundProfile>): String {
            // Simple robust format: id|name|b0,b1,b2,b3,b4|bass|virt;...
            return profiles.filter { it.isCustom }.joinToString(";;") { profile ->
                val bands = profile.bandLevels.joinToString(",") { it.toString() }
                "${profile.id}::${profile.name}::$bands::${profile.bassBoost}::${profile.virtualizer}"
            }
        }

        fun deserialize(raw: String): List<SoundProfile> {
            if (raw.isBlank()) return emptyList()
            return runCatching {
                raw.split(";;").mapNotNull { entry ->
                    val parts = entry.split("::")
                    if (parts.size >= 5) {
                        val id = parts[0]
                        val name = parts[1]
                        val bands = parts[2].split(",").mapNotNull { it.toFloatOrNull() }
                        val bass = parts[3].toFloatOrNull() ?: 0f
                        val virt = parts[4].toFloatOrNull() ?: 0f
                        if (bands.size == 5) {
                            SoundProfile(
                                id = id,
                                name = name,
                                isCustom = true,
                                bandLevels = bands,
                                bassBoost = bass,
                                virtualizer = virt
                            )
                        } else null
                    } else null
                }
            }.getOrDefault(emptyList())
        }
    }
}
