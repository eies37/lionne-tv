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
    /** Identifiant de la chaîne dans le guide des programmes (`tvg-id` des listes M3U). */
    val epgId: String? = null,
)

/** Une émission du guide des programmes. Heures en millisecondes depuis l'époque Unix (UTC). */
data class Programme(
    val channelId: String,
    val title: String,
    val startMs: Long,
    val endMs: Long,
    val description: String? = null,
)

/**
 * Contrat d'un module de guide des programmes (XMLTV, API d'un fournisseur…).
 * Même principe que [ContentSource] : un module `source/<format>` par format.
 */
interface GuideSource {
    val id: String
    val name: String
    suspend fun load(): List<Programme>
}

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

    /**
     * Adresses de guides des programmes annoncées par la source elle-même
     * (par exemple `url-tvg` dans l'en-tête d'une liste M3U), connues après [load].
     */
    val guideLocations: List<String> get() = emptyList()
}
