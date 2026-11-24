package com.lionne.tv.source.jsoncatalog

import com.lionne.tv.core.data.SourceDescriptor
import com.lionne.tv.core.data.TextLoader
import com.lionne.tv.core.model.ContentType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class JsonCatalogParserTest {

    private val sample = """
        { "version": 1, "entries": [
          { "id": "info", "title": "Info", "type": "LIVE", "category": "Actu", "url": "https://x/info.m3u8", "logo": "https://x/l.png" },
          { "id": "film", "title": "Film", "type": "vod", "url": "https://x/film.mpd", "poster": "https://x/p.jpg" },
          { "title": "Sans adresse", "url": "" }
        ] }
    """.trimIndent()

    @Test
    fun `lecture du catalogue`() {
        val entries = JsonCatalogParser.parse(sample, "demo")

        assertEquals(2, entries.size)
        assertEquals("demo:info", entries[0].id)
        assertEquals(ContentType.LIVE, entries[0].type)
        assertEquals("Actu", entries[0].category)
        assertEquals("https://x/l.png", entries[0].logoUrl)
        assertEquals(ContentType.VOD, entries[1].type)
        assertEquals(JsonCatalogParser.DEFAULT_CATEGORY, entries[1].category)
        assertEquals("https://x/p.jpg", entries[1].posterUrl)
    }

    @Test
    fun `la fabrique cree une source qui passe par le chargeur`() = runBlocking {
        val source = JsonCatalogSourceFactory.create(
            SourceDescriptor("json", "demo", "Démo", "asset://demo.json"),
            TextLoader { sample },
        )
        assertEquals("demo", source.id)
        assertEquals(2, source.load().size)
    }
}
