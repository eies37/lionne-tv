package com.lionne.tv.core.data

import com.lionne.tv.core.model.ContentSource
import com.lionne.tv.core.model.ContentType
import com.lionne.tv.core.model.MediaEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.withLock

/** Échec de chargement d'une source, affichable à l'utilisateur. */
data class SourceFailure(val sourceId: String, val sourceName: String, val message: String)

/** État du catalogue à un instant donné : tous les contenus et les sources en échec. */
data class CatalogSnapshot(
    val entries: List<MediaEntry>,
    val failures: List<SourceFailure> = emptyList(),
) {
    private val byId: Map<String, MediaEntry> by lazy { entries.associateBy { it.id } }

    val live: List<MediaEntry> get() = entries.filter { it.type == ContentType.LIVE }
    val vod: List<MediaEntry> get() = entries.filter { it.type == ContentType.VOD }

    fun find(id: String): MediaEntry? = byId[id]

    /** Contenus d'un type regroupés par catégorie, dans l'ordre d'apparition. */
    fun byCategory(type: ContentType): Map<String, List<MediaEntry>> =
        entries.filter { it.type == type }.groupBy { it.category }
}

/**
 * Agrège toutes les sources actives. Les sources sont chargées en parallèle et isolées :
 * une liste M3U injoignable n'empêche pas le reste du catalogue de s'afficher.
 */
class CatalogRepository(private val sources: () -> List<ContentSource>) {
    constructor(sources: List<ContentSource>) : this({ sources })

    private val refreshLock = kotlinx.coroutines.sync.Mutex()

    private val _snapshot = MutableStateFlow<CatalogSnapshot?>(null)
    val snapshot: StateFlow<CatalogSnapshot?> = _snapshot.asStateFlow()

    suspend fun refresh(): CatalogSnapshot = refreshLock.withLock { loadSnapshot() }

    private suspend fun loadSnapshot(): CatalogSnapshot = coroutineScope {
        val sources = sources()
        val results = sources.map { source ->
            async<Result<List<MediaEntry>>> {
                try {
                    Result.success(source.load())
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
        }.awaitAll()

        val entries = mutableListOf<MediaEntry>()
        val seenIds = HashSet<String>()
        val failures = mutableListOf<SourceFailure>()
        results.forEachIndexed { index, result ->
            val source = sources[index]
            result
                .onSuccess { loaded -> loaded.filterTo(entries) { seenIds.add(it.id) } }
                .onFailure { error ->
                    failures += SourceFailure(
                        sourceId = source.id,
                        sourceName = source.name,
                        message = error.message ?: error::class.simpleName.orEmpty(),
                    )
                }
        }
        CatalogSnapshot(entries, failures).also { _snapshot.value = it }
    }
}
