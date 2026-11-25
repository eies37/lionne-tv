package com.lionne.tv.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lionne.tv.core.data.CatalogRepository
import com.lionne.tv.core.data.CatalogSnapshot
import com.lionne.tv.core.data.GuideRepository
import com.lionne.tv.core.data.FavoritesStore
import com.lionne.tv.core.data.SourceFailure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(val rows: List<HomeRow>, val failures: List<SourceFailure>) : HomeUiState
}

class HomeViewModel(
    private val repository: CatalogRepository,
    private val favorites: FavoritesStore,
    private val guide: GuideRepository? = null,
) : ViewModel() {

    /**
     * Où était l'utilisateur avant de lancer une lecture, pour l'y ramener au retour :
     * contenu lancé (focus) et défilement vertical de l'accueil.
     */
    var lastPlayedId: String? = null
        private set
    var scrollIndex: Int = 0
        private set
    var scrollOffset: Int = 0
        private set

    fun rememberPosition(playedId: String?, index: Int, offset: Int) {
        if (playedId != null) lastPlayedId = playedId
        scrollIndex = index
        scrollOffset = offset
    }

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var guideFailures: List<SourceFailure> = emptyList()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = HomeUiState.Loading
            val snapshot = repository.refresh()
            publish(snapshot)
            // Le guide arrive ensuite, sans retarder l'affichage des chaînes.
            if (guide != null) {
                guideFailures = guide.refresh()
                publish(snapshot)
            }
        }
    }

    fun toggleFavorite(id: String) {
        favorites.toggle(id)
        repository.snapshot.value?.let(::publish)
    }

    /** Remet à jour « en ce moment » (à appeler au retour sur l'accueil). */
    fun refreshNowPlaying() {
        if (guide == null || guide.isEmpty) return
        repository.snapshot.value?.let(::publish)
    }

    private fun publish(snapshot: CatalogSnapshot) {
        val rows = buildHomeRows(snapshot, favorites.ids()) { entry ->
            guide?.nowNext(entry.epgId)?.now?.title
        }
        _state.value = HomeUiState.Ready(rows, snapshot.failures + guideFailures)
    }
}
