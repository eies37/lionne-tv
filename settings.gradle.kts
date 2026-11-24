pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "lionne-tv"

// Application : assemble les modules ci-dessous.
include(":app")

// Socle partagé.
include(":core:model")        // Kotlin pur : entités et contrat ContentSource
include(":core:data")         // Kotlin pur : registre de sources, catalogue, zapping, reprise
include(":core:storage")      // Android : favoris et positions de lecture
include(":core:playback")     // Android : lecteur Media3 (HLS, DASH, MP4)
include(":core:designsystem") // Android : thème, marque, composants TV

// Sources de contenu interchangeables : un module par format.
include(":source:jsoncatalog") // catalogue JSON (direct + VOD)
include(":source:m3u")         // listes IPTV M3U / M3U8

// Écrans.
include(":feature:home")
include(":feature:player")
