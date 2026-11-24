# TASKS — Lionne TV

Répartition et suivi des tâches. Chaque tâche a un identifiant `T-xxx` repris dans les branches, les commits et les pull requests.

**Statuts :** `À faire` · `En cours` · `En revue` · `Fait` · `Bloqué`
**Responsables :** `Claude` · `ChatGPT` · `eies37`

## Tableau des tâches

| ID | Tâche | Responsable | Statut | Dépend de | Branche / PR | Notes |
|----|-------|-------------|--------|-----------|--------------|-------|
| T-001 | Créer les fichiers de coordination (COMMUNICATION, TASKS, PROJECT, CHANGELOG) | Claude | Fait | — | `main` | |
| T-002 | Répondre aux questions Q1 à Q3 (plateformes, contenu, backend) | eies37 | En cours | — | — | Q1 : Android TV. Q2 : tout. Q3 : ouverte |
| T-003 | Relire les règles de communication et la répartition proposée | ChatGPT | À faire | T-001 | — | Q4, Q5 |
| T-004 | Compléter le cahier des charges fonctionnel dans `PROJECT.md` | ChatGPT + eies37 | À faire | T-002 | — | Vision, public, priorités |
| T-005 | Choisir l'architecture et la stack | Claude | Fait | T-002 | `claude/T-006-socle-android-tv` | D-002, D-003 |
| T-006 | Socle Android TV multi-modules : sources JSON/M3U, lecteur Media3, favoris, reprise, accueil et lecteur minimaux | Claude | En revue | T-005 | `claude/T-006-socle-android-tv` | Interface volontairement simple, à reprendre en T-007 |
| T-007 | Concevoir l'interface 10-foot définitive (accueil, fiches, lecteur, bandeau) | ChatGPT | À faire | T-006 | — | Modules `feature/*` et `core/designsystem` |
| T-008 | Mettre en place la CI (tests, build, APK) | Claude | En revue | T-006 | `claude/T-006-socle-android-tv` | `.github/workflows/android.yml` |
| T-009 | Écran Paramètres : ajouter / activer / retirer une liste M3U depuis la TV | ChatGPT | À faire | T-006 | — | Nouveau module `feature/settings` ; stockage des sources utilisateur côté `core/storage` (Claude peut fournir l'API) |
| T-010 | Logos des chaînes et affiches VOD (chargement d'images léger, avec cache borné) | Claude | À faire | T-006 | — | Champs `logoUrl` / `posterUrl` déjà disponibles |
| T-011 | Guide des programmes (EPG, format XMLTV `url-tvg`) | Claude | À faire | T-006 | — | Nouveau module `source/xmltv` |
| T-012 | Identité visuelle : logo, icône, bannière TV, palette définitive | ChatGPT + eies37 | À faire | — | — | Provisoire dans `core/designsystem` et `app/src/main/res` |
| T-013 | Signature et publication d'une version release (Play Store ou APK) | eies37 + Claude | À faire | T-007 | — | Clé de signature à créer par eies37 |
| T-014 | Lint Android et tests d'interface dans la CI | Claude | À faire | T-008 | — | |

## Règles

- On change le statut de **sa propre** tâche. Pour reprendre une tâche de l'autre, on le demande dans `COMMUNICATION.md`.
- Une nouvelle tâche prend l'ID suivant disponible ; on ne réutilise jamais un ID.
- Une tâche passe à `Fait` quand sa PR est fusionnée dans `main`.
