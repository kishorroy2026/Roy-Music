package com.example.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log

data class EqualizerPreset(
    val name: String,
    val bandLevels: List<Short> // 5 bands in millibels (-1000 to +1000)
)

class AudioEffectsManager {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentSessionId: Int = 0

    var isEnabled: Boolean = false
        private set

    val presets = listOf(
        EqualizerPreset("Flat / Normal", listOf(0, 0, 0, 0, 0)),
        EqualizerPreset("Rock", listOf(400, 200, -100, 300, 500)),
        EqualizerPreset("Pop", listOf(-100, 200, 500, 200, -100)),
        EqualizerPreset("Classical", listOf(400, 300, -100, 300, 400)),
        EqualizerPreset("Jazz", listOf(300, 100, -100, 200, 400)),
        EqualizerPreset("Bass Boost", listOf(600, 400, 0, 0, -200)),
        EqualizerPreset("Vocal Boost", listOf(-200, 100, 500, 300, 100)),
        EqualizerPreset("Electronic", listOf(500, 300, 0, 200, 500))
    )

    fun attachAudioSession(sessionId: Int) {
        if (sessionId <= 0 || sessionId == currentSessionId) return
        release()
        currentSessionId = sessionId

        runCatching {
            equalizer = Equalizer(0, sessionId).apply {
                enabled = this@AudioEffectsManager.isEnabled
            }
        }.onFailure { Log.w("AudioEffects", "Equalizer init failed: ${it.message}") }

        runCatching {
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = this@AudioEffectsManager.isEnabled
            }
        }.onFailure { Log.w("AudioEffects", "BassBoost init failed: ${it.message}") }

        runCatching {
            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = this@AudioEffectsManager.isEnabled
            }
        }.onFailure { Log.w("AudioEffects", "Virtualizer init failed: ${it.message}") }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        runCatching { equalizer?.enabled = enabled }
        runCatching { bassBoost?.enabled = enabled }
        runCatching { virtualizer?.enabled = enabled }
    }

    fun applyPreset(index: Int) {
        val preset = presets.getOrNull(index) ?: return
        val eq = equalizer ?: return
        val numBands = eq.numberOfBands.toInt()
        val minLevel = eq.bandLevelRange?.getOrNull(0) ?: -1000
        val maxLevel = eq.bandLevelRange?.getOrNull(1) ?: 1000

        for (i in 0 until numBands) {
            val level = preset.bandLevels.getOrElse(i) { 0 }
            val clamped = level.coerceIn(minLevel, maxLevel)
            runCatching { eq.setBandLevel(i.toShort(), clamped) }
        }
    }

    fun setBandLevel(bandIndex: Short, levelMilliBels: Short) {
        runCatching {
            equalizer?.setBandLevel(bandIndex, levelMilliBels)
        }
    }

    fun getBandLevel(bandIndex: Short): Short {
        return runCatching { equalizer?.getBandLevel(bandIndex) ?: 0 }.getOrDefault(0)
    }

    fun setBassBoost(strength: Int) {
        // strength 0 to 1000
        runCatching {
            bassBoost?.setStrength(strength.coerceIn(0, 1000).toShort())
        }
    }

    fun setVirtualizer(strength: Int) {
        // strength 0 to 1000
        runCatching {
            virtualizer?.setStrength(strength.coerceIn(0, 1000).toShort())
        }
    }

    fun release() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
        currentSessionId = 0
    }
}
