package com.lionne.tv.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage

/**
 * Vignette focalisable à la télécommande (agrandie et entourée au focus).
 * Affiche le logo ou l'affiche ([imageUrl]) ; à défaut, ou si l'image ne charge pas, des initiales sur dégradé.
 */
@Composable
fun MediaCard(
    title: String,
    subtitle: String?,
    badge: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    imageUrl: String? = null,
) {
    val brand = LocalBrand.current
    var imageLoaded by remember(imageUrl) { mutableStateOf(false) }
    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.width(CardWidth).height(CardHeight),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(brand.surface, brand.primary.copy(alpha = 0.35f)))),
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onSuccess = { imageLoaded = true },
                    onError = { imageLoaded = false },
                    modifier = Modifier.matchParentSize(),
                )
            }
            if (imageLoaded) {
                // Voile en bas de l'image pour garder le titre lisible.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)))),
                )
            }
            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                if (!imageLoaded) {
                    Text(
                        text = initials(title),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = brand.onSurface.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                if (badge != null) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = brand.onPrimary,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(brand.primary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                if (isFavorite) {
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.titleMedium,
                        color = brand.accent,
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = brand.onSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

val CardWidth = 220.dp
val CardHeight = 124.dp

internal fun initials(title: String): String =
    title.split(' ', '-', '_')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }
