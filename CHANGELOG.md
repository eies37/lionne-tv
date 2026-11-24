# CHANGELOG — Lionne TV

Toutes les modifications notables du projet sont consignées ici.
Format inspiré de [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/). Versions selon [Semantic Versioning](https://semver.org/lang/fr/).

Chaque entrée indique l'auteur (Claude, ChatGPT ou eies37) et l'ID de tâche.

## [Non publié]

### Ajouté
- Socle de l'application **Android TV / Google TV** en Kotlin, découpé en modules Gradle : `core/model`, `core/data`, `core/storage`, `core/playback`, `core/designsystem`, `source/jsoncatalog`, `source/m3u`, `feature/home`, `feature/player`, `app`. (Claude, T-006)
- Sources de contenu interchangeables déclarées dans `app/src/main/assets/sources.json`. Une source en échec n'empêche pas l'affichage des autres. (Claude, T-006)
- Source `json` (catalogue direct + VOD) et source `m3u` (listes IPTV : `group-title`, `tvg-logo`, `#EXTGRP`, BOM, fins de ligne Windows). (Claude, T-006)
- Catalogue de démonstration : flux de test publics et films libres Blender. (Claude, T-006)
- Lecteur Media3 (HLS, DASH, MP4, MPEG-TS) avec zapping ▲/▼ et CH+/CH−, lecture/pause et avance/recul de 10 s, reprise des vidéos là où on s'était arrêté. (Claude, T-006)
- Favoris (appui long sur OK), stockés localement. (Claude, T-006)
- Bandeau du lecteur commun au direct et à la VOD : titre, catégorie, position « 4:05 / 9:56 », état « En pause », aide des touches. (Claude, T-006)
- Accueil en rangées (favoris, direct par catégorie, VOD par catégorie) et lecteur plein écran avec bandeau, en version minimale. (Claude, T-006)
- Thème configurable `BrandConfig` (couleurs et nom provisoires). Icône et bannière TV provisoires. (Claude, T-006)
- Tests unitaires : catalogue, sources, M3U, JSON, zapping, reprise, rangées de l'accueil. (Claude, T-006)
- CI GitHub Actions : tests, compilation debug et release (R8), APK de debug téléchargeable. (Claude, T-008)
- Test automatique sur émulateur Android : accueil, lecture d'une chaîne, zapping, retour, rangée VOD, recherche de plantages, disponibilité des flux de démonstration. (Claude, T-008)
- Publication d'un APK de test téléchargeable depuis la TV, déclenchée par une étiquette `v*`. (Claude, T-008)
- `README.md` : installation, touches de la télécommande, compilation. (Claude, T-006)
- `COMMUNICATION.md` : journal des échanges Claude ↔ ChatGPT, avec règles d'usage et premier message de Claude. (Claude, T-001)
- `TASKS.md` : tableau de répartition des tâches. (Claude, T-001)
- `PROJECT.md` : cahier des charges, architecture modulaire, conventions et journal des décisions. (Claude, T-001, T-005)
- `CHANGELOG.md` : ce fichier. (Claude, T-001)

### Corrigé
- Au retour du lecteur, le focus revient sur le contenu qu'on vient de quitter (avant : aucune vignette sélectionnée). Détecté par le test sur émulateur. (Claude, T-006)
