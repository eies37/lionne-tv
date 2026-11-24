package com.lionne.tv.core.data

import com.lionne.tv.core.model.ContentSource
import com.lionne.tv.core.model.MediaEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SourcesTest {

    private val noLoader = TextLoader { error("non utilisé") }

    @Test
    fun `lecture de sources_json`() {
        val config = SourcesConfig.parse(
            """
            { "sources": [
              { "type": "json", "id": "demo", "name": "Démo", "location": "asset://demo.json" },
              { "type": "m3u", "id": "x", "name": "X", "location": "https://x/l.m3u", "enabled": false, "inconnu": 1 }
            ] }
            """.trimIndent(),
        )
        assertEquals(2, config.sources.size)
        assertTrue(config.sources[0].enabled)
        assertEquals(false, config.sources[1].enabled)
    }

    @Test
    fun `seules les sources actives sont creees et un type inconnu echoue proprement`() = runBlocking {
        val registry = SourceRegistry(
            mapOf(
                "json" to SourceFactory { d, _ ->
                    object : ContentSource {
                        override val id = d.id
                        override val name = d.name
                        override suspend fun load(): List<MediaEntry> = emptyList()
                    }
                },
            ),
        )
        val config = SourcesConfig(
            listOf(
                SourceDescriptor("json", "a", "A", "asset://a"),
                SourceDescriptor("json", "b", "B", "asset://b", enabled = false),
                SourceDescriptor("xtream", "c", "C", "https://c"),
            ),
        )

        val sources = registry.create(config, noLoader)
        assertEquals(listOf("a", "c"), sources.map { it.id })

        val snapshot = CatalogRepository(sources).refresh()
        assertEquals(listOf("c"), snapshot.failures.map { it.sourceId })
        assertTrue(snapshot.failures.single().message.contains("xtream"))
    }

    @Test
    fun `le chargeur composite choisit selon le schema`() = runBlocking {
        val loader = CompositeTextLoader(mapOf("asset" to TextLoader { "contenu de $it" }))
        assertEquals("contenu de asset://a.json", loader.load("asset://a.json"))
        val failure = runCatching { loader.load("ftp://x") }.exceptionOrNull()
        assertTrue(failure is java.io.IOException)
    }
}
