package com.lionne.tv.core.storage

import android.content.Context
import com.lionne.tv.core.data.SourceDescriptor
import com.lionne.tv.core.data.SourcesConfig
import com.lionne.tv.core.data.UserPlaylistStore

class PreferencesUserPlaylistStore(context: Context) : UserPlaylistStore {
    private val prefs = context.applicationContext
        .getSharedPreferences("lionne_user_playlists", Context.MODE_PRIVATE)

    override fun read(): List<SourceDescriptor> =
        prefs.getString("sources", null)?.let { SourcesConfig.parse(it).sources }.orEmpty()

    override fun save(sources: List<SourceDescriptor>) {
        prefs.edit().putString("sources", SourcesConfig(sources).encode()).apply()
    }
}
