package com.lionne.tv.core.data

import com.lionne.tv.core.model.ContentSource
import com.lionne.tv.core.model.MediaEntry
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Une source déclarée dans `sources.json`.
 *
 * - [type] choisit le module qui sait la lire (`json`, `m3u`…).
 * - [location] est l'adresse du contenu (`asset://…`, `https://…`).
 */
@Serializable
data class SourceDescriptor(
    val type: String,
    val id: String,
    val name: String,
    val location: String,
    val enabled: Boolean = true,
)

@Serializable
data class SourcesConfig(val sources: List<SourceDescriptor> = emptyList()) {
    fun encode(): String = json.encodeToString(serializer(), this)

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun parse(text: String): SourcesConfig = json.decodeFromString(serializer(), text)
    }
}

/** Fabrique d'un type de source. Chaque module `source/<format>` en fournit une. */
fun interface SourceFactory {
    fun create(descriptor: SourceDescriptor, loader: TextLoader): ContentSource
}

/**
 * Registre des types de sources disponibles dans l'application.
 * Ajouter un format = enregistrer sa fabrique ici, sans toucher au reste du code.
 */
class SourceRegistry(private val factories: Map<String, SourceFactory>) {

    val supportedTypes: Set<String> get() = factories.keys

    fun create(config: SourcesConfig, loader: TextLoader): List<ContentSource> =
        config.sources
            .filter { it.enabled }
            .map { descriptor ->
                val factory = factories[descriptor.type.lowercase()]
                factory?.create(descriptor, loader) ?: UnsupportedSource(descriptor, supportedTypes)
            }
}

/** Source dont le type n'est pas installé : elle échoue proprement, sans bloquer les autres. */
internal class UnsupportedSource(
    private val descriptor: SourceDescriptor,
    private val supported: Set<String>,
) : ContentSource {
    override val id: String get() = descriptor.id
    override val name: String get() = descriptor.name
    override suspend fun load(): List<MediaEntry> =
        throw IllegalArgumentException(
            "Type de source « ${descriptor.type} » non pris en charge (disponibles : ${supported.sorted().joinToString()})",
        )
}
