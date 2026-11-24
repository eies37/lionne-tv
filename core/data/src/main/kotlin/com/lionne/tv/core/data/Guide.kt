package com.lionne.tv.core.data

import com.lionne.tv.core.model.GuideSource
import com.lionne.tv.core.model.Programme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Émission en cours et suivante d'une chaîne. */
data class NowNext(val now: Programme?, val next: Programme?)

/** Fabrique d'un type de guide. Chaque module de guide (`source/xmltv`…) en fournit une. */
fun interface GuideFactory {
    fun create(descriptor: SourceDescriptor, loader: TextLoader): GuideSource
}

/** Registre des formats de guide installés, sur le modèle de [SourceRegistry]. */
class GuideRegistry(private val factories: Map<String, GuideFactory>) {
    fun create(descriptors: List<SourceDescriptor>, loader: TextLoader): List<GuideSource> =
        descriptors
            .filter { it.enabled }
            .map { descriptor ->
                factories[descriptor.type.lowercase()]?.create(descriptor, loader)
                    ?: UnsupportedGuide(descriptor, factories.keys)
            }
}

private class UnsupportedGuide(
    private val descriptor: SourceDescriptor,
    private val supported: Set<String>,
) : GuideSource {
    override val id: String get() = descriptor.id
    override val name: String get() = descriptor.name
    override suspend fun load(): List<Programme> =
        throw IllegalArgumentException(
            "Type de guide « ${descriptor.type} » non pris en charge (disponibles : ${supported.sorted().joinToString()})",
        )
}

/**
 * Guide des programmes agrégé. Les chaînes sont reliées au guide par [com.lionne.tv.core.model.MediaEntry.epgId]
 * (sans tenir compte des majuscules). Un guide en échec n'empêche pas les autres de charger.
 */
class GuideRepository(
    private val sources: List<GuideSource>,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val _programmes = MutableStateFlow<Map<String, List<Programme>>>(emptyMap())

    /** Programmes par identifiant de chaîne (en minuscules), triés par heure de début. */
    val programmes: StateFlow<Map<String, List<Programme>>> = _programmes.asStateFlow()

    val isEmpty: Boolean get() = _programmes.value.isEmpty()

    suspend fun refresh(): List<SourceFailure> = coroutineScope {
        val results = sources.map { source ->
            async<Result<List<Programme>>> {
                try {
                    Result.success(source.load())
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
        }.awaitAll()

        val failures = mutableListOf<SourceFailure>()
        val all = mutableListOf<Programme>()
        results.forEachIndexed { index, result ->
            result.onSuccess { all += it }.onFailure { error ->
                val source = sources[index]
                failures += SourceFailure(source.id, source.name, error.message ?: error::class.simpleName.orEmpty())
            }
        }
        _programmes.value = all
            .groupBy { key(it.channelId) }
            .mapValues { (_, list) -> list.distinctBy { it.startMs }.sortedBy { it.startMs } }
        failures
    }

    fun nowNext(epgId: String?, nowMs: Long = clock()): NowNext? {
        val list = epgId?.let { _programmes.value[key(it)] } ?: return null
        val now = list.lastOrNull { it.startMs <= nowMs && nowMs < it.endMs }
        val next = list.firstOrNull { it.startMs >= (now?.endMs ?: nowMs) }
        if (now == null && next == null) return null
        return NowNext(now, next)
    }

    private fun key(channelId: String) = channelId.trim().lowercase()
}
