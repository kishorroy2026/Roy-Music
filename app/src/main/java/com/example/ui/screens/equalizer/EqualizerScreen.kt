package com.example.ui.screens.equalizer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SoundProfile
import com.example.playback.AudioEffectsManager
import com.example.ui.components.RoyMusicLogoBadge
import java.util.UUID

@Composable
fun EqualizerScreen(
    audioEffectsManager: AudioEffectsManager,
    initialEnabled: Boolean,
    customProfiles: List<SoundProfile> = emptyList(),
    onSaveCustomProfile: (SoundProfile) -> Unit = {},
    onDeleteCustomProfile: (String) -> Unit = {},
    onBack: () -> Unit,
    onSavePreferences: (Boolean, Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var isEnabled by remember { mutableStateOf(initialEnabled) }
    var selectedProfileId by remember { mutableStateOf("studio_flat") }
    var bassBoostValue by remember { mutableFloatStateOf(0f) }
    var virtualizerValue by remember { mutableFloatStateOf(0f) }

    // 5 Bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz
    val bandFrequencies = listOf(
        Pair("60 Hz", "Sub-Bass"),
        Pair("230 Hz", "Bass / Warmth"),
        Pair("910 Hz", "Midrange"),
        Pair("3.6 kHz", "Presence"),
        Pair("14 kHz", "Air / Treble")
    )
    val bandLevels = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f) }

    // Dialog state for saving custom profile
    var showSaveProfileDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }

    val allProfiles = remember(customProfiles) {
        SoundProfile.builtInProfiles + customProfiles
    }

    fun applyProfile(profile: SoundProfile) {
        selectedProfileId = profile.id
        for (i in 0 until 5) {
            val level = profile.bandLevels.getOrElse(i) { 0f }
            bandLevels[i] = level
            audioEffectsManager.setBandLevel(i.toShort(), (level * 100).toInt().toShort())
        }
        bassBoostValue = profile.bassBoost
        audioEffectsManager.setBassBoost((profile.bassBoost * 1000).toInt())

        virtualizerValue = profile.virtualizer
        audioEffectsManager.setVirtualizer((profile.virtualizer * 1000).toInt())

        onSavePreferences(
            isEnabled,
            0,
            (bassBoostValue * 1000).toInt(),
            (virtualizerValue * 1000).toInt()
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .testTag("equalizer_screen")
        ) {
            // Header with Modern Logo & Master Power Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("eq_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    RoyMusicLogoBadge(size = 38.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Studio Equalizer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEnabled) "Active Studio Engine" else "Equalizer Bypassed",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (isEnabled) "ACTIVE" else "OFF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = {
                            isEnabled = it
                            audioEffectsManager.setEnabled(it)
                            onSavePreferences(
                                isEnabled,
                                0,
                                (bassBoostValue * 1000).toInt(),
                                (virtualizerValue * 1000).toInt()
                            )
                        },
                        modifier = Modifier.testTag("equalizer_power_switch")
                    )
                }
            }

            // 1. Live Interactive Visual Frequency Response Curve
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Frequency Response",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = {
                                val flatProfile = SoundProfile.builtInProfiles.first()
                                applyProfile(flatProfile)
                            },
                            enabled = isEnabled,
                            modifier = Modifier.testTag("eq_reset_btn")
                        ) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Animated Curve Canvas
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val tertiaryColor = MaterialTheme.colorScheme.tertiary
                    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                    val baselineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0B0A14))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
                            val w = size.width
                            val h = size.height
                            val midY = h / 2f

                            // Draw decibel grid lines (+10dB, 0dB, -10dB)
                            drawLine(
                                color = baselineColor,
                                start = Offset(0f, midY),
                                end = Offset(w, midY),
                                strokeWidth = 1.5f
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, midY - (h * 0.4f)),
                                end = Offset(w, midY - (h * 0.4f)),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, midY + (h * 0.4f)),
                                end = Offset(w, midY + (h * 0.4f)),
                                strokeWidth = 1f
                            )

                            // 5 Points across width
                            val points = (0 until 5).map { i ->
                                val x = (w / 4f) * i
                                // Map -10dB..+10dB to height coordinates (inverted)
                                val levelClamped = bandLevels[i].coerceIn(-10f, 10f)
                                val y = midY - (levelClamped / 10f) * (h * 0.4f)
                                Offset(x, y)
                            }

                            // Build smooth cubic bezier path
                            val path = Path()
                            val fillPath = Path()

                            if (points.isNotEmpty()) {
                                path.moveTo(points.first().x, points.first().y)
                                fillPath.moveTo(points.first().x, midY)
                                fillPath.lineTo(points.first().x, points.first().y)

                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val controlX = (p0.x + p1.x) / 2f
                                    path.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                    fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                }

                                fillPath.lineTo(points.last().x, midY)
                                fillPath.close()

                                // Gradient fill under the curve
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = if (isEnabled) 0.35f else 0.1f),
                                            tertiaryColor.copy(alpha = if (isEnabled) 0.15f else 0.05f),
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = h
                                    )
                                )

                                // Main smooth response line
                                drawPath(
                                    path = path,
                                    brush = Brush.horizontalGradient(
                                        colors = if (isEnabled) listOf(primaryColor, tertiaryColor) else listOf(Color.Gray, Color.DarkGray)
                                    ),
                                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                                )

                                // Node circles on each band
                                points.forEachIndexed { idx, pt ->
                                    drawCircle(
                                        color = if (isEnabled) primaryColor else Color.Gray,
                                        radius = 5.5f,
                                        center = pt
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 2.5f,
                                        center = pt
                                    )
                                }
                            }
                        }
                    }

                    // Axis labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        bandFrequencies.forEach { (freq, _) ->
                            Text(
                                text = freq,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 2. Sound Profiles Carousel & Custom Profiles
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sound Profiles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Studio presets & custom profiles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showSaveProfileDialog = true },
                        enabled = isEnabled,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp).testTag("save_profile_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Profile", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allProfiles) { profile ->
                        val isSelected = (selectedProfileId == profile.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = { applyProfile(profile) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (profile.isCustom) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = "Custom profile",
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(profile.name)
                                }
                            },
                            trailingIcon = if (profile.isCustom) {
                                {
                                    IconButton(
                                        onClick = { onDeleteCustomProfile(profile.id) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete custom profile",
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            } else null,
                            enabled = isEnabled,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. 5-Band Frequency Sliders
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Frequency Band Controls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Adjust individual frequency gains (-10 dB to +10 dB)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    bandFrequencies.forEachIndexed { bandIdx, (freq, description) ->
                        val level = bandLevels[bandIdx]
                        val isBoosted = level > 0.5f
                        val isCut = level < -0.5f

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = freq,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            isBoosted -> MaterialTheme.colorScheme.primaryContainer
                                            isCut -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ) {
                                        Text(
                                            text = "${if (level > 0) "+" else ""}${level.toInt()} dB",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isBoosted -> MaterialTheme.colorScheme.primary
                                                isCut -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Slider(
                                    value = level,
                                    onValueChange = { newVal ->
                                        bandLevels[bandIdx] = newVal
                                        audioEffectsManager.setBandLevel(bandIdx.toShort(), (newVal * 100).toInt().toShort())
                                    },
                                    valueRange = -10f..10f,
                                    steps = 20,
                                    enabled = isEnabled,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("eq_band_slider_$bandIdx")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Bass Boost & 3D Virtualizer Audio Effects
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Acoustic Effects & Depth",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sub-bass resonance and 3D spatial soundstage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bass Boost Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Waves,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bass Boost",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${(bassBoostValue * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Slider(
                                value = bassBoostValue,
                                onValueChange = {
                                    bassBoostValue = it
                                    audioEffectsManager.setBassBoost((it * 1000).toInt())
                                    onSavePreferences(isEnabled, 0, (it * 1000).toInt(), (virtualizerValue * 1000).toInt())
                                },
                                enabled = isEnabled,
                                modifier = Modifier.testTag("bass_boost_slider")
                            )

                            // Quick Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(0f to "Off", 0.3f to "30%", 0.6f to "60%", 1.0f to "Max").forEach { (v, lbl) ->
                                    OutlinedButton(
                                        onClick = {
                                            bassBoostValue = v
                                            audioEffectsManager.setBassBoost((v * 1000).toInt())
                                            onSavePreferences(isEnabled, 0, (v * 1000).toInt(), (virtualizerValue * 1000).toInt())
                                        },
                                        enabled = isEnabled,
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Text(lbl, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3D Virtualizer Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SurroundSound,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "3D Surround Virtualizer",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${(virtualizerValue * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Slider(
                                value = virtualizerValue,
                                onValueChange = {
                                    virtualizerValue = it
                                    audioEffectsManager.setVirtualizer((it * 1000).toInt())
                                    onSavePreferences(isEnabled, 0, (bassBoostValue * 1000).toInt(), (it * 1000).toInt())
                                },
                                enabled = isEnabled,
                                modifier = Modifier.testTag("virtualizer_slider")
                            )

                            // Quick Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(0f to "Off", 0.3f to "30%", 0.6f to "60%", 1.0f to "Wide").forEach { (v, lbl) ->
                                    OutlinedButton(
                                        onClick = {
                                            virtualizerValue = v
                                            audioEffectsManager.setVirtualizer((v * 1000).toInt())
                                            onSavePreferences(isEnabled, 0, (bassBoostValue * 1000).toInt(), (v * 1000).toInt())
                                        },
                                        enabled = isEnabled,
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Text(lbl, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    // Save Custom Profile Dialog
    if (showSaveProfileDialog) {
        AlertDialog(
            onDismissRequest = { showSaveProfileDialog = false },
            title = {
                Text(
                    text = "Save Custom Sound Profile",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Give this custom audio profile a memorable name (e.g. My Headphones, Car Bass, Acoustic Chill).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newProfileName.trim().ifEmpty { "Custom Profile ${customProfiles.size + 1}" }
                        val custom = SoundProfile(
                            id = "custom_${UUID.randomUUID()}",
                            name = name,
                            isCustom = true,
                            bandLevels = bandLevels.toList(),
                            bassBoost = bassBoostValue,
                            virtualizer = virtualizerValue
                        )
                        onSaveCustomProfile(custom)
                        selectedProfileId = custom.id
                        newProfileName = ""
                        showSaveProfileDialog = false
                    },
                    modifier = Modifier.testTag("confirm_save_profile_btn")
                ) {
                    Text("Save Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
