package com.lionne.tv.core.data

import com.lionne.tv.core.model.ContentSource
import com.lionne.tv.core.model.ContentType
import com.lionne.tv.core.model.MediaEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogRepositoryTest {

    private fun entry(id: String, type: ContentType = ContentType.LIVE, category: String = "Info") =
        MediaEntry(id = id, title = id, streamUrl = "https://x/$id.m3u8", type = type, category = category, sourceId = "s")

    private class FakeSource(
        override val id: String,
        private val entries: List<MediaEntry>? = null,
        private val error: Throwable? = null,
    ) : ContentSource {
        override val name: String = "Source $id"
        override suspend fun load(): List<MediaEntry> = error?.let { throw it } ?: entries.orEmpty()
    }

    @Test
    fun `une source en echec n'empeche pas les autres de charger`() = runBlocking {
        val repository = CatalogRepository(
            listOf(
                FakeSource("ok", listOf(entry("a"), entry("b"))),
                FakeSource("ko", error = IllegalStateException("injoignable")),
            ),
        )

        val snapshot = repository.refresh()

        assertEquals(listOf("a", "b"), snapshot.entries.map { it.id })
        assertEquals(1, snapshot.failures.size)
        assertEquals("ko", snapshot.failures.single().sourceId)
        assertEquals("injoignable", snapshot.failures.single().message)
        assertEquals(snapshot, repository.snapshot.value)
    }

    @Test
    fun `un rafraichissement relit les sources actives`() = runBlocking {
        val bundled = FakeSource("bundled", listOf(entry("a")))
        val personal = FakeSource("personal", listOf(entry("b")))
        var active = listOf(bundled)
        val repository = CatalogRepository { active }

        assertEquals(listOf("a"), repository.refresh().entries.map { it.id })
        active = listOf(bundled, personal)
        assertEquals(listOf("a", "b"), repository.refresh().entries.map { it.id })
        active = listOf(bundled)
        assertEquals(listOf("a"), repository.refresh().entries.map { it.id })
    }

    @Test
    fun `les doublons d'identifiant sont ignores`() = runBlocking {
        val repository = CatalogRepository(
            listOf(FakeSource("1", listOf(entry("a"))), FakeSource("2", listOf(entry("a"), entry("c")))),
        )
        assertEquals(listOf("a", "c"), repository.refresh().entries.map { it.id })
    }

    @Test
    fun `regroupement par type et categorie dans l'ordre d'apparition`() {
        val snapshot = CatalogSnapshot(
            listOf(
                entry("1", category = "Sport"),
                entry("2", category = "Info"),
                entry("3", ContentType.VOD, "Films"),
                entry("4", category = "Sport"),
            ),
        )
        val live = snapshot.byCategory(ContentType.LIVE)
        assertEquals(listOf("Sport", "Info"), live.keys.toList())
        assertEquals(listOf("1", "4"), live.getValue("Sport").map { it.id })
        assertEquals(listOf("3"), snapshot.vod.map { it.id })
        assertEquals("2", snapshot.find("2")?.id)
        assertNull(snapshot.find("absent"))
        assertTrue(snapshot.failures.isEmpty())
    }
}
