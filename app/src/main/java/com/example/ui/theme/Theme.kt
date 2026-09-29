package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.RoyTheme

@Composable
fun RoyMusicTheme(
    selectedTheme: RoyTheme = RoyTheme.MIDNIGHT_BLACK,
    isDarkMode: Boolean = true,
    isSystemTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = if (isSystemTheme) isSystemInDarkTheme() else isDarkMode
    val colorScheme = getThemeColorScheme(selectedTheme, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backward compatibility for any preview
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    RoyMusicTheme(
        selectedTheme = RoyTheme.MIDNIGHT_BLACK,
        isDarkMode = darkTheme,
        content = content
    )
}
