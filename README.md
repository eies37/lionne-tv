# Lionne TV

Application de télévision pour **Android TV / Google TV** : chaînes en direct, vidéo à la demande et listes IPTV (M3U), pilotable entièrement à la télécommande.

Projet développé à deux IA, Claude et ChatGPT, sous la direction d'eies37. Voir `PROJECT.md` (architecture), `TASKS.md` (tâches) et `COMMUNICATION.md` (échanges).

## Installer l'APK de test

1. Dans l'onglet **Actions** du dépôt, ouvrez la dernière exécution verte de « Android CI ».
2. Téléchargez l'artefact `lionne-tv-debug-apk` et décompressez-le.
3. Installez-le sur la TV :
   - par câble ou réseau : `adb connect <ip-de-la-tv>` puis `adb install app-debug.apk` ;
   - ou avec une application comme « Downloader » ou « Send Files to TV ».

L'application apparaît dans la rangée des applications de la TV.

## Utilisation

| Écran | Touche | Action |
|-------|--------|--------|
| Accueil | ◀ ▶ ▲ ▼ | Se déplacer |
| Accueil | OK | Lire |
| Accueil | OK (appui long) | Ajouter / retirer des favoris |
| Direct | ▲ ▼ ou CH+ CH− | Chaîne suivante / précédente |
| Direct | OK | Afficher le bandeau d'information |
| Vidéo à la demande | OK ou ⏯ | Lecture / pause |
| Vidéo à la demande | ◀ ▶ | Reculer / avancer de 10 s |
| Partout | Retour | Revenir à l'accueil |

## Ajouter une liste de chaînes

Modifiez `app/src/main/assets/sources.json` :

```json
{ "type": "m3u", "id": "ma-liste", "name": "Ma liste", "location": "https://…/liste.m3u", "enabled": true }
```

Puis recompilez. L'ajout d'une liste depuis la TV, sans recompiler, est prévu (tâche T-009).

Lionne TV ne fournit aucune chaîne : n'ajoutez que des listes dont vous avez les droits.

## Compiler

Prérequis : JDK 17 et le SDK Android (Android Studio Ladybug ou plus récent).

```bash
./gradlew test           # tests unitaires
./gradlew assembleDebug  # APK : app/build/outputs/apk/debug/
```

## Architecture modulaire

```
app/              assemblage
feature/home      accueil          feature/player   lecteur
source/jsoncatalog catalogue JSON  source/m3u       listes IPTV
core/designsystem thème et marque  core/playback    Media3
core/storage      favoris, reprise core/data        catalogue, sources (Kotlin pur)
core/model        entités (Kotlin pur)
```

Pour ajouter un format de source, un écran ou changer de thème, voir `PROJECT.md` § 4.
