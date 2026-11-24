package com.lionne.tv.feature.home

import com.lionne.tv.core.data.CatalogSnapshot
import com.lionne.tv.core.model.ContentType
import com.lionne.tv.core.model.MediaEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeRowsTest {

    private fun entry(id: String, type: ContentType, category: String) =
        MediaEntry(id, id, "https://x/$id", type, category, "s")

    private val snapshot = CatalogSnapshot(
        listOf(
            entry("info", ContentType.LIVE, "Info"),
            entry("foot", ContentType.LIVE, "Sport"),
            entry("film", ContentType.VOD, "Films"),
        ),
    )

    @Test
    fun `favoris d'abord, puis direct, puis a la demande`() {
        val rows = buildHomeRows(snapshot, favoriteIds = setOf("film"))

        assertEquals(
            listOf("favorites", "live:Info", "live:Sport", "vod:Films"),
            rows.map { it.key },
        )
        assertEquals(listOf("film"), rows.first().items.map { it.entry.id })
        assertTrue(rows.last().items.single().isFavorite)
    }

    @Test
    fun `pas de rangee favoris si aucun favori`() {
        assertEquals("live:Info", buildHomeRows(snapshot, emptySet()).first().key)
    }
}
