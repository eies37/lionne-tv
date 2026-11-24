package com.lionne.tv.source.jsoncatalog

import com.lionne.tv.core.data.SourceDescriptor
import com.lionne.tv.core.data.SourceFactory
import com.lionne.tv.core.data.TextLoader
import com.lionne.tv.core.model.ContentSource
import com.lionne.tv.core.model.ContentType
import com.lionne.tv.core.model.MediaEntry
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Catalogue au format JSON Lionne TV : chaînes en direct et vidéos à la demande.
 *
 * ```json
 * { "entries": [
 *   { "id": "info", "title": "Lionne Info", "type": "LIVE", "category": "Info",
 *     "url": "https://…/master.m3u8", "logo": "https://…/logo.png" }
 * ] }
 * ```
 */
class JsonCatalogSource(
    override val id: String,
    override val name: String,
    private val location: String,
    private val loader: TextLoader,
) : ContentSource {
    override suspend fun load(): List<MediaEntry> = JsonCatalogParser.parse(loader.load(location), id)
}

/** Fabrique enregistrée sous le type `json` dans `sources.json`. */
object JsonCatalogSourceFactory : SourceFactory {
    const val TYPE = "json"

    override fun create(descriptor: SourceDescriptor, loader: TextLoader): ContentSource =
        JsonCatalogSource(descriptor.id, descriptor.name, descriptor.location, loader)
}

object JsonCatalogParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String, sourceId: String): List<MediaEntry> {
        val catalog = json.decodeFromString(JsonCatalog.serializer(), text)
        return catalog.entries
            .filter { it.url.isNotBlank() && it.title.isNotBlank() }
            .map { entry ->
                MediaEntry(
                    id = "$sourceId:${entry.id ?: entry.url}",
                    title = entry.title,
                    streamUrl = entry.url,
                    type = parseType(entry.type),
                    category = entry.category?.takeIf { it.isNotBlank() } ?: DEFAULT_CATEGORY,
                    sourceId = sourceId,
                    description = entry.description,
                    logoUrl = entry.logo,
                    posterUrl = entry.poster,
                )
            }
    }

    private fun parseType(raw: String?): ContentType = when (raw?.trim()?.uppercase()) {
        "VOD", "MOVIE", "FILM", "SERIES" -> ContentType.VOD
        else -> ContentType.LIVE
    }

    const val DEFAULT_CATEGORY = "Général"
}

@Serializable
internal data class JsonCatalog(val entries: List<JsonEntry> = emptyList())

@Serializable
internal data class JsonEntry(
    val id: String? = null,
    val title: String = "",
    val url: String = "",
    val type: String? = null,
    val category: String? = null,
    val description: String? = null,
    val logo: String? = null,
    val poster: String? = null,
)
