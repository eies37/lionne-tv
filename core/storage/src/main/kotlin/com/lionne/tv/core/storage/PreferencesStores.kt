package com.lionne.tv.core.storage

import android.content.Context
import android.content.SharedPreferences
import com.lionne.tv.core.data.FavoritesStore
import com.lionne.tv.core.data.PlaybackPositionStore

/** Favoris stockés dans les préférences Android (quelques Ko au plus). */
class PreferencesFavoritesStore(context: Context) : FavoritesStore {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("lionne_favorites", Context.MODE_PRIVATE)

    override fun ids(): Set<String> = prefs.getStringSet(KEY, emptySet()).orEmpty().toSet()

    override fun toggle(id: String): Boolean {
        val current = ids().toMutableSet()
        val nowFavorite = if (current.remove(id)) false else current.add(id)
        prefs.edit().putStringSet(KEY, current).apply()
        return nowFavorite
    }

    private companion object {
        const val KEY = "ids"
    }
}

/**
 * Positions de reprise des vidéos, bornées à [maxEntries] éléments :
 * les plus anciennes sont oubliées pour ne pas encombrer le stockage de la TV.
 */
class PreferencesPlaybackPositionStore(
    context: Context,
    private val maxEntries: Int = 200,
) : PlaybackPositionStore {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("lionne_positions", Context.MODE_PRIVATE)

    override fun get(id: String): Long =
        prefs.getString(id, null)?.substringBefore('|')?.toLongOrNull() ?: 0L

    override fun save(id: String, positionMs: Long) {
        val editor = prefs.edit().putString(id, "$positionMs|${System.currentTimeMillis()}")
        val all = prefs.all
        if (!all.containsKey(id) && all.size >= maxEntries) {
            all.entries
                .sortedBy { (it.value as? String)?.substringAfter('|')?.toLongOrNull() ?: 0L }
                .take(all.size - maxEntries + 1)
                .forEach { editor.remove(it.key) }
        }
        editor.apply()
    }

    override fun clear(id: String) {
        prefs.edit().remove(id).apply()
    }
}
