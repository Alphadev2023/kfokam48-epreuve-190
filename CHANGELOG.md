# Changelog

Toutes les évolutions notables du projet. Chaque version correspond à un jalon de l'historique Git.

## [1.0.0] — 2026-09-25

Version finale, après l'enveloppe de l'étape 3 (un bug et un changement de besoin).

### Ajouté

- Chaque exercice est relu par **deux pairs différents**, tirés au hasard parmi les présents les moins chargés, et complétés à chaque nouvelle présence (#24, PR #27 ; RG12, RG13, RG14).
- **Note retenue** : moyenne des relectures rendues, arrondie à 2 décimales, marquée « provisoire » tant qu'une seule est rendue (#25 ; RG21).
- L'étudiant voit sa note retenue et les commentaires reçus, sans jamais le nom des relecteurs (#13, intégrée à #25 ; RG18).
- Tableau du formateur : moyenne calculée sur les notes retenues et champ `moyenneProvisoire` (#25 ; RG17, H21).
- Liste des exercices en attente, avec l'état de chaque relecteur (#9 ; Q11).
- Présence ajoutée à la main par le formateur, visible comme « ajouté par le formateur », et liste des présences d'une séance (#10 ; Q14, RG7).
- Migration `V101` : deux relecteurs par exercice, sans modifier `V1`. Les exercices déjà déposés gardent un seul relecteur (RG22). Démonstration `V102`.

### Corrigé

- Des marquages de présence simultanés pouvaient perdre une présence : un verrou pessimiste sur la session sérialise désormais la présence et le dépôt (#22, PR #26 ; ENF8). Le bug est reproduit par `PresenceConcurrenceTest`, commité avant le correctif.
- Deux relecteurs qui rendent en même temps ne font plus manquer le passage à RELU (verrou sur l'exercice ; ENF8).

### Modifié

- Cahier des charges version 2, diagrammes D2 et D4, en conséquence du changement de besoin.
- Contrat d'API 2.0 : forme de `GET /api/etudiants/{id}/exercices`, `moyenneProvisoire` dans `GET /api/tableau`, nouvelles opérations de suivi.

### Sorti du périmètre

- Clôture de session (#11) et remplacement du lien (#12), sortis à l'étape 3 pour absorber le changement de besoin. Décrits dans le contrat comme non implémentés.
- Blocage après cinq codes erronés (#14), Could non planifiée.

## [0.1.0] — 2026-09-25

Première version : toutes les exigences Must.

### Ajouté

- Socle démarrable par `docker compose up`, avec données de démonstration et format d'erreur unique `{code, message}` (#1).
- Ouverture d'une session avec un code de 6 caractères valable 15 minutes (#2 ; RG1, RG19).
- Choix de l'étudiant dans la liste de sa promotion, sans mot de passe (#3 ; Q1).
- Marquage de la présence par code : 400, 409 et 410 selon le contrat (#4 ; RG4, RG5, RG6).
- Dépôt du lien de l'exercice par un étudiant présent (#5 ; RG8 à RG11).
- Attribution aléatoire d'un relecteur, différée s'il n'y a aucun candidat (#6 ; RG13, RG14).
- Relecture notée de 0 à 20, définitive une fois rendue (#7 ; RG3, RG15).
- Tableau récapitulatif du formateur en requêtes agrégées (#8 ; ENF2).
