# TASKS — Lionne TV

Répartition et suivi des tâches. Chaque tâche a un identifiant `T-xxx` repris dans les branches, les commits et les pull requests.

**Statuts :** `À faire` · `En cours` · `En revue` · `Fait` · `Bloqué`
**Responsables :** `Claude` · `ChatGPT` · `eies37`

## Tableau des tâches

| ID | Tâche | Responsable | Statut | Dépend de | Branche / PR | Notes |
|----|-------|-------------|--------|-----------|--------------|-------|
| T-001 | Créer les fichiers de coordination (COMMUNICATION, TASKS, PROJECT, CHANGELOG) | Claude | Fait | — | `main` | Commit initial |
| T-002 | Répondre aux questions Q1 à Q3 (plateformes, contenu, backend) | eies37 | À faire | — | — | Voir `COMMUNICATION.md` |
| T-003 | Relire les règles de communication et la répartition proposée | ChatGPT | À faire | T-001 | — | Q4, Q5 |
| T-004 | Compléter le cahier des charges fonctionnel dans `PROJECT.md` | ChatGPT + eies37 | À faire | T-002 | — | |
| T-005 | Proposer les options d'architecture et de stack | Claude | À faire | T-002 | — | Décision finale : eies37 |
| T-006 | Créer la structure du dépôt (README, .gitignore, licence, dossiers) | Claude | À faire | T-005 | — | |
| T-007 | Concevoir la navigation à la télécommande et les écrans principaux | ChatGPT | À faire | T-004 | — | Interface 10-foot |
| T-008 | Mettre en place la CI (lint, tests, build) | Claude | À faire | T-006 | — | |

## Règles

- On change le statut de **sa propre** tâche. Pour reprendre une tâche de l'autre, on le demande dans `COMMUNICATION.md`.
- Une nouvelle tâche prend l'ID suivant disponible ; on ne réutilise jamais un ID.
- Une tâche passe à `Fait` quand sa PR est fusionnée dans `main`.
