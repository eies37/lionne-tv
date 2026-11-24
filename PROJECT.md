# PROJECT — Lionne TV

Cahier des charges et architecture. Document vivant : tout point marqué **[À CONFIRMER]** reste une hypothèse tant qu'eies37 ne l'a pas validé.

## 1. Vision

**[À CONFIRMER]** Lionne TV est une application de télévision destinée aux Smart TV.

- Objectif principal : _à définir_
- Public visé : _à définir_
- Ce qui la distingue : _à définir_

## 2. Périmètre fonctionnel

### 2.1 Fonctions envisagées [À CONFIRMER]

| Fonction | Priorité (MVP / plus tard) | Statut |
|----------|----------------------------|--------|
| Lecture de flux en direct | ? | À confirmer |
| Catalogue VOD | ? | À confirmer |
| Guide des programmes (EPG) | ? | À confirmer |
| Favoris / reprise de lecture | ? | À confirmer |
| Comptes utilisateurs / profils | ? | À confirmer |
| Recherche | ? | À confirmer |
| Contrôle parental | ? | À confirmer |

### 2.2 Hors périmètre

_à définir_

## 3. Exigences non fonctionnelles (proposées)

- **Navigation entièrement à la télécommande** (touches directionnelles, OK, retour) : focus toujours visible, aucun élément accessible uniquement à la souris ou au tactile.
- **Interface 10-foot :** textes lisibles à 3 m, marges de sécurité de l'écran respectées.
- **Faible empreinte :** les TV ont peu de stockage et de mémoire. Cible proposée : application installée de moins de 50 Mo, cache borné et vidable. **[À CONFIRMER]**
- **Performances :** démarrage à froid en moins de 3 s et changement de chaîne en moins de 1,5 s sur une TV d'entrée de gamme. **[À CONFIRMER]**
- **Vidéo :** HLS et/ou DASH, de 1080p à 4K selon l'appareil. DRM à prévoir si le contenu l'exige.
- **Accessibilité :** sous-titres, contraste suffisant, compatibilité avec le lecteur d'écran de la plateforme.
- **Langue :** français d'abord. Prévoir l'internationalisation dès le départ ? **[À CONFIRMER]**

## 4. Architecture

**Pas encore décidée.** Elle dépend des plateformes cibles (Q1) et du type de contenu (Q2). Pistes à comparer (tâche T-005) :

| Option | Principe | Plateformes couvertes | Points forts | Points faibles |
|--------|----------|-----------------------|--------------|----------------|
| A. Native Android TV | Kotlin, Jetpack Compose for TV, Media3/ExoPlayer | Android TV, Google TV, Fire TV | Meilleures performances et lecteur robuste | Ne couvre ni Samsung ni LG |
| B. Application web TV | TypeScript et framework web, empaquetée pour Tizen et webOS | Samsung, LG, navigateur | Un seul code pour plusieurs marques | Performances variables selon la TV, outils propres à chaque fabricant |
| C. Hybride | Cœur partagé (données, logique) avec une interface native par plateforme | Toutes | Couverture maximale | Coût et complexité plus élevés |

Composants probables quelle que soit l'option :
- **Application TV :** interface, navigation, lecteur.
- **Couche données :** catalogue, EPG, favoris, cache local.
- **Backend** (si Q3 = oui) : API, authentification, gestion du catalogue.

## 5. Organisation du dépôt (proposée)

```
lionne-tv/
├── COMMUNICATION.md   # échanges Claude ↔ ChatGPT
├── TASKS.md           # répartition des tâches
├── PROJECT.md         # ce document
├── CHANGELOG.md       # suivi des modifications
├── README.md          # présentation et démarrage (T-006)
├── docs/              # maquettes, schémas, décisions détaillées
├── app/               # code de l'application TV (structure selon l'option retenue)
└── backend/           # seulement si nécessaire
```

## 6. Conventions

- **Branches :** `claude/T-xxx-description`, `chatgpt/T-xxx-description`.
- **Commits :** `T-xxx: description courte au présent`.
- **Pull requests :** relues par l'autre IA, fusionnées par eies37. Chaque PR met à jour `CHANGELOG.md`.
- **Langue :** documentation en français. Langue du code et des commentaires : **[À CONFIRMER]**.

## 7. Décisions

| ID | Date | Décision | Décidé par |
|----|------|----------|------------|
| D-001 | 2026-11-24 | Coordination via `COMMUNICATION.md`, `TASKS.md`, `PROJECT.md` et `CHANGELOG.md` | eies37 |

## 8. Questions ouvertes

Voir `COMMUNICATION.md` (Q1 à Q5).
