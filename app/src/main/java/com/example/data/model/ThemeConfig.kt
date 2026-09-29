package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class RoyTheme(val displayName: String, val primaryHex: Long, val backgroundHex: Long, val surfaceHex: Long) {
    MIDNIGHT_BLACK("Midnight Black", 0xFF8B5CF6, 0xFF0D0C15, 0xFF171626),
    DEEP_PURPLE("Deep Purple", 0xFFA855F7, 0xFF120A21, 0xFF1E1338),
    OCEAN_BLUE("Ocean Blue", 0xFF06B6D4, 0xFF08131E, 0xFF102235),
    EMERALD_GREEN("Emerald Green", 0xFF10B981, 0xFF071B14, 0xFF0E2C22),
    SUNSET_ORANGE("Sunset Orange", 0xFFF97316, 0xFF1F100B, 0xFF321B13),
    ROSE_PINK("Rose Pink", 0xFFF43F5E, 0xFF1C0A12, 0xFF2D121F),
    AMOLED_BLACK("AMOLED Black", 0xFF38BDF8, 0xFF000000, 0xFF121212),
    LIGHT_MINIMAL("Light Minimal", 0xFF6366F1, 0xFFF8FAFC, 0xFFFFFFFF),
    NEON_CYBERPUNK("Neon Cyberpunk", 0xFF22D3EE, 0xFF090714, 0xFF140F28),
    TITANIUM_GOLD("Titanium Gold", 0xFFF59E0B, 0xFF0F0E13, 0xFF1B1922),
    SAPPHIRE_NIGHT("Sapphire Night", 0xFF3B82F6, 0xFF060B18, 0xFF0D172F),
    STUDIO_CARBON("Studio Carbon", 0xFFFF5757, 0xFF101014, 0xFF1A1A20);
}

enum class ArtworkShape(val displayName: String) {
    ROUNDED("Rounded"),
    SQUIRCLE("Squircle"),
    CIRCLE("Circle")
}

enum class MiniPlayerStyle(val displayName: String) {
    FLOATING_CARD("Floating Card"),
    COMPACT_BAR("Compact Bar"),
    GLASS_BANNER("Glass Banner")
}
