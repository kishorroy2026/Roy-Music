package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ArtworkShape
import com.example.data.model.MiniPlayerStyle
import com.example.data.model.RoyTheme
import com.example.data.model.SoundProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "roy_music_preferences")

data class UserPreferences(
    val theme: RoyTheme = RoyTheme.MIDNIGHT_BLACK,
    val artworkShape: ArtworkShape = ArtworkShape.ROUNDED,
    val miniPlayerStyle: MiniPlayerStyle = MiniPlayerStyle.FLOATING_CARD,
    val isDarkMode: Boolean = true,
    val isSystemTheme: Boolean = false,
    val isAutoFavoriteEnabled: Boolean = true,
    val autoFavoriteThreshold: Int = 5,
    val autoFavoriteMinPercentage: Float = 0.70f,
    val autoFavoriteMinSeconds: Int = 60,
    val autoRefavoriteAllowed: Boolean = false,
    val equalizerEnabled: Boolean = false,
    val equalizerPreset: Int = 0,
    val bassBoostStrength: Int = 0,
    val virtualizerStrength: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val isVisualizerEnabled: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("app_theme")
        val ARTWORK_SHAPE = stringPreferencesKey("artwork_shape")
        val MINI_PLAYER_STYLE = stringPreferencesKey("mini_player_style")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_SYSTEM_THEME = booleanPreferencesKey("is_system_theme")
        val IS_AUTO_FAVORITE_ENABLED = booleanPreferencesKey("is_auto_fav_enabled")
        val AUTO_FAVORITE_THRESHOLD = intPreferencesKey("auto_fav_threshold")
        val AUTO_FAV_MIN_PERCENTAGE = floatPreferencesKey("auto_fav_min_pct")
        val AUTO_FAV_MIN_SECONDS = intPreferencesKey("auto_fav_min_sec")
        val AUTO_REFAVORITE_ALLOWED = booleanPreferencesKey("auto_refav_allowed")
        val EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
        val EQUALIZER_PRESET = intPreferencesKey("equalizer_preset")
        val BASS_BOOST = intPreferencesKey("bass_boost")
        val VIRTUALIZER = intPreferencesKey("virtualizer")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val VISUALIZER_ENABLED = booleanPreferencesKey("visualizer_enabled")
        val CUSTOM_PROFILES = stringPreferencesKey("custom_sound_profiles")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val themeStr = preferences[PreferencesKeys.THEME] ?: RoyTheme.MIDNIGHT_BLACK.name
        val theme = runCatching { RoyTheme.valueOf(themeStr) }.getOrDefault(RoyTheme.MIDNIGHT_BLACK)

        val shapeStr = preferences[PreferencesKeys.ARTWORK_SHAPE] ?: ArtworkShape.ROUNDED.name
        val shape = runCatching { ArtworkShape.valueOf(shapeStr) }.getOrDefault(ArtworkShape.ROUNDED)

        val styleStr = preferences[PreferencesKeys.MINI_PLAYER_STYLE] ?: MiniPlayerStyle.FLOATING_CARD.name
        val style = runCatching { MiniPlayerStyle.valueOf(styleStr) }.getOrDefault(MiniPlayerStyle.FLOATING_CARD)

        UserPreferences(
            theme = theme,
            artworkShape = shape,
            miniPlayerStyle = style,
            isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: true,
            isSystemTheme = preferences[PreferencesKeys.IS_SYSTEM_THEME] ?: false,
            isAutoFavoriteEnabled = preferences[PreferencesKeys.IS_AUTO_FAVORITE_ENABLED] ?: true,
            autoFavoriteThreshold = preferences[PreferencesKeys.AUTO_FAVORITE_THRESHOLD] ?: 5,
            autoFavoriteMinPercentage = preferences[PreferencesKeys.AUTO_FAV_MIN_PERCENTAGE] ?: 0.70f,
            autoFavoriteMinSeconds = preferences[PreferencesKeys.AUTO_FAV_MIN_SECONDS] ?: 60,
            autoRefavoriteAllowed = preferences[PreferencesKeys.AUTO_REFAVORITE_ALLOWED] ?: false,
            equalizerEnabled = preferences[PreferencesKeys.EQUALIZER_ENABLED] ?: false,
            equalizerPreset = preferences[PreferencesKeys.EQUALIZER_PRESET] ?: 0,
            bassBoostStrength = preferences[PreferencesKeys.BASS_BOOST] ?: 0,
            virtualizerStrength = preferences[PreferencesKeys.VIRTUALIZER] ?: 0,
            playbackSpeed = preferences[PreferencesKeys.PLAYBACK_SPEED] ?: 1.0f,
            isVisualizerEnabled = preferences[PreferencesKeys.VISUALIZER_ENABLED] ?: true
        )
    }

    suspend fun setTheme(theme: RoyTheme) {
        context.dataStore.edit { it[PreferencesKeys.THEME] = theme.name }
    }

    suspend fun setArtworkShape(shape: ArtworkShape) {
        context.dataStore.edit { it[PreferencesKeys.ARTWORK_SHAPE] = shape.name }
    }

    suspend fun setMiniPlayerStyle(style: MiniPlayerStyle) {
        context.dataStore.edit { it[PreferencesKeys.MINI_PLAYER_STYLE] = style.name }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_DARK_MODE] = isDark }
    }

    suspend fun setSystemTheme(isSystem: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_SYSTEM_THEME] = isSystem }
    }

    suspend fun setAutoFavoriteEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_AUTO_FAVORITE_ENABLED] = enabled }
    }

    suspend fun setAutoFavoriteThreshold(threshold: Int) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_FAVORITE_THRESHOLD] = threshold }
    }

    suspend fun setAutoFavoriteMinPercentage(pct: Float) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_FAV_MIN_PERCENTAGE] = pct }
    }

    suspend fun setAutoFavoriteMinSeconds(sec: Int) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_FAV_MIN_SECONDS] = sec }
    }

    suspend fun setAutoRefavoriteAllowed(allowed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_REFAVORITE_ALLOWED] = allowed }
    }

    suspend fun setEqualizerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.EQUALIZER_ENABLED] = enabled }
    }

    suspend fun setEqualizerPreset(preset: Int) {
        context.dataStore.edit { it[PreferencesKeys.EQUALIZER_PRESET] = preset }
    }

    suspend fun setBassBoostStrength(strength: Int) {
        context.dataStore.edit { it[PreferencesKeys.BASS_BOOST] = strength }
    }

    suspend fun setVirtualizerStrength(strength: Int) {
        context.dataStore.edit { it[PreferencesKeys.VIRTUALIZER] = strength }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[PreferencesKeys.PLAYBACK_SPEED] = speed }
    }

    suspend fun setVisualizerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VISUALIZER_ENABLED] = enabled }
    }

    val customSoundProfilesFlow: Flow<List<SoundProfile>> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.CUSTOM_PROFILES] ?: ""
        SoundProfile.deserialize(raw)
    }

    suspend fun saveCustomSoundProfile(profile: SoundProfile) {
        val current = customSoundProfilesFlow.first().filter { it.id != profile.id }
        val updated = current + profile.copy(isCustom = true)
        val serialized = SoundProfile.serialize(updated)
        context.dataStore.edit { it[PreferencesKeys.CUSTOM_PROFILES] = serialized }
    }

    suspend fun deleteCustomSoundProfile(profileId: String) {
        val current = customSoundProfilesFlow.first().filter { it.id != profileId }
        val serialized = SoundProfile.serialize(current)
        context.dataStore.edit { it[PreferencesKeys.CUSTOM_PROFILES] = serialized }
    }
}
