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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
                viewModel = viewModel,
                onPlay = onPlay,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Ready,
    viewModel: HomeViewModel,
    onPlay: (MediaEntry) -> Unit,
) {
    val brand = LocalBrand.current
    val focusTarget = remember { FocusRequester() }
    var targetFocused by remember { mutableStateOf(false) }
    val listState = rememberLazyListState(viewModel.scrollIndex, viewModel.scrollOffset)

    // Au retour du lecteur, le focus revient sur le contenu lancé ; sinon sur la première vignette.
    val targetId = viewModel.lastPlayedId
        ?.takeIf { id -> state.rows.any { row -> row.items.any { it.entry.id == id } } }
        ?: state.rows.firstOrNull()?.items?.firstOrNull()?.entry?.id
    val targetRowKey = state.rows.firstOrNull { row -> row.items.any { it.entry.id == targetId } }?.key

    DisposableEffect(listState) {
        onDispose {
            viewModel.rememberPosition(null, listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }
    }

    LazyColumn(
        state = listState,
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
        items(state.rows, key = { row -> row.key }) { row ->
            val isTargetRow = row.key == targetRowKey
            val rowState = rememberLazyListState(
                initialFirstVisibleItemIndex = if (isTargetRow) {
                    row.items.indexOfFirst { it.entry.id == targetId }.coerceAtLeast(0)
                } else {
                    0
                },
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = row.title, style = MaterialTheme.typography.titleMedium)
                LazyRow(
                    state = rowState,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(row.items, key = { item -> item.entry.id }) { item ->
                        val focusModifier =
                            if (isTargetRow && item.entry.id == targetId) {
                                Modifier
                                    .focusRequester(focusTarget)
                                    .onFocusChanged { targetFocused = it.isFocused }
                            } else {
                                Modifier
                            }
                        MediaCard(
                            title = item.entry.title,
                            subtitle = item.entry.description ?: item.entry.category,
                            badge = if (item.entry.type == ContentType.LIVE) "DIRECT" else null,
                            isFavorite = item.isFavorite,
                            onClick = {
                                viewModel.rememberPosition(
                                    item.entry.id,
                                    listState.firstVisibleItemIndex,
                                    listState.firstVisibleItemScrollOffset,
                                )
                                onPlay(item.entry)
                            },
                            onLongClick = { viewModel.toggleFavorite(item.entry.id) },
                            modifier = focusModifier,
                            imageUrl = item.entry.posterUrl ?: item.entry.logoUrl,
                        )
                    }
                }
            }
        }
    }

    // Une seule fois par affichage de l'accueil : ne pas voler le focus quand on ajoute un favori.
    LaunchedEffect(Unit) {
        if (targetId == null) return@LaunchedEffect
        // Les vignettes n'existent qu'après la première mise en page : on réessaie quelques images.
        repeat(FOCUS_ATTEMPTS) {
            withFrameNanos { }
            if (targetFocused) return@LaunchedEffect
            runCatching { focusTarget.requestFocus() }
        }
    }
}

private const val FOCUS_ATTEMPTS = 30

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
