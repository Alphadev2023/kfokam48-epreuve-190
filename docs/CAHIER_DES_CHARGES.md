# Cahier des charges — KF48 Présences & Relectures

**Auteur :** Diallo Ibrahima Bobo · KF48-DLA-190
**Version :** 2 · **Date :** 25 septembre 2026
**Frontend choisi :** React (Vite + TypeScript), parce que trois écrans simples ne justifient pas la structure d'Angular et que React donne le build le plus rapide à vérifier depuis un clone vierge.

> **Version 2 :** conséquence du changement de besoin reçu à l'étape 3 (enveloppe). Chaque exercice est désormais relu par deux pairs, et la note retenue est la moyenne des deux, provisoire tant qu'une seule relecture est rendue. Les passages modifiés sont signalés par « (v2) ».

---

## 1. Contexte et objectif

La direction de la formation KFOKAM48 suit aujourd'hui la présence et les exercices de ses étudiants sans outil commun. L'appel se fait à la main, les liens d'exercices circulent dans des messageries, et la relecture entre pairs, qui est au cœur de la pédagogie, n'est ni tracée ni notée de façon exploitable.

L'application permet au formateur d'ouvrir une séance et de laisser les étudiants pointer eux-mêmes depuis leur téléphone, avec un code court valable quinze minutes. Chaque étudiant présent dépose ensuite le lien de son exercice. Le système lui attribue au hasard deux relecteurs parmi les autres présents (v2). Chacun rend une note sur 20 et un commentaire, et la note retenue est la moyenne des deux.

Le formateur dispose d'un tableau unique par promotion : présences, exercices déposés, moyenne des notes reçues et relectures encore dues. L'objectif est qu'il repère en un coup d'œil qui décroche et quelles relectures bloquent.

## 2. Acteurs et rôles

| Acteur    | Ce qu'il peut faire                                                                                                                                                     | Ce qu'il ne peut pas faire                                                                                                                                               |
| --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Formateur | Ouvrir une session et afficher son code (EF1). Ajouter une présence à la main (EF9). Consulter le tableau et les exercices en attente (EF7, EF8)                        | Marquer une présence au nom d'un étudiant sans que cela se voie (RG7). Déposer un exercice ou rédiger une relecture                                                      |
| Étudiant  | Se choisir dans la liste de sa promotion (EF2). Marquer sa présence (EF3). Déposer le lien de son exercice (EF4). Voir la note retenue et les commentaires reçus (EF12) | S'authentifier par mot de passe (hors périmètre, Q1). Déposer sans être présent (RG9). Connaître l'identité de ses relecteurs (RG18). Choisir ses relecteurs             |
| Relecteur | Voir les exercices qui lui sont attribués. Rendre une note entière et un commentaire (EF6)                                                                              | Relire son propre exercice (RG2). Modifier une relecture rendue (RG15). Relire un exercice qui ne lui est pas attribué. Être tiré deux fois pour le même exercice (RG12) |
| Système   | Tirer deux relecteurs distincts au hasard au dépôt, et compléter les relecteurs manquants dès qu'un candidat devient présent (EF5, RG12 à RG14) (v2)                    | Attribuer un relecteur absent de la session ou auteur de l'exercice (RG2, RG13)                                                                                          |

**Décision : le relecteur n'est pas un acteur distinct.** C'est un étudiant désigné sur une relecture précise, un rôle qu'il tient vis-à-vis d'un exercice donné et non une identité.

Conséquences sur le modèle de données :

- il n'existe ni table ni rôle « relecteur » ;
- l'entité `RELECTURE` porte une clé `relecteur_id` vers `ETUDIANT` ;
- un exercice a désormais jusqu'à deux relectures, chacune avec un relecteur différent (v2).

## 3. Périmètre

**Inclus dans cette version :**

- Session et code, présence par code et manuelle, dépôt du lien (EF1 à EF4, EF9).
- Attribution aléatoire de deux relecteurs, relecture, note retenue et note provisoire (EF5, EF6, EF12, EF14) (v2).
- Tableau du formateur et exercices en attente (EF7, EF8).
- Trois écrans : formateur, étudiant, relecteur.
- Des données de démonstration, dont une séance à deux relecteurs (v2), et un démarrage par `docker compose up`.

**Explicitement exclu :**

- L'authentification (Q1).
- La création et la modification des promotions et des étudiants.
- La modification d'une note déjà rendue (Q15).
- Le blocage après cinq codes erronés (Q4, EF13 en Could).
- Le détail de la présence session par session dans le tableau (Q16, H13).
- La réattribution manuelle d'un relecteur défaillant.
- La réouverture de session, les notifications, l'export et le rendu visuel.
- **Sortis du périmètre à l'étape 3 (v2), puis réintégrés après la soumission à la demande du formateur (v2.1 et v2.2) :** la clôture de session (EF10) et le remplacement du lien (EF11).

**Pourquoi ce sacrifice.** Le passage à deux relecteurs est un Must arrivé tard, qui touche la base, le contrat et le front à la fois. Parmi les exigences restantes, EF10 et EF11 sont celles dont l'absence coûte le moins :

- sans clôture, les sessions restent ouvertes et le dépôt reste possible, ce qui va dans le sens de Q12 ;
- sans remplacement, un étudiant qui se trompe de lien le signale au formateur.

EF8 (Q11) et EF9 (Q14) sont gardées, parce que le client les a demandées explicitement. Ce sacrifice a tenu jusqu'à la soumission. Les issues #11 et #12 ont été traitées ensuite.

## 4. Exigences fonctionnelles

| Réf  | Exigence                                                                                     | Critère d'acceptation                                                                                                                                                                                                                                                                                           | Priorité                  |
| ---- | -------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------- | --- | --------------- |
| EF1  | Le formateur ouvre une session pour une promotion et obtient un code de présence (RG1, RG19) | Quand le formateur saisit un titre et choisit une promotion, alors une session est créée et un code de 6 caractères s'affiche avec son heure d'expiration, égale à l'heure d'ouverture plus 15 minutes                                                                                                          | Must                      |
| EF2  | L'étudiant s'identifie en se choisissant dans la liste de sa promotion (Q1)                  | Quand l'étudiant choisit sa promotion, alors la liste de ses étudiants s'affiche triée par nom, et il se sélectionne sans mot de passe                                                                                                                                                                          | Must                      |
| EF3  | L'étudiant marque sa présence avec le code (RG1, RG4, RG5, RG6)                              | Quand je saisis un code valide et non expiré, alors ma présence apparaît dans le tableau du formateur. Quand le code a expiré, alors je lis « Le code de présence a expiré » et rien n'est enregistré. Quand je suis déjà présent, alors je lis un message d'erreur et aucune seconde présence n'est créée      | Must                      |
| EF4  | L'étudiant présent dépose le lien de son exercice pour une session (RG8 à RG11)              | Quand un étudiant présent saisit un lien http(s) pour une session non clôturée, alors l'exercice est enregistré avec son statut. Quand il dépose une seconde fois pour la même session, alors il lit « exercice déjà déposé »                                                                                   | Must                      |
| EF5  | Le système attribue deux relecteurs au hasard (RG2, RG12, RG13, RG14) (v2)                   | Quand un exercice est déposé et qu'au moins deux autres étudiants sont présents, alors deux relecteurs différents, dont aucun n'est l'auteur, lui sont attribués. S'il en manque, l'exercice est « en attente d'attribution » et reçoit les relecteurs manquants dès que d'autres étudiants deviennent présents | Must                      |
| EF6  | Le relecteur rend une note et un commentaire (RG2, RG3, RG15) (v2)                           | Quand le relecteur envoie une note entière de 0 à 20 et un commentaire, alors sa relecture est enregistrée. Quand toutes les relectures attendues sont rendues, alors l'exercice passe à « relu ». Quand il tente de renvoyer une relecture, alors il lit « relecture déjà rendue » et la note ne change pas    | Must                      |
| EF7  | Le formateur consulte le tableau récapitulatif d'une promotion (Q16, RG17) (v2)              | Quand le formateur choisit une promotion, alors il voit pour chaque étudiant son nombre de présences, son nombre d'exercices déposés, sa moyenne calculée sur les notes retenues (vide s'il n'a aucune note, marquée « provisoire » si l'une d'elles l'est) et son nombre de relectures en attente              | Must                      |
| EF8  | Le formateur voit les exercices encore sans relecture complète (Q11)                         | Quand un exercice n'a pas toutes ses relectures rendues, alors il figure dans la liste des exercices en attente du formateur, avec l'auteur, la session et son statut                                                                                                                                           | Should                    |
| EF9  | Le formateur ajoute une présence à la main (RG7)                                             | Quand le formateur ajoute un étudiant à une session non clôturée, alors la présence est créée avec la mention « ajouté par le formateur » (source FORMATEUR), même après l'expiration du code                                                                                                                   | Should                    |
| EF10 | Le formateur clôture une session (RG10, RG20)                                                | Quand le formateur clôture une session, alors plus aucune présence ni aucun dépôt n'est accepté pour cette session ; les relectures déjà attribuées peuvent encore être rendues                                                                                                                                 | Should (réintégrée, v2.1) |     | Sorti (étape 3) |
| EF11 | L'étudiant remplace le lien de son exercice (RG10, RG16)                                     | Quand l'auteur remplace le lien d'un exercice dont aucune relecture n'est rendue et dont la séance n'est pas clôturée, alors les relecteurs voient le nouveau lien. Sinon, il lit un message d'erreur et le lien ne change pas                                                                                  | Should (réintégrée, v2.2) |     | Sorti (étape 3) |
| EF12 | L'étudiant consulte la note retenue et les commentaires reçus (Q8, RG18, RG21) (v2)          | Quand au moins une relecture de mon exercice est rendue, alors je vois la note retenue, marquée « provisoire » s'il en manque une, et les commentaires, sans aucun nom de relecteur ni dans l'écran ni dans la réponse de l'API                                                                                 | Must (v2)                 |
| EF13 | Protection contre la devinette des codes (Q4)                                                | Quand un étudiant a saisi 5 codes inconnus, alors ses tentatives sont refusées pendant 2 minutes                                                                                                                                                                                                                | Could                     |
| EF14 | La note retenue d'un exercice est la moyenne de ses relectures (RG21) (v2)                   | Quand les deux relectures sont rendues avec 13 et 16, alors la note retenue est 14,5 et elle n'est pas provisoire. Quand une seule est rendue avec 12, alors la note retenue est 12, marquée provisoire. Quand aucune n'est rendue, alors il n'y a pas de note                                                  | Must                      |

## 5. Exigences non fonctionnelles

| Réf  | Exigence                                                                                                                              | Comment on la vérifie                                                                                                                                                                                          |
| ---- | ------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| ENF1 | L'interface de marquage de présence est utilisable sur un téléphone                                                                   | Dans les outils de développement, mode appareil en 360 × 640 : parcours complet (choix du nom, saisie du code, dépôt du lien) sans zoom ni défilement horizontal                                               |
| ENF2 | Le tableau du formateur répond en moins de 2 s pour une promotion de 60 étudiants                                                     | Avec les données de démonstration, `Measure-Command` sur `GET /api/tableau` donne moins de 2 s sur 5 appels. Le calcul se fait en requêtes agrégées, sans une requête par étudiant                             |
| ENF3 | Toutes les erreurs suivent le format `{ code, message }`, avec un message en français                                                 | Test d'intégration sur un cas d'erreur. Appels manuels sur une route inexistante et avec un JSON mal formé : même format, jamais de stack trace                                                                |
| ENF4 | L'application démarre chez un tiers depuis le seul README, avec des données de démonstration                                          | Clone dans un dossier vide, `docker compose up`, puis ouverture du front : le tableau de la promotion de démonstration est rempli                                                                              |
| ENF5 | Les tests tournent sur un poste vierge, sans base locale                                                                              | `./mvnw test` passe sur un clone vierge, avec une base H2 en mémoire                                                                                                                                           |
| ENF6 | Les horodatages sont stockés en UTC et affichés dans l'heure locale du navigateur                                                     | Dans la réponse de `POST /api/sessions`, `expirationAt` vaut `ouvertureAt` plus 15 minutes. L'écran affiche l'heure locale                                                                                     |
| ENF7 | Le système tient la volumétrie visée : 60 étudiants par promotion, une quarantaine de sessions, environ 2 400 exercices par promotion | Des index existent sur toutes les clés étrangères et sur `code`                                                                                                                                                |
| ENF8 | Des opérations simultanées ne perdent aucune donnée : présences et relectures                                                         | Test `PresenceConcurrenceTest` : 8 marquages simultanés donnent 8 présences ; 5 envois simultanés du même étudiant donnent 1 présence et 4 réponses 409. Le rendu d'une relecture verrouille son exercice (v2) |

## 6. Règles de gestion

| Réf  | Règle                                                                                                                                                                                                                                                        | Source                             |
| ---- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------- |
| RG1  | Un code de présence expire 15 minutes après l'ouverture de la session                                                                                                                                                                                        | Q2                                 |
| RG2  | Un étudiant ne peut pas relire son propre exercice                                                                                                                                                                                                           | Q5                                 |
| RG3  | Une note **donnée** est un entier compris entre 0 et 20. Une valeur décimale comme 12.5 est refusée                                                                                                                                                          | Q9                                 |
| RG4  | Une présence par code n'est acceptée que si le code n'a pas expiré et que la session n'est pas clôturée                                                                                                                                                      | Q2, Q3                             |
| RG5  | Un étudiant a au plus une présence par session, quelle qu'en soit la source                                                                                                                                                                                  | Contrat (409 DEJA_PRESENT)         |
| RG6  | Un étudiant ne peut marquer sa présence que dans une session de sa propre promotion                                                                                                                                                                          | H9                                 |
| RG7  | Une présence ajoutée par le formateur porte la source FORMATEUR. Elle est acceptée même après l'expiration du code, tant que la session n'est pas clôturée                                                                                                   | Q14                                |
| RG8  | Un étudiant dépose au plus un exercice par session                                                                                                                                                                                                           | Contrat (409 EXERCICE_DEJA_DEPOSE) |
| RG9  | Seul un étudiant présent à la session peut y déposer un exercice                                                                                                                                                                                             | H4                                 |
| RG10 | Le dépôt est possible jusqu'à la clôture de la session, même après l'expiration du code                                                                                                                                                                      | Q12                                |
| RG11 | Un lien d'exercice est une URL absolue en http ou https                                                                                                                                                                                                      | Contrat (LIEN_INVALIDE)            |
| RG12 | **(v2)** Un exercice déposé a exactement deux relecteurs, distincts l'un de l'autre                                                                                                                                                                          | Enveloppe étape 3 (remplace Q6)    |
| RG13 | **(v2)** Les relecteurs sont tirés au hasard parmi les étudiants présents à la session, en excluant l'auteur et le relecteur déjà attribué au même exercice, et en ne retenant que les moins chargés en relectures dans cette session                        | Q7, H3, enveloppe                  |
| RG14 | **(v2)** S'il manque des candidats au dépôt, l'exercice reste « en attente d'attribution » avec les relecteurs déjà tirés. Le tirage des relecteurs manquants est retenté à chaque nouvelle présence dans la session                                         | H2, H19                            |
| RG15 | Une relecture rendue est définitive : ni la note ni le commentaire ne peuvent être modifiés                                                                                                                                                                  | Q15 (contradiction Q10 tranchée)   |
| RG16 | **(v2)** Le lien d'un exercice ne peut plus être remplacé dès qu'une de ses relectures est rendue                                                                                                                                                            | Q13, H6                            |
| RG17 | **(v2)** La moyenne d'un étudiant est la moyenne arithmétique des notes retenues de ses exercices ayant au moins une relecture rendue, arrondie à 2 décimales. Elle est signalée provisoire si l'une de ces notes l'est. Elle vaut null s'il n'a aucune note | Q16, contrat, H21                  |
| RG18 | L'auteur d'un exercice ne voit jamais le nom de ses relecteurs                                                                                                                                                                                               | Q8                                 |
| RG19 | Un code de présence est unique parmi toutes les sessions                                                                                                                                                                                                     | H8                                 |
| RG20 | Une session clôturée ne peut pas être rouverte. Une relecture attribuée peut encore être rendue après la clôture                                                                                                                                             | H10                                |
| RG21 | **(v2)** La note retenue d'un exercice est la moyenne des notes de ses relectures rendues, arrondie à 2 décimales. Elle est provisoire tant que moins de relectures que prévu sont rendues. L'exercice passe à « relu » quand toutes sont rendues            | Enveloppe étape 3, H20             |
| RG22 | **(v2)** Un exercice déposé avant le passage à deux relecteurs garde un seul relecteur, et sa note reste définitive                                                                                                                                          | H18                                |

## 7. Zones d'ombre, hypothèses et contradictions

**Le trou que personne n'a comblé :** Q7 dit parmi qui le relecteur est tiré, mais aucune question ne dit **quand** il l'est, ni **ce qui se passe s'il n'y a personne d'autre de présent** au moment du dépôt. Les hypothèses H2 et H3 le comblent, et H19 les prolonge pour deux relecteurs.

**Points que la demande ne tranche pas :**

| Point                                                                                                           | Réponse client (Qx) ou hypothèse                                         | Décision retenue                                                                                                                 | Conséquence                                                                                                 |
| --------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| H1 — « Fin de session » et « clôture » sont-elles la même chose ?                                               | Q3, Q10 et Q12 emploient les deux termes sans les définir                | Deux notions distinctes : la fin de la fenêtre de présence (expiration du code, Q2) et la clôture, action explicite du formateur | Statut de session OUVERTE ou CLOTUREE                                                                       |
| H2 — Moment du tirage du relecteur                                                                              | Q7 ne le précise pas (le trou)                                           | Tirage au dépôt. Sans candidat, statut EN_ATTENTE_ATTRIBUTION et nouvelle tentative à chaque nouvelle présence                   | Statut d'exercice dédié, voir D4 et RG14                                                                    |
| H3 — Répartition des relectures                                                                                 | Q7 dit « au hasard » sans parler d'équité                                | Au hasard parmi les présents qui ont le moins de relectures attribuées dans la session                                           | Personne ne cumule les relectures. Testé unitairement                                                       |
| H4 — Faut-il être présent pour déposer ?                                                                        | Rien ne le dit. Q7 raisonne sur les présents                             | Oui                                                                                                                              | 409 ETUDIANT_NON_PRESENT. Le formateur peut régulariser avec EF9                                            |
| H5 — Le relecteur doit-il avoir déposé lui-même ?                                                               | Non précisé                                                              | Non, seule sa présence compte                                                                                                    | Le vivier de candidats est plus large                                                                       |
| H6 — Que veut dire « personne n'a commencé à le relire » (Q13) ?                                                | La relecture est rendue en un seul envoi                                 | « Commencé » équivaut à « une relecture est rendue » (v2)                                                                        | RG16                                                                                                        |
| H7 — Comment connaître l'appelant sans authentification (Q1), alors que le contrat prévoit 403 AUTO_RELECTURE ? | Q1 exclut le mot de passe                                                | En-tête optionnel `X-Etudiant-Id`, plus un contrôle défensif au tirage (RG2)                                                     | 403 AUTO_RELECTURE ou RELECTEUR_NON_ATTRIBUE. Garde-fou contre l'erreur, pas une sécurité                   |
| H8 — Le contrat de présence ne transmet pas `sessionId`                                                         | Contrat imposé                                                           | Le code seul identifie la session : il est unique et sans caractères ambigus                                                     | RG19                                                                                                        |
| H9 — Un étudiant d'une autre promotion saisit un code                                                           | Non précisé                                                              | Refusé                                                                                                                           | 400 ETUDIANT_HORS_PROMOTION (RG6)                                                                           |
| H10 — Peut-on rendre une relecture après la clôture ?                                                           | Non précisé                                                              | Oui                                                                                                                              | La clôture ne fait perdre aucune note (RG20)                                                                |
| H11 — Présence manuelle après expiration ou après clôture                                                       | Q14                                                                      | Autorisée après l'expiration, refusée après la clôture                                                                           | Q14 reste utile, Q3 est respecté                                                                            |
| H12 — Cas d'erreur non prévus dans les 5 opérations imposées                                                    | Contrat « à la lettre »                                                  | Aucun statut HTTP ajouté pour un cas métier. Une requête illisible renvoie partout 400 REQUETE_INVALIDE                          | Respect de B2                                                                                               |
| H13 — Q16 demande « sa présence à chaque session »                                                              | Le contrat fixe `presences` à un entier                                  | Le tableau respecte le contrat et affiche un total                                                                               | Détail par session exclu                                                                                    |
| H14 — Blocage après cinq erreurs (Q4)                                                                           | Le contrat ne prévoit aucun statut 429                                   | Reporté en Could (EF13)                                                                                                          | Aucun impact sur le contrat                                                                                 |
| H15 — Moyenne d'un étudiant sans note                                                                           | Contrat : `moyenne` nullable                                             | null, affichée « — »                                                                                                             | RG17                                                                                                        |
| H16 — Une note décimale donnée, par exemple 12.5                                                                | Q9                                                                       | Refusée avec 400 NOTE_INVALIDE                                                                                                   | RG3                                                                                                         |
| H17 — `GET /api/sessions` renvoie les codes de présence                                                         | Sans authentification (Q1), n'importe qui peut l'appeler                 | L'écran étudiant n'affiche jamais le code                                                                                        | Risque résiduel assumé, conséquence de Q1                                                                   |
| H18 — **(v2)** Que deviennent les exercices déposés avant le changement ?                                       | L'enveloppe ne le dit pas                                                | Non rétroactif : ils gardent un seul relecteur, et leur note reste définitive                                                    | Colonne `relecteurs_attendus` (1 pour l'existant, 2 ensuite) dans la migration V101 ; RG22                  |
| H19 — **(v2)** Un seul candidat présent au dépôt                                                                | L'enveloppe ne le dit pas                                                | On attribue celui qui est disponible ; le second est tiré dès qu'un autre étudiant devient présent                               | L'exercice peut avoir une relecture rendue alors qu'il attend encore son second relecteur : note provisoire |
| H20 — **(v2)** La note retenue peut-elle être décimale ?                                                        | Q9 impose des entiers ; la moyenne de 13 et 16 vaut 14,5                 | Q9 porte sur la note **donnée** par un relecteur. La note **retenue** est une moyenne, arrondie à 2 décimales                    | RG3 inchangée ; RG21                                                                                        |
| H21 — **(v2)** La moyenne du tableau compte-t-elle les notes provisoires ?                                      | L'enveloppe demande d'afficher la note provisoire « en attendant »       | Oui, et le tableau signale qu'elle contient du provisoire                                                                        | Champ `moyenneProvisoire` ajouté au contrat du tableau                                                      |
| H22 — **(v2)** Numéro de la nouvelle migration                                                                  | Flyway refuse d'appliquer une version inférieure à la dernière appliquée | La migration de l'étape 3 est V101 : les bases de démonstration ont déjà appliqué V100                                           | C9 ; la base remplie survit                                                                                 |

**Contradictions relevées :**

| Réponses en conflit                                                                                                               | Ce que j'ai choisi                                   | Pourquoi                                                                                                                                                                                        |
| --------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Q10 (« le relecteur peut corriger sa note tant que la session n'est pas clôturée ») contre Q15 (« une fois validée, c'est fini ») | **Q15** : une relecture rendue est définitive (RG15) | 1) Le contrat imposé prévoit 409 RELECTURE_DEJA_RENDUE : retenir Q10 violerait B2. 2) Q15 est motivée par le client, Q10 est un oui sans justification. 3) Q15 simplifie le modèle              |
| Q3 contre Q12 — fausse contradiction                                                                                              | Les deux s'appliquent                                | Elles portent sur deux actions différentes. Voir H1                                                                                                                                             |
| **(v2)** Q6 (« un seul relecteur ») contre l'enveloppe de l'étape 3 (« deux pairs différents »)                                   | **L'enveloppe**                                      | C'est la demande la plus récente du client, motivée par un problème constaté en usage réel (un relecteur qui ne rend rien laisse l'étudiant sans note). Q6 devient obsolète ; RG12 est réécrite |

## 8. Contraintes techniques

**Imposées par le sujet :**

- **B1** : Java 17 ou plus, Maven, wrapper `mvnw` commité.
- **B2** : `api/contrat.yaml` respecté à la lettre.
- **B3** : couches contrôleur, service et repository séparées, uniquement des DTO en JSON.
- **B4** : validation des entrées et gestion centralisée des erreurs, aucune stack trace renvoyée.
- **B5** : schéma versionné par Flyway, `ddl-auto=validate` partout.
- **B6** : un test unitaire sur une règle réelle (tirage des relecteurs : RG2, RG12, RG13, RG14) et un test d'intégration sur `POST /api/presences`.
- **F1** : React déclaré et justifié dans le README, et le build passe.
- **F2** : trois écrans (formateur, étudiant, relecteur).
- **F3** : appels API dans `frontend/src/api/`, états de chargement et d'erreur gérés, aucune note ni moyenne recalculée dans le front.

**Que je m'impose :**

- **C1** : PostgreSQL 16 à l'exécution et H2 en mode PostgreSQL pour les tests.
- **C2** : toute évolution du schéma passe par une nouvelle migration. Une migration déjà poussée n'est jamais modifiée.
- **C3** : les données de démonstration sont dans des migrations séparées (`db/demo`), activées par le profil `demo`.
- **C4** : les horodatages sont en `timestamp with time zone` côté base et en `Instant` côté Java.
- **C5** : Jackson refuse de tronquer les décimaux en entiers (RG3).
- **C6** : Git. Une branche par issue, une PR par branche, des commits qui citent les EF et RG.
- **C7** : aucun secret dans le dépôt.
- **C8** : le front utilise React 18, Vite, TypeScript et React Router.
- **C9 (v2)** : une nouvelle migration porte un numéro supérieur à la dernière appliquée sur toutes les bases existantes, démonstration comprise (H22).

## 9. Livrables

- Le dépôt public `kfokam48-epreuve-190`, avec `/docs`, `/api`, `/backend` et `/frontend`.
- `docs/CAHIER_DES_CHARGES.md`, tenu à jour.
- `docs/JOURNAL.md`, avec une entrée par étape.
- `docs/diagrammes/` : D1 à D4.
- `api/contrat.yaml`.
- Le backlog en issues et les PR liées.
- Les jalons `[JALON] analyse`, `[JALON] v0.1` et `[JALON] v1.0`.
- `README.md`, `CHANGELOG.md` et `docker-compose.yml`.

## 10. Démarche prévue

1. **Analyse** avant tout code, puis `[JALON] analyse`.
2. **Première version** : les Must, une issue par branche et par PR, puis `[JALON] v0.1`.
3. **Enveloppe** : issues ouvertes avant le code ; bug reproduit par un test qui échoue puis corrigé, sur sa propre branche ; évolution sur des branches séparées, analyse mise à jour en premier, nouvelle migration V101.
4. **Version finale** : l'évolution, puis EF8 et EF9 s'il reste du temps, puis le CHANGELOG et le README.

**Repriorisation de l'étape 3 (v2) :** le bug d'abord, parce qu'une perte de données passe avant tout. Ensuite l'évolution 1 (deux relecteurs), puis l'évolution 2 (note retenue, qui intègre EF12). Ensuite EF8 et EF9. EF10 et EF11 sont sorties du périmètre (section 3).

**Definition of Done — un ticket est terminé quand :**

- tous ses critères d'acceptation ont été vérifiés à la main ;
- le code est sur une branche dédiée, fusionnée par une PR qui contient `Closes #n` ;
- `./mvnw verify` et `npm run build` passent, et les tests restent verts ;
- les erreurs suivent le format du contrat ;
- si le ticket touche au schéma ou à l'API, la migration, D2 et `contrat.yaml` sont cohérents ;
- les messages de commit citent l'EF ou la RG concernée.

---

## Journal des révisions

| Version | Quand                          | Ce qui a changé et pourquoi                                                                                                                                                                                                                                                                                          |
| ------- | ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1       | 25/09/2026, étape 1            | Version initiale                                                                                                                                                                                                                                                                                                     |
| 1.1     | 25/09/2026, issue #5           | Ajout de H17 : exposition des codes par `GET /api/sessions`                                                                                                                                                                                                                                                          |
| 1.2     | 25/09/2026, étape 3            | Ajout de ENF8 après le bug des marquages simultanés                                                                                                                                                                                                                                                                  |
| 2       | Étape 3, issues #<E1> et #<E2> | **Conséquence du changement de besoin de l'enveloppe** : deux relecteurs par exercice et note retenue. RG12, RG13, RG14, RG16 et RG17 réécrites ; RG21 et RG22 ajoutées ; EF5, EF6, EF7 et EF12 modifiées ; EF14 ajoutée ; H18 à H22 et la contradiction Q6 / enveloppe ajoutées ; EF10 et EF11 sorties du périmètre |
