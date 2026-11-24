package com.lionne.tv.core.data

/** Configuration des listes personnelles, indépendante de l’interface et du format de stockage. */
interface UserPlaylistStore {
    fun read(): List<SourceDescriptor>
    fun save(sources: List<SourceDescriptor>)
}
