package com.lionne.tv.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.lionne.tv.core.data.SourceFailure
import com.lionne.tv.core.designsystem.LocalBrand
import com.lionne.tv.core.designsystem.MediaCard
import com.lionne.tv.core.model.ContentType
import com.lionne.tv.core.model.MediaEntry

/**
 * Accueil provisoire : rangées horizontales navigables à la télécommande.
 * Appui long sur OK pour ajouter ou retirer un favori.
 * Conception finale de l'interface : tâche T-007 (ChatGPT).
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPlay: (MediaEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (val current = state) {
            HomeUiState.Loading -> Text(
                text = "Chargement des chaînes…",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center),
            )
            is HomeUiState.Ready -> HomeContent(
                state = current,
                onPlay = onPlay,
                onToggleFavorite = viewModel::toggleFavorite,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Ready,
    onPlay: (MediaEntry) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val brand = LocalBrand.current
    val firstCard = remember { FocusRequester() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 27.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item(key = "header") {
            Column {
                Text(
                    text = brand.appName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = brand.primary,
                )
                Text(
                    text = "Appui long sur OK : ajouter ou retirer des favoris",
                    style = MaterialTheme.typography.bodySmall,
                    color = brand.onSurfaceMuted,
                )
            }
        }
        if (state.failures.isNotEmpty()) {
            item(key = "failures") { FailuresBanner(state.failures) }
        }
        if (state.rows.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = "Aucun contenu. Ajoutez une source dans sources.json.",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        itemsIndexed(state.rows, key = { _, row -> row.key }) { rowIndex, row ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = row.title, style = MaterialTheme.typography.titleMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    itemsIndexed(row.items, key = { _, item -> item.entry.id }) { index, item ->
                        val focusModifier =
                            if (rowIndex == 0 && index == 0) Modifier.focusRequester(firstCard) else Modifier
                        MediaCard(
                            title = item.entry.title,
                            subtitle = item.entry.description ?: item.entry.category,
                            badge = if (item.entry.type == ContentType.LIVE) "DIRECT" else null,
                            isFavorite = item.isFavorite,
                            onClick = { onPlay(item.entry) },
                            onLongClick = { onToggleFavorite(item.entry.id) },
                            modifier = focusModifier,
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(state.rows.isNotEmpty()) {
        if (state.rows.isNotEmpty()) runCatching { firstCard.requestFocus() }
    }
}

@Composable
private fun FailuresBanner(failures: List<SourceFailure>) {
    val brand = LocalBrand.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(brand.surface)
            .padding(12.dp),
    ) {
        Text(
            text = "Sources indisponibles",
            style = MaterialTheme.typography.titleSmall,
            color = brand.accent,
        )
        failures.forEach { failure ->
            Text(
                text = "${failure.sourceName} : ${failure.message}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
