package com.lionne.tv.core.data

import com.lionne.tv.core.model.ContentType
import com.lionne.tv.core.model.MediaEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackTest {

    private val channels = listOf("a", "b", "c").map {
        MediaEntry(it, it, "https://x/$it", ContentType.LIVE, "Info", "s")
    }

    @Test
    fun `zapping avance et recule en boucle`() {
        assertEquals("b", Zapping.move(channels, "a", +1)?.id)
        assertEquals("a", Zapping.move(channels, "c", +1)?.id)
        assertEquals("c", Zapping.move(channels, "a", -1)?.id)
        assertEquals("b", Zapping.move(channels, "inconnu", +1)?.id)
        assertNull(Zapping.move(emptyList(), "a", +1))
    }

    @Test
    fun `reprise ignoree au debut et a la fin`() {
        assertNull(ResumePolicy.positionToStore(5_000, 600_000))
        assertNull(ResumePolicy.positionToStore(590_000, 600_000))
        assertEquals(120_000L, ResumePolicy.positionToStore(120_000, 600_000))
        assertEquals(120_000L, ResumePolicy.positionToStore(120_000, -1))
    }

    @Test
    fun `favoris en memoire`() {
        val store = InMemoryFavoritesStore()
        assertTrue(store.toggle("a"))
        assertEquals(setOf("a"), store.ids())
        assertFalse(store.toggle("a"))
        assertTrue(store.ids().isEmpty())
    }

    @Test
    fun `affichage des durees`() {
        assertEquals("0:00", TimeFormat.format(0))
        assertEquals("4:05", TimeFormat.format(245_000))
        assertEquals("1:02:09", TimeFormat.format(3_729_000))
        assertEquals("0:00", TimeFormat.format(-5))
    }
}
