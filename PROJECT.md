# PROJECT — Lionne TV

Cahier des charges et architecture. Document vivant : tout point marqué **[À CONFIRMER]** reste une hypothèse tant qu'eies37 ne l'a pas validé.

## 1. Vision

Lionne TV est une application de télévision pour **Android TV / Google TV** (Sony, TCL, Philips, Hisense sous Google TV, Chromecast avec Google TV, box Android…).

- **Exigence clé d'eies37 : l'application doit être modulable.** On doit pouvoir ajouter, retirer ou remplacer une source de contenu, un écran, le lecteur ou l'identité visuelle sans réécrire le reste.
- Objectif principal, public visé, ce qui la distingue : _à définir_ **[À CONFIRMER]**

## 2. Périmètre fonctionnel

Décision d'eies37 (2026-11-24) : la première version couvre **tous les types de contenu**.

| Fonction | Version | Statut |
|----------|---------|--------|
| Chaînes en direct (HLS, DASH, MPEG-TS) | MVP | Fait (T-006) |
| Zapping ▲/▼ et CH+/CH− | MVP | Fait (T-006) |
| Vidéo à la demande avec reprise de lecture | MVP | Fait (T-006) |
| Catalogue JSON (direct + VOD) | MVP | Fait (T-006) |
| Listes IPTV M3U / M3U8 | MVP | Fait (T-006), ajout depuis l'écran : T-009 |
| Favoris (appui long sur OK) | MVP | Fait (T-006) |
| Interface 10-foot définitive | MVP | À faire (T-007, ChatGPT) |
| Logos et affiches | MVP | Fait (T-010) |
| Guide des programmes (EPG, XMLTV) | MVP | Fait (T-011) |
| Recherche | Plus tard | À planifier |
| Comptes / profils / backend | Plus tard | **[À CONFIRMER]** (Q3) |
| Contrôle parental | Plus tard | **[À CONFIRMER]** |

### Hors périmètre

- Lionne TV ne fournit aucune liste de chaînes. Le catalogue de démonstration n'utilise que des flux de test publics et des films libres (Blender, CC BY). Les listes M3U ajoutées relèvent de la responsabilité de l'utilisateur, qui doit détenir les droits de diffusion.

## 3. Exigences non fonctionnelles

- **Navigation entièrement à la télécommande** (touches directionnelles, OK, retour, CH+/CH−, touches média) : focus toujours visible.
- **Interface 10-foot :** textes lisibles à 3 m, marges de sécurité de l'écran (48 dp × 27 dp).
- **Faible empreinte :** les TV ont peu de stockage. Pas de bibliothèque superflue (ni injection de dépendances, ni client HTTP externe), R8 et réduction des ressources en release, positions de lecture bornées à 200 entrées. Cible : APK release de moins de 15 Mo. **[À CONFIRMER]**
- **Robustesse :** une source injoignable n'empêche jamais les autres de s'afficher. L'erreur est affichée à l'écran.
- **Compatibilité :** Android 5.0 (API 21) et plus.
- **Langue :** interface en français. Internationalisation : **[À CONFIRMER]**

## 4. Architecture (décidée : D-002, D-003)

Application **native Kotlin** : Jetpack Compose for TV pour l'interface, Media3/ExoPlayer pour la lecture. Le projet est découpé en **modules Gradle indépendants**.

```
app/                    Assemblage : branche les modules entre eux (AppContainer)
│
├── feature/home        Écran d'accueil (rangées favoris / direct / VOD)
├── feature/player      Lecteur plein écran (zapping, reprise, bandeau)
│
├── source/jsoncatalog  Source « json » : catalogue Lionne TV
├── source/m3u          Source « m3u » : listes IPTV
├── source/xmltv        Guide « xmltv » : programmes en cours et suivants
│
├── core/designsystem   Thème, marque (BrandConfig), composants TV
├── core/playback       LionnePlayer : seule porte d'entrée vers Media3
├── core/storage        Favoris et positions (préférences Android)
├── core/data           Kotlin pur : registre de sources, catalogue, zapping, reprise
└── core/model          Kotlin pur : MediaEntry, ContentSource
```

**Règles de dépendance :**
- `core/model` et `core/data` sont en Kotlin pur : aucune dépendance Android, testables sur la JVM.
- Un module `source/*` ne dépend que de `core/data`. Un module `feature/*` ne dépend jamais d'un autre `feature/*` ni d'une `source/*`.
- Seul `app` connaît tous les modules.

### Comment l'application est modulable

| Je veux… | Ce que je change |
|----------|------------------|
| Ajouter, retirer ou désactiver une source | `app/src/main/assets/sources.json` (sans recompiler le code) |
| Ajouter un **format** de source (Xtream, backend maison…) | Nouveau module `source/<format>` qui implémente `ContentSource` et `SourceFactory`, puis une ligne dans `AppContainer` |
| Changer les couleurs ou le nom affiché | `BrandConfig` dans `core/designsystem` |
| Refaire un écran | Son module `feature/*` uniquement |
| Changer de moteur de lecture | `core/playback` uniquement |
| Stocker favoris et positions ailleurs (cloud, compte) | Nouvelle implémentation de `FavoritesStore` / `PlaybackPositionStore`, branchée dans `AppContainer` |

### Format de `sources.json`

```json
{ "sources": [
  { "type": "json", "id": "demo", "name": "Démo", "location": "asset://catalog-demo.json" },
  { "type": "m3u", "id": "perso", "name": "Ma liste", "location": "https://…/liste.m3u", "enabled": false }
] }
```

`location` accepte `asset://` (fichier embarqué), `http://` et `https://`.

Une section `guides` (même format, type `xmltv`) déclare les guides des programmes. Les chaînes y sont reliées par leur `tvg-id` (M3U) ou leur `epgId` (JSON). Une liste M3U qui annonce son guide (`url-tvg`) n'a pas besoin d'être déclarée ici.

### Format du catalogue JSON

```json
{ "entries": [
  { "id": "info", "title": "Lionne Info", "type": "LIVE", "category": "Info",
    "url": "https://…/master.m3u8", "description": "…", "logo": "https://…", "poster": "https://…" }
  // logo / poster acceptent aussi les images embarquées : "file:///android_asset/images/x.png"
] }
```

`type` : `LIVE` (par défaut) ou `VOD`.

## 5. Organisation du dépôt

```
lionne-tv/
├── COMMUNICATION.md   échanges Claude ↔ ChatGPT
├── TASKS.md           répartition des tâches
├── PROJECT.md         ce document
├── CHANGELOG.md       suivi des modifications
├── README.md          présentation, compilation, installation
├── .github/workflows/ CI : tests + APK de debug
├── gradle/            versions centralisées (libs.versions.toml) et wrapper
├── app/ core/ source/ feature/   modules (voir § 4)
```

## 6. Conventions

- **Branches :** `claude/T-xxx-description`, `chatgpt/T-xxx-description`.
- **Commits :** `T-xxx: description courte au présent`.
- **Pull requests :** relues par l'autre IA, fusionnées par eies37, CI verte obligatoire. Chaque PR met à jour `CHANGELOG.md`.
- **Versions des bibliothèques :** uniquement dans `gradle/libs.versions.toml`.
- **Code :** Kotlin, style officiel. Identifiants en anglais, commentaires et documentation en français.

## 7. Décisions

| ID | Date | Décision | Décidé par |
|----|------|----------|------------|
| D-001 | 2026-11-24 | Coordination via `COMMUNICATION.md`, `TASKS.md`, `PROJECT.md` et `CHANGELOG.md` | eies37 |
| D-002 | 2026-11-24 | Plateforme : Android TV / Google TV, application native Kotlin (Compose for TV + Media3) | eies37 |
| D-003 | 2026-11-24 | Contenu v1 : direct, vidéo à la demande et listes M3U. Architecture modulaire (modules Gradle, sources déclarées dans `sources.json`) | eies37 |
| D-004 | 2026-11-24 | Pas de backend en v1 : sources configurables, stockage local. À revoir avec Q3 | Claude (provisoire, en attente d'eies37) |

## 8. Questions ouvertes

Voir `COMMUNICATION.md`. Restent ouvertes : Q3 (backend), Q4 et Q5 (ChatGPT).
