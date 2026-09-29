package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.data.model.RoyTheme

// Accent & Brand Colors
val RoyNeonViolet = Color(0xFF8B5CF6)
val RoyElectricCyan = Color(0xFF06B6D4)
val RoyEmerald = Color(0xFF10B981)
val RoySunset = Color(0xFFF97316)
val RoyRose = Color(0xFFF43F5E)
val RoyGold = Color(0xFFFFB300)

fun getThemeColorScheme(theme: RoyTheme, isDark: Boolean) = when (theme) {
    RoyTheme.MIDNIGHT_BLACK -> darkColorScheme(
        primary = Color(0xFFA78BFA),
        onPrimary = Color(0xFF1E1145),
        primaryContainer = Color(0xFF3B2773),
        onPrimaryContainer = Color(0xFFEDE9FE),
        secondary = Color(0xFF38BDF8),
        onSecondary = Color(0xFF082F49),
        secondaryContainer = Color(0xFF0C4A6E),
        onSecondaryContainer = Color(0xFFE0F2FE),
        background = Color(0xFF0A0912),
        onBackground = Color(0xFFF3F4F6),
        surface = Color(0xFF141322),
        onSurface = Color(0xFFF3F4F6),
        surfaceVariant = Color(0xFF1F1D33),
        onSurfaceVariant = Color(0xFFCBD5E1)
    )

    RoyTheme.DEEP_PURPLE -> darkColorScheme(
        primary = Color(0xFFC084FC),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F2787),
        onPrimaryContainer = Color(0xFFF3E8FF),
        secondary = Color(0xFFE879F9),
        onSecondary = Color(0xFF581C87),
        background = Color(0xFF0F071D),
        onBackground = Color(0xFFFAF5FF),
        surface = Color(0xFF1A0E31),
        onSurface = Color(0xFFFAF5FF),
        surfaceVariant = Color(0xFF28174A),
        onSurfaceVariant = Color(0xFFE9D5FF)
    )

    RoyTheme.OCEAN_BLUE -> darkColorScheme(
        primary = Color(0xFF22D3EE),
        onPrimary = Color(0xFF083344),
        primaryContainer = Color(0xFF155E75),
        onPrimaryContainer = Color(0xFFCFFAFE),
        secondary = Color(0xFF38BDF8),
        onSecondary = Color(0xFF082F49),
        background = Color(0xFF06101E),
        onBackground = Color(0xFFF0FDF4),
        surface = Color(0xFF0E1F36),
        onSurface = Color(0xFFF0F9FF),
        surfaceVariant = Color(0xFF162E4F),
        onSurfaceVariant = Color(0xFFBAE6FD)
    )

    RoyTheme.EMERALD_GREEN -> darkColorScheme(
        primary = Color(0xFF34D399),
        onPrimary = Color(0xFF064E3B),
        primaryContainer = Color(0xFF047857),
        onPrimaryContainer = Color(0xFFD1FAE5),
        secondary = Color(0xFF2DD4BF),
        onSecondary = Color(0xFF134E4A),
        background = Color(0xFF061510),
        onBackground = Color(0xFFECFDF5),
        surface = Color(0xFF0C241C),
        onSurface = Color(0xFFECFDF5),
        surfaceVariant = Color(0xFF14372B),
        onSurfaceVariant = Color(0xFFA7F3D0)
    )

    RoyTheme.SUNSET_ORANGE -> darkColorScheme(
        primary = Color(0xFFFB923C),
        onPrimary = Color(0xFF431407),
        primaryContainer = Color(0xFF9A3412),
        onPrimaryContainer = Color(0xFFFFEDD5),
        secondary = Color(0xFFFBBF24),
        onSecondary = Color(0xFF451A03),
        background = Color(0xFF160A06),
        onBackground = Color(0xFFFFF7ED),
        surface = Color(0xFF27130D),
        onSurface = Color(0xFFFFF7ED),
        surfaceVariant = Color(0xFF3D1F16),
        onSurfaceVariant = Color(0xFFFED7AA)
    )

    RoyTheme.ROSE_PINK -> darkColorScheme(
        primary = Color(0xFFFB7185),
        onPrimary = Color(0xFF4C0519),
        primaryContainer = Color(0xFF9F1239),
        onPrimaryContainer = Color(0xFFFFE4E6),
        secondary = Color(0xFFF472B6),
        onSecondary = Color(0xFF500724),
        background = Color(0xFF14070D),
        onBackground = Color(0xFFFFF1F2),
        surface = Color(0xFF250F1A),
        onSurface = Color(0xFFFFF1F2),
        surfaceVariant = Color(0xFF3B182B),
        onSurfaceVariant = Color(0xFFFECDD3)
    )

    RoyTheme.AMOLED_BLACK -> darkColorScheme(
        primary = Color(0xFF38BDF8),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF0284C7),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFA855F7),
        onSecondary = Color(0xFF000000),
        background = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF0A0A0A),
        onSurface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFF1A1A1A),
        onSurfaceVariant = Color(0xFFE2E8F0)
    )

    RoyTheme.LIGHT_MINIMAL -> if (isDark) {
        darkColorScheme(
            primary = Color(0xFF818CF8),
            onPrimary = Color(0xFF1E1B4B),
            background = Color(0xFF0F172A),
            surface = Color(0xFF1E293B),
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF8FAFC)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF4F46E5),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEEF2FF),
            onPrimaryContainer = Color(0xFF312E81),
            secondary = Color(0xFF0EA5E9),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF475569)
        )
    }

    RoyTheme.NEON_CYBERPUNK -> darkColorScheme(
        primary = Color(0xFF22D3EE),
        onPrimary = Color(0xFF042F2E),
        primaryContainer = Color(0xFF134E4A),
        onPrimaryContainer = Color(0xFFCCFBF1),
        secondary = Color(0xFFF43F5E),
        onSecondary = Color(0xFF4C0519),
        background = Color(0xFF090714),
        onBackground = Color(0xFFF8FAFC),
        surface = Color(0xFF140F28),
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = Color(0xFF231B42),
        onSurfaceVariant = Color(0xFFCBD5E1)
    )

    RoyTheme.TITANIUM_GOLD -> darkColorScheme(
        primary = Color(0xFFF59E0B),
        onPrimary = Color(0xFF451A03),
        primaryContainer = Color(0xFF78350F),
        onPrimaryContainer = Color(0xFFFEF3C7),
        secondary = Color(0xFFFBBF24),
        onSecondary = Color(0xFF451A03),
        background = Color(0xFF0F0E13),
        onBackground = Color(0xFFFFFBEB),
        surface = Color(0xFF1B1922),
        onSurface = Color(0xFFFFFBEB),
        surfaceVariant = Color(0xFF2B2835),
        onSurfaceVariant = Color(0xFFE2E8F0)
    )

    RoyTheme.SAPPHIRE_NIGHT -> darkColorScheme(
        primary = Color(0xFF3B82F6),
        onPrimary = Color(0xFF172554),
        primaryContainer = Color(0xFF1E3A8A),
        onPrimaryContainer = Color(0xFFDBEAFE),
        secondary = Color(0xFF60A5FA),
        onSecondary = Color(0xFF1E3A8A),
        background = Color(0xFF060B18),
        onBackground = Color(0xFFEFF6FF),
        surface = Color(0xFF0D172F),
        onSurface = Color(0xFFEFF6FF),
        surfaceVariant = Color(0xFF16254A),
        onSurfaceVariant = Color(0xFFBFDBFE)
    )

    RoyTheme.STUDIO_CARBON -> darkColorScheme(
        primary = Color(0xFFFF5757),
        onPrimary = Color(0xFF450A0A),
        primaryContainer = Color(0xFF7F1D1D),
        onPrimaryContainer = Color(0xFFFEE2E2),
        secondary = Color(0xFF94A3B8),
        onSecondary = Color(0xFF0F172A),
        background = Color(0xFF101014),
        onBackground = Color(0xFFF8FAFC),
        surface = Color(0xFF1A1A20),
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = Color(0xFF282832),
        onSurfaceVariant = Color(0xFFCBD5E1)
    )
}
