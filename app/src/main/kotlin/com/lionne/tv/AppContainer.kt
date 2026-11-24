package com.lionne.tv

import android.content.Context
import com.lionne.tv.core.data.CatalogRepository
import com.lionne.tv.core.data.CompositeTextLoader
import com.lionne.tv.core.data.FavoritesStore
import com.lionne.tv.core.data.GuideRegistry
import com.lionne.tv.core.data.GuideRepository
import com.lionne.tv.core.data.HttpTextLoader
import com.lionne.tv.core.data.PlaybackPositionStore
import com.lionne.tv.core.data.SourceRegistry
import com.lionne.tv.core.data.SourcesConfig
import com.lionne.tv.core.data.TextLoader
import com.lionne.tv.core.storage.PreferencesFavoritesStore
import com.lionne.tv.core.storage.PreferencesPlaybackPositionStore
import com.lionne.tv.source.jsoncatalog.JsonCatalogSourceFactory
import com.lionne.tv.source.m3u.M3uSourceFactory
import com.lionne.tv.source.xmltv.XmltvGuideFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Point d'assemblage de l'application : c'est ici qu'on branche ou débranche les modules.
 * Pas de framework d'injection, pour rester simple et léger.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    /** Types de sources installés. Ajouter un format = une ligne ici + son module dans build.gradle.kts. */
    private val sourceRegistry = SourceRegistry(
        mapOf(
            JsonCatalogSourceFactory.TYPE to JsonCatalogSourceFactory,
            M3uSourceFactory.TYPE to M3uSourceFactory,
        ),
    )

    private val textLoader: TextLoader = CompositeTextLoader(
        mapOf(
            "asset" to AssetTextLoader(appContext),
            "http" to HttpTextLoader(),
            "https" to HttpTextLoader(),
        ),
    )

    val favorites: FavoritesStore = PreferencesFavoritesStore(appContext)
    val positions: PlaybackPositionStore = PreferencesPlaybackPositionStore(appContext)

    /** Formats de guide des programmes installés. */
    private val guideRegistry = GuideRegistry(
        mapOf(XmltvGuideFactory.TYPE to XmltvGuideFactory),
    )

    private val config: SourcesConfig by lazy {
        appContext.assets.open(SOURCES_FILE).bufferedReader().use { SourcesConfig.parse(it.readText()) }
    }

    /** Sources déclarées dans `assets/sources.json` (section `sources`). */
    val catalog: CatalogRepository by lazy {
        CatalogRepository(sourceRegistry.create(config, textLoader))
    }

    /** Guides des programmes déclarés dans `assets/sources.json` (section `guides`). */
    val guide: GuideRepository by lazy {
        GuideRepository(guideRegistry.create(config.guides, textLoader))
    }

    private companion object {
        const val SOURCES_FILE = "sources.json"
    }
}

/** Lit les fichiers embarqués dans l'APK : `asset://catalog-demo.json`. */
private class AssetTextLoader(private val context: Context) : TextLoader {
    override suspend fun load(location: String): String = withContext(Dispatchers.IO) {
        val path = location.substringAfter("asset://")
        try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw IOException("Fichier embarqué introuvable : $path", e)
        }
    }
}
