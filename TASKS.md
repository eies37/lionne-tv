# TASKS — Lionne TV

Répartition et suivi des tâches. Chaque tâche a un identifiant `T-xxx` repris dans les branches, les commits et les pull requests.

**Statuts :** `À faire` · `En cours` · `En revue` · `Fait` · `Bloqué`
**Responsables :** `Claude` · `ChatGPT` · `eies37`

## Tableau des tâches

| ID | Tâche | Responsable | Statut | Dépend de | Branche / PR | Notes |
|----|-------|-------------|--------|-----------|--------------|-------|
| T-001 | Créer les fichiers de coordination (COMMUNICATION, TASKS, PROJECT, CHANGELOG) | Claude | Fait | — | `main` | |
| T-002 | Répondre aux questions Q1 à Q3 (plateformes, contenu, backend) | eies37 | En cours | — | — | Q1 : Android TV. Q2 : tout. Q3 : ouverte |
| T-003 | Relire les règles de communication et la répartition proposée | ChatGPT | Fait | T-001 | PR #2 | Réponses Q4 à Q9 dans COMMUNICATION.md |
| T-004 | Compléter le cahier des charges fonctionnel dans `PROJECT.md` | ChatGPT + eies37 | À faire | T-002 | — | Vision, public, priorités |
| T-005 | Choisir l'architecture et la stack | Claude | Fait | T-002 | PR #1 | D-002, D-003 |
| T-006 | Socle Android TV multi-modules : sources JSON/M3U, lecteur Media3, favoris, reprise, accueil et lecteur minimaux | Claude | Fait | T-005 | PR #1 | Interface volontairement simple, à reprendre en T-007. Testé sur émulateur : accueil, direct, zapping, VOD, pause |
| T-007 | Concevoir l'interface 10-foot définitive (accueil, fiches, lecteur, bandeau) | ChatGPT | Fait | T-006 | PR #2 | Modules `feature/*` et `core/designsystem` |
| T-008 | Mettre en place la CI (tests, build, APK, test sur émulateur) | Claude | Fait | T-006 | PR #1 | `android.yml`, `smoke.yml`, `release-test.yml` |
| T-009 | Écran Paramètres : ajouter / activer / retirer une liste M3U depuis la TV | ChatGPT | Fait | T-006 | PR #2 | Nouveau module `feature/settings` ; stockage des sources utilisateur côté `core/storage` (Claude peut fournir l'API) |
| T-010 | Logos des chaînes et affiches VOD (chargement d'images léger, avec cache borné) | Claude | Fait | T-006 | PR #5 | Champs `logoUrl` / `posterUrl` déjà disponibles |
| T-011 | Guide des programmes (EPG, format XMLTV) | Claude | Fait | T-010 | PR #6 | Module `source/xmltv`, `url-tvg` des listes M3U pris en compte |
| T-012 | Identité visuelle : logo, icône, bannière TV, palette définitive | ChatGPT + eies37 | En revue | — | PR #2 | Proposition fusionnée (ambre et prune) ; validation finale par eies37 |
| T-013 | Signature et publication d'une version release (Play Store ou APK) | eies37 + Claude | En cours | T-007 | PR #8 | Partie CI faite. eies37 : créer la clé et les 4 secrets (README), puis une étiquette `vX.Y.Z` |
| T-014 | Lint Android et tests d'interface dans la CI | Claude | Fait | T-008 | PR #7 | Lint de tous les modules ; scénario Paramètres dans le test émulateur |
| T-015 | Accueil : émission en cours dans `SelectionPreview` et aide « appui long = favoris » | ChatGPT | À faire | T-011 | — | Re Q8 |
| T-016 | Sélecteur de flux par chaîne dans le lecteur | ChatGPT | À faire | T-006 | — | Testé localement par ChatGPT sur TV TCL |
| T-017 | Nom personnalisé des listes M3U ajoutées dans les Paramètres | ChatGPT | À faire | T-009 | — | |

## Règles

- On change le statut de **sa propre** tâche. Pour reprendre une tâche de l'autre, on le demande dans `COMMUNICATION.md`.
- Une nouvelle tâche prend l'ID suivant disponible ; on ne réutilise jamais un ID.
- Une tâche passe à `Fait` quand sa PR est fusionnée dans `main`.
