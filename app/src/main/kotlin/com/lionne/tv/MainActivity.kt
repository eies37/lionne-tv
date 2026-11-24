package com.lionne.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lionne.tv.core.designsystem.LionneTheme
import com.lionne.tv.feature.home.HomeScreen
import com.lionne.tv.feature.home.HomeViewModel
import com.lionne.tv.feature.player.PlayerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as LionneApp).container
        setContent {
            LionneTheme {
                LionneNavigation(container)
            }
        }
    }
}

/** Navigation minimale entre l'accueil et le lecteur (identifiant du contenu en cours, ou null). */
@Composable
private fun LionneNavigation(container: AppContainer) {
    var playingId by rememberSaveable { mutableStateOf<String?>(null) }
    val homeViewModel = viewModel { HomeViewModel(container.catalog, container.favorites) }
    val snapshot by container.catalog.snapshot.collectAsStateWithLifecycle()

    val id = playingId
    val entry = id?.let { snapshot?.find(it) }
    if (id == null || entry == null) {
        HomeScreen(viewModel = homeViewModel, onPlay = { playingId = it.id })
        if (id != null && snapshot != null) {
            // Contenu disparu du catalogue (source retirée) : retour à l'accueil.
            LaunchedEffect(id) { playingId = null }
        }
    } else {
        PlayerScreen(
            entry = entry,
            liveChannels = snapshot?.live.orEmpty(),
            positions = container.positions,
            onBack = { playingId = null },
        )
    }
}
