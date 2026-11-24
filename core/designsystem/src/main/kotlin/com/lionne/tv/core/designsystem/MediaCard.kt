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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/**
 * Vignette focalisable à la télécommande (agrandie et entourée au focus).
 * Sans image pour l'instant : initiales sur dégradé, ce qui évite tout téléchargement.
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
) {
    val brand = LocalBrand.current
    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.width(CardWidth).height(CardHeight),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(brand.surface, brand.primary.copy(alpha = 0.35f))))
                .padding(12.dp),
        ) {
            Text(
                text = initials(title),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = brand.onSurface.copy(alpha = 0.85f),
                modifier = Modifier.align(Alignment.Center),
            )
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

val CardWidth = 220.dp
val CardHeight = 124.dp

internal fun initials(title: String): String =
    title.split(' ', '-', '_')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }
