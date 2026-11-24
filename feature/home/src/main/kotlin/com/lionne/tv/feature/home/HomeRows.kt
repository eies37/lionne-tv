package com.lionne.tv.feature.home

import com.lionne.tv.core.data.CatalogSnapshot
import com.lionne.tv.core.model.ContentType
import com.lionne.tv.core.model.MediaEntry

data class HomeItem(val entry: MediaEntry, val isFavorite: Boolean)

data class HomeRow(val key: String, val title: String, val items: List<HomeItem>)

/**
 * Construit les rangées de l'accueil : favoris, puis le direct par catégorie,
 * puis la vidéo à la demande par catégorie.
 */
fun buildHomeRows(snapshot: CatalogSnapshot, favoriteIds: Set<String>): List<HomeRow> {
    fun item(entry: MediaEntry) = HomeItem(entry, entry.id in favoriteIds)

    val rows = mutableListOf<HomeRow>()
    val favorites = snapshot.entries.filter { it.id in favoriteIds }
    if (favorites.isNotEmpty()) {
        rows += HomeRow("favorites", "Mes favoris", favorites.map(::item))
    }
    snapshot.byCategory(ContentType.LIVE).forEach { (category, entries) ->
        rows += HomeRow("live:$category", "En direct · $category", entries.map(::item))
    }
    snapshot.byCategory(ContentType.VOD).forEach { (category, entries) ->
        rows += HomeRow("vod:$category", "À la demande · $category", entries.map(::item))
    }
    return rows
}
