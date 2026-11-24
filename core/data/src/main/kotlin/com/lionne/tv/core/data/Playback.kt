package com.lionne.tv.core.data

import com.lionne.tv.core.model.MediaEntry

/** Changement de chaîne (zapping) dans une liste, en boucle. */
object Zapping {
    /**
     * Renvoie la chaîne située à [step] positions de [currentId] (négatif = chaîne précédente).
     * Si [currentId] est introuvable, part du début de la liste.
     */
    fun move(channels: List<MediaEntry>, currentId: String, step: Int): MediaEntry? {
        if (channels.isEmpty()) return null
        val index = channels.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
        val target = Math.floorMod(index + step, channels.size)
        return channels[target]
    }
}

/** Règle de reprise de lecture des vidéos à la demande. */
object ResumePolicy {
    private const val MIN_POSITION_MS = 10_000L
    private const val END_THRESHOLD = 0.95

    /**
     * Position à mémoriser, ou `null` s'il faut oublier la position
     * (vidéo à peine commencée, ou presque terminée).
     */
    fun positionToStore(positionMs: Long, durationMs: Long): Long? {
        if (positionMs < MIN_POSITION_MS) return null
        if (durationMs > 0 && positionMs >= durationMs * END_THRESHOLD) return null
        return positionMs
    }
}

/** Favoris de l'utilisateur. */
interface FavoritesStore {
    fun ids(): Set<String>

    /** Ajoute ou retire [id] ; renvoie `true` si le contenu est désormais en favori. */
    fun toggle(id: String): Boolean
}

/** Positions de lecture des vidéos à la demande. */
interface PlaybackPositionStore {
    fun get(id: String): Long
    fun save(id: String, positionMs: Long)
    fun clear(id: String)
}

class InMemoryFavoritesStore : FavoritesStore {
    private val ids = LinkedHashSet<String>()
    override fun ids(): Set<String> = ids.toSet()
    override fun toggle(id: String): Boolean = if (ids.remove(id)) false else ids.add(id)
}

class InMemoryPlaybackPositionStore : PlaybackPositionStore {
    private val positions = HashMap<String, Long>()
    override fun get(id: String): Long = positions[id] ?: 0L
    override fun save(id: String, positionMs: Long) { positions[id] = positionMs }
    override fun clear(id: String) { positions.remove(id) }
}
