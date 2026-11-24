package com.lionne.tv.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lionne.tv.core.data.CatalogRepository
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

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = HomeUiState.Loading
            val snapshot = repository.refresh()
            _state.value = HomeUiState.Ready(buildHomeRows(snapshot, favorites.ids()), snapshot.failures)
        }
    }

    fun toggleFavorite(id: String) {
        favorites.toggle(id)
        val snapshot = repository.snapshot.value ?: return
        _state.value = HomeUiState.Ready(buildHomeRows(snapshot, favorites.ids()), snapshot.failures)
    }
}
