package com.lionne.tv.core.model

/** Nature d'un contenu : chaîne en direct ou vidéo à la demande. */
enum class ContentType { LIVE, VOD }

/**
 * Un élément lisible, quelle que soit sa source (catalogue JSON, liste M3U, futur backend…).
 *
 * [id] est unique dans tout le catalogue : il est préfixé par l'identifiant de la source.
 */
data class MediaEntry(
    val id: String,
    val title: String,
    val streamUrl: String,
    val type: ContentType,
    val category: String,
    val sourceId: String,
    val description: String? = null,
    val logoUrl: String? = null,
    val posterUrl: String? = null,
)

/**
 * Contrat que doit respecter tout module de source de contenu.
 *
 * Pour ajouter un format (Xtream, backend maison, fichiers locaux…), créer un module
 * `source/<format>` qui implémente cette interface et une `SourceFactory` (voir core:data).
 */
interface ContentSource {
    val id: String
    val name: String

    /** Charge les contenus. Peut lever une exception : le catalogue l'isole des autres sources. */
    suspend fun load(): List<MediaEntry>
}
