package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.ArtworkShape
import com.example.data.model.Song

@Composable
fun ArtworkImage(
    song: Song?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: ArtworkShape = ArtworkShape.ROUNDED,
    iconSize: Dp = size / 2
) {
    val clipShape = when (shape) {
        ArtworkShape.ROUNDED -> RoundedCornerShape(12.dp)
        ArtworkShape.SQUIRCLE -> RoundedCornerShape(22.dp)
        ArtworkShape.CIRCLE -> CircleShape
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(clipShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // In Android, contentUri or album art Uri can be loaded via Coil
        if (song != null && song.contentUri.isNotBlank()) {
            AsyncImage(
                model = song.contentUri,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Fallback or decorative music icon
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        )
    }
}
