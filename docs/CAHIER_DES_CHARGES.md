# Cahier des charges — KF48 Présences & Relectures

**Auteur :** Diallo Ibrahima Bobo · KF48-DLA-190
**Version :** 1 · **Date :** 25 septembre 2026
**Frontend choisi :** React (Vite + TypeScript), parce que trois écrans simples ne justifient pas la structure d'Angular et que React donne le build le plus rapide à vérifier depuis un clone vierge.

---

## 1. Contexte et objectif

La direction de la formation KFOKAM48 suit aujourd'hui la présence et les exercices de ses étudiants sans outil commun. L'appel se fait à la main, les liens d'exercices circulent dans des messageries, et la relecture entre pairs, qui est au cœur de la pédagogie, n'est ni tracée ni notée de façon exploitable.

L'application permet au formateur d'ouvrir une séance et de laisser les étudiants pointer eux-mêmes depuis leur téléphone, avec un code court valable quinze minutes. Chaque étudiant présent dépose ensuite le lien de son exercice. Le système lui attribue au hasard un relecteur parmi les autres présents, et ce relecteur rend une note sur 20 accompagnée d'un commentaire.

Le formateur dispose d'un tableau unique par promotion : présences, exercices déposés, moyenne des notes reçues et relectures encore dues. L'objectif est qu'il repère en un coup d'œil qui décroche et quelles relectures bloquent.

## 2. Acteurs et rôles

| Acteur    | Ce qu'il peut faire                                                                                                                                                                | Ce qu'il ne peut pas faire                                                                                                                                        |
| --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Formateur | Ouvrir une session et afficher son code (EF1). Ajouter une présence à la main (EF9). Clôturer une session (EF10). Consulter le tableau et les exercices en attente (EF7, EF8)      | Marquer une présence au nom d'un étudiant sans que cela se voie (RG7). Déposer un exercice ou rédiger une relecture. Rouvrir une session clôturée (RG20)          |
| Étudiant  | Se choisir dans la liste de sa promotion (EF2). Marquer sa présence (EF3). Déposer puis remplacer le lien de son exercice (EF4, EF11). Voir la note et le commentaire reçus (EF12) | S'authentifier par mot de passe (hors périmètre, Q1). Déposer sans être présent (RG9). Connaître l'identité de son relecteur (RG18). Choisir son relecteur (RG13) |
| Relecteur | Voir les exercices qui lui sont attribués. Rendre une note entière et un commentaire (EF6)                                                                                         | Relire son propre exercice (RG2). Modifier une relecture rendue (RG15). Relire un exercice qui ne lui est pas attribué                                            |
| Système   | Tirer un relecteur au hasard au dépôt, ou dès qu'un candidat devient présent (EF5, RG13, RG14)                                                                                     | Attribuer un relecteur absent de la session ou auteur de l'exercice (RG2, RG13)                                                                                   |

**Décision : le relecteur n'est pas un acteur distinct.** C'est un étudiant désigné sur une relecture précise, un rôle qu'il tient vis-à-vis d'un exercice donné et non une identité.

Conséquences sur le modèle de données :

- il n'existe ni table ni rôle « relecteur » ;
- l'entité `RELECTURE` porte une clé `relecteur_id` vers `ETUDIANT` ;
- un même étudiant peut être auteur d'un exercice et relecteur d'un autre dans la même session.

## 3. Périmètre

**Inclus dans cette version :**

- Les exigences EF1 à EF12 : session et code, présence par code et manuelle, clôture, dépôt et remplacement de lien, attribution aléatoire du relecteur, relecture, tableau, exercices en attente, consultation de sa note.
- Trois écrans : formateur, étudiant, relecteur.
- Des données de démonstration chargées au démarrage : une promotion de 60 étudiants avec un historique de sessions, et une petite promotion pour la démonstration manuelle.
- Un démarrage par `docker compose up`.

**Explicitement exclu :**

- L'authentification et la gestion de comptes (Q1). L'étudiant se choisit dans une liste.
- La création et la modification des promotions et des étudiants. Ils sont fournis par les données de démonstration.
- Plusieurs relecteurs par exercice (Q6).
- La modification d'une note déjà rendue (Q15, voir la contradiction tranchée en section 7).
- Le blocage de deux minutes après cinq codes erronés (Q4). Il est reporté en Could (EF13), justification en section 7.
- Le détail de la présence session par session dans le tableau (Q16). Le contrat impose un total, voir la section 7.
- La réattribution manuelle d'un relecteur défaillant. Q11 demande seulement de voir l'exercice en attente.
- La réouverture d'une session clôturée, les notifications, l'export et le soin du rendu visuel.

## 4. Exigences fonctionnelles

| Réf  | Exigence                                                                                     | Critère d'acceptation                                                                                                                                                                                                                                                                                      | Priorité |
| ---- | -------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| EF1  | Le formateur ouvre une session pour une promotion et obtient un code de présence (RG1, RG19) | Quand le formateur saisit un titre et choisit une promotion, alors une session est créée et un code de 6 caractères s'affiche avec son heure d'expiration, égale à l'heure d'ouverture plus 15 minutes                                                                                                     | Must     |
| EF2  | L'étudiant s'identifie en se choisissant dans la liste de sa promotion (Q1)                  | Quand l'étudiant choisit sa promotion, alors la liste de ses étudiants s'affiche triée par nom, et il se sélectionne sans mot de passe                                                                                                                                                                     | Must     |
| EF3  | L'étudiant marque sa présence avec le code (RG1, RG4, RG5, RG6)                              | Quand je saisis un code valide et non expiré, alors ma présence apparaît dans le tableau du formateur. Quand le code a expiré, alors je lis « Le code de présence a expiré » et rien n'est enregistré. Quand je suis déjà présent, alors je lis un message d'erreur et aucune seconde présence n'est créée | Must     |
| EF4  | L'étudiant présent dépose le lien de son exercice pour une session (RG8, RG9, RG10, RG11)    | Quand un étudiant présent saisit un lien http(s) pour une session non clôturée, alors l'exercice est enregistré avec son statut. Quand il dépose une seconde fois pour la même session, alors il lit « exercice déjà déposé »                                                                              | Must     |
| EF5  | Le système attribue un relecteur au hasard (RG2, RG12, RG13, RG14)                           | Quand un exercice est déposé et qu'au moins un autre étudiant est présent à la session, alors un relecteur différent de l'auteur lui est attribué. Sinon, l'exercice est « en attente d'attribution » et reçoit un relecteur dès qu'un autre étudiant devient présent                                      | Must     |
| EF6  | Le relecteur rend une note et un commentaire (RG2, RG3, RG15)                                | Quand le relecteur envoie une note entière de 0 à 20 et un commentaire, alors l'exercice passe à « relu » et la note entre dans la moyenne de l'auteur. Quand il tente de renvoyer une relecture, alors il lit « relecture déjà rendue » et la note ne change pas                                          | Must     |
| EF7  | Le formateur consulte le tableau récapitulatif d'une promotion (Q16, RG17)                   | Quand le formateur choisit une promotion, alors il voit pour chaque étudiant son nombre de présences, son nombre d'exercices déposés, sa moyenne reçue (vide s'il n'a aucune note) et son nombre de relectures en attente                                                                                  | Must     |
| EF8  | Le formateur voit les exercices encore sans relecture rendue (Q11)                           | Quand un exercice n'a pas de relecture rendue, alors il figure dans la liste des exercices en attente du formateur, avec l'auteur, la session, le relecteur attribué ou « aucun », et son statut                                                                                                           | Should   |
| EF9  | Le formateur ajoute une présence à la main (RG7)                                             | Quand le formateur ajoute un étudiant à une session non clôturée, alors la présence est créée avec la mention « ajouté par le formateur » (source FORMATEUR), même après l'expiration du code                                                                                                              | Should   |
| EF10 | Le formateur clôture une session (RG10, RG20)                                                | Quand le formateur clôture une session, alors plus aucune présence, aucun dépôt ni aucun remplacement de lien n'est accepté pour cette session                                                                                                                                                             | Should   |
| EF11 | L'étudiant remplace le lien de son exercice (RG10, RG16)                                     | Quand l'auteur remplace le lien d'un exercice dont la relecture n'est pas rendue et dont la session n'est pas clôturée, alors le relecteur voit le nouveau lien. Sinon, le remplacement est refusé avec un message explicite                                                                               | Should   |
| EF12 | L'étudiant consulte la note et le commentaire reçus (Q8, RG18)                               | Quand la relecture de mon exercice est rendue, alors je vois la note et le commentaire, et le nom du relecteur n'apparaît nulle part dans l'écran ni dans la réponse de l'API                                                                                                                              | Should   |
| EF13 | Protection contre la devinette des codes (Q4)                                                | Quand un étudiant a saisi 5 codes inconnus, alors ses tentatives sont refusées pendant 2 minutes                                                                                                                                                                                                           | Could    |

## 5. Exigences non fonctionnelles

| Réf  | Exigence                                                                                                                              | Comment on la vérifie                                                                                                                                                                                                  |
| ---- | ------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| ENF1 | L'interface de marquage de présence est utilisable sur un téléphone                                                                   | Dans les outils de développement, mode appareil en 360 × 640 : parcours complet (choix du nom, saisie du code, dépôt du lien) sans zoom ni défilement horizontal                                                       |
| ENF2 | Le tableau du formateur répond en moins de 2 s pour une promotion de 60 étudiants                                                     | Avec les données de démonstration (promotion de 60 étudiants), `curl -w "%{time_total}"` sur `GET /api/tableau` donne moins de 2 s sur 5 appels. Le calcul se fait en requêtes agrégées, sans une requête par étudiant |
| ENF3 | Toutes les erreurs suivent le format `{ code, message }`, avec un message en français                                                 | Test d'intégration sur un cas d'erreur. Appels manuels sur une route inexistante et avec un JSON mal formé : même format, jamais de stack trace                                                                        |
| ENF4 | L'application démarre chez un tiers depuis le seul README, avec des données de démonstration                                          | Clone dans un dossier vide, `docker compose up`, puis ouverture du front : le tableau de la promotion de démonstration est rempli                                                                                      |
| ENF5 | Les tests tournent sur un poste vierge, sans base locale                                                                              | `./mvnw test` passe sur un clone vierge, avec une base H2 en mémoire                                                                                                                                                   |
| ENF6 | Les horodatages sont stockés en UTC et affichés dans l'heure locale du navigateur                                                     | Dans la réponse de `POST /api/sessions`, `expirationAt` vaut `ouvertureAt` plus 15 minutes. L'écran affiche l'heure locale                                                                                             |
| ENF7 | Le système tient la volumétrie visée : 60 étudiants par promotion, une quarantaine de sessions, environ 2 400 exercices par promotion | Des index existent sur toutes les clés étrangères et sur `code` (visibles dans la migration V1)                                                                                                                        |

## 6. Règles de gestion

| Réf  | Règle                                                                                                                                                                     | Source                             |
| ---- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------- |
| RG1  | Un code de présence expire 15 minutes après l'ouverture de la session                                                                                                     | Q2                                 |
| RG2  | Un étudiant ne peut pas relire son propre exercice                                                                                                                        | Q5                                 |
| RG3  | Une note est un entier compris entre 0 et 20. Une valeur décimale comme 12.5 est refusée, pas arrondie                                                                    | Q9                                 |
| RG4  | Une présence par code n'est acceptée que si le code n'a pas expiré et que la session n'est pas clôturée                                                                   | Q2, Q3                             |
| RG5  | Un étudiant a au plus une présence par session, quelle qu'en soit la source                                                                                               | Contrat (409 DEJA_PRESENT)         |
| RG6  | Un étudiant ne peut marquer sa présence que dans une session de sa propre promotion                                                                                       | Hypothèse H9                       |
| RG7  | Une présence ajoutée par le formateur porte la source FORMATEUR. Elle est acceptée même après l'expiration du code, tant que la session n'est pas clôturée                | Q14                                |
| RG8  | Un étudiant dépose au plus un exercice par session                                                                                                                        | Contrat (409 EXERCICE_DEJA_DEPOSE) |
| RG9  | Seul un étudiant présent à la session peut y déposer un exercice                                                                                                          | Hypothèse H4                       |
| RG10 | Le dépôt et le remplacement d'un lien sont possibles jusqu'à la clôture de la session, même après l'expiration du code                                                    | Q12, Q13                           |
| RG11 | Un lien d'exercice est une URL absolue en http ou https                                                                                                                   | Contrat (LIEN_INVALIDE)            |
| RG12 | Un exercice a exactement un relecteur                                                                                                                                     | Q6                                 |
| RG13 | Le relecteur est tiré au hasard parmi les étudiants présents à la session, auteur exclu, en ne retenant que les moins chargés en relectures dans cette session            | Q7, hypothèse H3                   |
| RG14 | S'il n'y a aucun candidat au dépôt, l'exercice reste « en attente d'attribution ». Le tirage est retenté à chaque nouvelle présence dans la session                       | Hypothèse H2                       |
| RG15 | Une relecture rendue est définitive : ni la note ni le commentaire ne peuvent être modifiés                                                                               | Q15 (contradiction Q10 tranchée)   |
| RG16 | Le lien d'un exercice ne peut plus être remplacé une fois sa relecture rendue                                                                                             | Q13, hypothèse H6                  |
| RG17 | La moyenne d'un étudiant est la moyenne arithmétique des notes des relectures rendues sur ses exercices, arrondie à 2 décimales. Elle vaut null s'il n'a reçu aucune note | Q16, contrat (moyenne nullable)    |
| RG18 | L'auteur d'un exercice ne voit jamais le nom de son relecteur                                                                                                             | Q8                                 |
| RG19 | Un code de présence est unique parmi toutes les sessions                                                                                                                  | Hypothèse H8                       |
| RG20 | Une session clôturée ne peut pas être rouverte. Une relecture attribuée peut encore être rendue après la clôture                                                          | Hypothèse H10                      |

## 7. Zones d'ombre, hypothèses et contradictions

**Le trou que personne n'a comblé :** Q7 dit parmi qui le relecteur est tiré, mais aucune question ne dit **quand** il l'est, ni **ce qui se passe s'il n'y a personne d'autre de présent** au moment du dépôt. Les hypothèses H2 et H3 le comblent.

**Points que la demande ne tranche pas :**

| Point                                                                                                          | Réponse client (Qx) ou hypothèse                                 | Décision retenue                                                                                                                                                                                                                                                                                    | Conséquence                                                                                                                                                                                              |
| -------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| H1 — « Fin de session » et « clôture » sont-elles la même chose ?                                              | Q3, Q10 et Q12 emploient les deux termes sans les définir        | Deux notions distinctes. La fin de la fenêtre de présence correspond à l'expiration du code (15 minutes, Q2). La clôture est une action explicite du formateur                                                                                                                                      | Statut de session OUVERTE ou CLOTUREE, et opération ajoutée `POST /api/sessions/{id}/cloture`                                                                                                            |
| H2 — Moment du tirage du relecteur                                                                             | Q7 ne le précise pas (le trou)                                   | Tirage au dépôt. S'il n'y a aucun candidat, le statut devient EN_ATTENTE_ATTRIBUTION et le tirage est retenté à chaque nouvelle présence dans la session                                                                                                                                            | Statut d'exercice dédié, voir le diagramme D4 et RG14                                                                                                                                                    |
| H3 — Répartition des relectures                                                                                | Q7 dit « au hasard » sans parler d'équité                        | Tirage au hasard parmi les présents qui ont le moins de relectures attribuées dans la session                                                                                                                                                                                                       | Personne ne cumule cinq relectures pendant qu'un autre n'en a aucune. Cette règle est testée unitairement                                                                                                |
| H4 — Faut-il être présent pour déposer ?                                                                       | Rien ne le dit. Q7 raisonne sur les présents                     | Oui                                                                                                                                                                                                                                                                                                 | 409 ETUDIANT_NON_PRESENT. Le formateur peut régulariser avec EF9                                                                                                                                         |
| H5 — Le relecteur doit-il avoir déposé lui-même ?                                                              | Non précisé                                                      | Non, seule sa présence compte                                                                                                                                                                                                                                                                       | Le vivier de candidats est plus large                                                                                                                                                                    |
| H6 — Que veut dire « personne n'a commencé à le relire » (Q13) ?                                               | La relecture est rendue en un seul envoi, sans état « en cours » | « Commencé » équivaut à « rendue »                                                                                                                                                                                                                                                                  | 409 RELECTURE_RENDUE sur le remplacement de lien                                                                                                                                                         |
| H7 — Comment connaître l'appelant sans authentification (Q1) alors que le contrat prévoit 403 AUTO_RELECTURE ? | Q1 exclut le mot de passe                                        | Le front envoie l'en-tête optionnel `X-Etudiant-Id`, correspondant à l'identité choisie dans la liste. On garde aussi un contrôle défensif au tirage (RG2)                                                                                                                                          | 403 AUTO_RELECTURE si l'en-tête désigne l'auteur, 403 RELECTEUR_NON_ATTRIBUE s'il désigne un autre étudiant que le relecteur. C'est un garde-fou contre l'erreur, pas une sécurité, et c'est assumé (Q1) |
| H8 — Le contrat de présence ne transmet pas `sessionId`                                                        | Contrat imposé                                                   | Le code seul identifie la session. Il est donc unique globalement, et généré sans caractères ambigus (ni 0, ni O, ni 1, ni I)                                                                                                                                                                       | Contrainte d'unicité sur `session_cours.code` (RG19)                                                                                                                                                     |
| H9 — Un étudiant d'une autre promotion saisit un code                                                          | Non précisé                                                      | Refusé                                                                                                                                                                                                                                                                                              | 400 ETUDIANT_HORS_PROMOTION (RG6)                                                                                                                                                                        |
| H10 — Peut-on rendre une relecture après la clôture ?                                                          | Non précisé. Q11 veut voir les exercices en attente              | Oui                                                                                                                                                                                                                                                                                                 | La clôture ne fait perdre aucune note (RG20)                                                                                                                                                             |
| H11 — Présence manuelle après expiration du code ou après clôture                                              | Q14 : cas d'un souci de téléphone                                | Autorisée après l'expiration, refusée après la clôture                                                                                                                                                                                                                                              | Q14 reste utile et Q3 est respecté                                                                                                                                                                       |
| H12 — Cas d'erreur non prévus dans les 5 opérations imposées                                                   | Contrat « à la lettre »                                          | On n'ajoute aucun statut HTTP pour un cas métier : on réutilise un statut déjà prévu avec un code précis (par exemple 400 SESSION_INCONNUE, 409 SESSION_CLOTUREE). Seule exception : une requête illisible (JSON mal formé, paramètre absent ou non numérique) renvoie partout 400 REQUETE_INVALIDE | Respect de B2 et format d'erreur uniforme (ENF3)                                                                                                                                                         |
| H13 — Q16 demande « sa présence à chaque session »                                                             | Le contrat fixe `presences` à un entier                          | Le tableau respecte le contrat et affiche un total                                                                                                                                                                                                                                                  | Le détail par session est exclu de la v1 et noté pour une version ultérieure                                                                                                                             |
| H14 — Blocage après cinq erreurs (Q4)                                                                          | Le contrat ne prévoit aucun statut 429 sur l'opération imposée   | Reporté en Could (EF13). Un code de 6 caractères valable 15 minutes limite déjà le risque                                                                                                                                                                                                           | Aucun impact sur le contrat de la v1                                                                                                                                                                     |
| H15 — Moyenne d'un étudiant sans note                                                                          | Contrat : `moyenne` nullable                                     | null dans l'API, affichée « — » à l'écran                                                                                                                                                                                                                                                           | RG17                                                                                                                                                                                                     |
| H16 — Une note décimale, par exemple 12.5                                                                      | Q9 : entiers                                                     | Refusée avec 400 NOTE_INVALIDE. La conversion automatique de Jackson des décimaux en entiers est désactivée                                                                                                                                                                                         | RG3, contrainte technique C5                                                                                                                                                                             |

**Contradictions relevées :**

| Réponses en conflit                                                                                                               | Ce que j'ai choisi                                   | Pourquoi                                                                                                                                                                                                                                                                                                                                          |
| --------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Q10 (« le relecteur peut corriger sa note tant que la session n'est pas clôturée ») contre Q15 (« une fois validée, c'est fini ») | **Q15** : une relecture rendue est définitive (RG15) | 1) Le contrat imposé, non négociable (B2), prévoit 409 RELECTURE_DEJA_RENDUE sur `POST /api/relectures/{id}`. Retenir Q10 obligerait à violer ce contrat. 2) Q15 est motivée par le client (« plus honnête pour tout le monde »), alors que Q10 est un oui sans justification. 3) Q15 simplifie le modèle : pas d'historique de notes à conserver |
| Q3 (pas de présence après la fin) contre Q12 (dépôt possible après la fin) — fausse contradiction                                 | Les deux s'appliquent                                | Elles portent sur deux actions différentes, et Q12 borne explicitement le dépôt par la clôture. Voir H1                                                                                                                                                                                                                                           |

## 8. Contraintes techniques

**Imposées par le sujet :**

- **B1** : Java 17 ou plus, Maven, wrapper `mvnw` commité.
- **B2** : `api/contrat.yaml` respecté à la lettre (chemins, verbes, statuts, format d'erreur).
- **B3** : couches contrôleur, service et repository séparées. Aucune requête en base dans un contrôleur, aucune entité JPA en JSON, uniquement des DTO.
- **B4** : validation des entrées (Bean Validation) et gestion centralisée des erreurs (`@RestControllerAdvice`). Aucune stack trace renvoyée au client.
- **B5** : schéma versionné par Flyway, migrations commitées, `ddl-auto=validate` partout.
- **B6** : un test unitaire sur une règle réelle (le tirage du relecteur : RG2, RG13, RG14) et un test d'intégration sur `POST /api/presences` (201, 409, 410). Les deux tournent sans base locale.
- **F1** : React déclaré et justifié dans le README, et `npm run build` passe.
- **F2** : trois écrans. Formateur (ouvrir une session, voir le tableau), étudiant (marquer sa présence, déposer son exercice), relecteur (faire une relecture).
- **F3** : appels API regroupés dans `frontend/src/api/`. États de chargement et d'erreur gérés sur chaque appel. La moyenne affichée vient de l'API, elle n'est jamais recalculée.

**Que je m'impose :**

- **C1** : PostgreSQL 16 à l'exécution (docker compose) et H2 en mode PostgreSQL pour les tests. Les migrations sont écrites dans un SQL compatible avec les deux.
- **C2** : toute évolution du schéma passe par une nouvelle migration `V<n>__...sql`. Une migration déjà poussée n'est jamais modifiée.
- **C3** : les données de démonstration sont dans une migration séparée (`db/demo`), activée par le profil `demo`, lui-même actif par défaut dans docker compose.
- **C4** : les horodatages sont en `timestamp with time zone` côté base et en `Instant` côté Java.
- **C5** : Jackson est configuré avec `accept-float-as-int=false`, pour que 12.5 soit refusé et non tronqué (RG3).
- **C6** : Git. Une branche par issue (`<n>-description-courte`), une PR par branche avec `Closes #n`, des messages de commit en français qui citent les EF ou RG. `main` ne reçoit que des fusions de PR dont le build passe, plus les commits de documentation et les jalons.
- **C7** : aucun secret dans le dépôt. Les identifiants PostgreSQL de démonstration, non sensibles, passent par des variables d'environnement de docker compose.
- **C8** : le front utilise React 18, Vite, TypeScript et React Router, sans bibliothèque de gestion d'état.

## 9. Livrables

- Le dépôt public `kfokam48-epreuve-KF48-DLA-190`, avec la structure `/docs`, `/api`, `/backend`, `/frontend`.
- `docs/CAHIER_DES_CHARGES.md`, tenu à jour après l'étape 3.
- `docs/JOURNAL.md`, avec une entrée par étape.
- `docs/diagrammes/` : D1 cas d'utilisation, D2 modèle de données, D3 séquence « marquer sa présence », D4 cycle de vie d'un exercice (bonus).
- `api/contrat.yaml`, complété et figé avant le premier commit de code.
- Le backlog en issues, avec labels de priorité, et les PR liées.
- Les trois jalons `[JALON] analyse`, `[JALON] v0.1` et `[JALON] v1.0`.
- `README.md` (démarrage testé depuis un clone vierge), `CHANGELOG.md` et `docker-compose.yml`.
- Le dépôt public `kfokam48-gitlab-KF48-DLA-190` (étape 5).
- `SOUMISSION.md`, déposé sur la plateforme avant 18h00.

## 10. Démarche prévue

1. **Analyse.** Le présent document, quatre diagrammes, le contrat complété et les issues, puis `[JALON] analyse`. Aucun code avant.
2. **Première version.** Les issues Must uniquement, dans l'ordre du backlog : le socle démarrable (schéma V1, données de démonstration, gestion d'erreurs), puis EF1, EF2, EF3, EF4, EF5, EF6 et EF7. Chaque issue est livrée en tranche verticale (API, puis écran s'il y en a un) sur sa propre branche avec sa PR. Puis `[JALON] v0.1`.
3. **Enveloppe.** Lecture complète, puis ouverture de deux issues avant tout code : une pour le bug, une pour l'évolution. Le bug est reproduit par un test qui échoue. Le correctif et l'évolution vivent dans deux branches et deux PR séparées. Le schéma change par une nouvelle migration, et le contrat est mis à jour dans la PR de l'évolution. La repriorisation est écrite dans ce document et dans le journal. Le cahier des charges et les diagrammes sont corrigés dans un commit dédié.
4. **Version finale.** Les Should dans l'ordre EF8, EF9, EF10, EF11, EF12, puis le CHANGELOG, le README testé depuis un clone vierge et le backlog restant trié. Puis `[JALON] v1.0`.
5. **Épreuve Git**, dans un dépôt séparé, sans jamais mélanger les historiques.
6. **Soumission**, avec une cible de dépôt à 17h00 et non à 18h00.

**En cas de retard**, on coupe dans cet ordre : EF12, EF11, EF10, EF9. EF8 est gardée parce que Q11 est explicite. On ne sacrifie jamais le journal, les jalons, l'étape 3, l'étape 5 ni la soumission.

**Definition of Done — un ticket est terminé quand :**

- tous ses critères d'acceptation ont été vérifiés à la main ;
- le code est sur une branche dédiée, fusionnée dans `main` par une PR qui contient `Closes #n` ;
- `./mvnw verify` et `npm run build` passent, et les tests existants restent verts ;
- les erreurs renvoyées suivent le format du contrat ;
- si le ticket touche au schéma ou à l'API, la migration, D2 et `contrat.yaml` sont cohérents entre eux ;
- les messages de commit citent l'EF ou la RG concernée.

---

## Journal des révisions

| Version | Quand               | Ce qui a changé et pourquoi |
| ------- | ------------------- | --------------------------- |
| 1       | 25/09/2026, étape 1 | Version initiale            |
