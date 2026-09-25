# Journal de bord — KF48-DLA-190

## Étape 1 — Analyse et conception

**Fait :** cahier des charges v1 (13 EF dont 7 Must, 20 RG, 16 hypothèses, 1 contradiction tranchée), 4 diagrammes Mermaid (D1 à D3 plus le bonus D4), 14 issues étiquetées Must, Should et Could, contrat complété avec 9 opérations, `[JALON] analyse` poussé.

**Bloqué :** ** min sur Q10 contre Q15. Je l'ai tranchée pour Q15, parce que le contrat imposé renvoie 409 RELECTURE_DEJA_RENDUE : suivre Q10 violerait B2. L'exemple du modèle, qui retient Q10, contredit le contrat. ** min sur le trou : quand tirer le relecteur si l'auteur est seul présent (H2).

**IA :** Claude a produit un premier jet du cahier des charges, des diagrammes, du contrat et des issues. Vérifications :

- chaque RG renvoie à une Qx ou à une hypothèse écrite en section 7 ;
- chaque code HTTP de D3 a été recoupé avec `contrat.yaml` ;
- chaque colonne de D2 correspond à une contrainte ou une règle citée ;
- le contrat est validé sur editor.swagger.io ;
- les diagrammes s'affichent dans l'aperçu GitHub ;
- chaque titre d'issue est relu en me demandant si le client le comprendrait.
  | 1.1 | 25/09/2026, issue #5 | Ajout de H17 : exposition des codes par GET /api/sessions, découverte en construisant l'écran étudiant |

  ## Étape 2 — Première version

**Fait :** issues Must #1 à #8 livrées, une branche et une PR par issue, sauf #2 (voir Bloqué). API conforme aux 5 opérations imposées, 3 écrans (formateur, étudiant, relecteur), données de démonstration, `docker compose up`. Tests verts sur H2 : unitaires RG1, RG2, RG11, RG13 et RG14, intégration de chaque endpoint. Tableau mesuré à \_\_ ms pour 60 étudiants (ENF2).

**Bloqué :** ** min. Les commits de l'issue #2 ont été poussés sur `main` sans branche. J'ai choisi de ne pas réécrire l'historique (pas de `push --force` sur `main`) ; l'issue a été fermée avec un commentaire qui renvoie aux commits, et un hook local `commit-msg` refuse désormais tout commit de code sur `main`. ** min sur la note 12.5, que Jackson transformait en `REQUETE_INVALIDE` au lieu de `NOTE_INVALIDE` : corrigé dans le gestionnaire d'erreurs.

**IA :** Claude a produit le code de chaque issue. Vérifications :

- `./mvnw test` après chaque issue ;
- chaque critère d'acceptation vérifié à la main dans l'application avant de cocher la case ;
- les codes HTTP comparés au contrat pour chaque endpoint ;
- les types de colonnes de D2 corrigés quand la validation Hibernate les a refusés (smallint, text) ;
- H17 (codes exposés par `GET /api/sessions`) relevé en relisant l'écran étudiant.

| 1.2 | 25/09/2026, étape 3, issue #<B> | Ajout de ENF8 après le bug de marquages simultanés signalé par le client ; D3 montre le verrou |

## Étape 3 — Enveloppe

> Entrées des étapes 3 à 6 rédigées après la soumission, à la demande du formateur, pour terminer le projet.

**Fait :** issues #22 (bug), #24 et #25 (évolution) créées avant tout code. Bug reproduit par `PresenceConcurrenceTest`, commité avant le correctif (660ddc0, puis b7bc6b9), et corrigé par un verrou pessimiste sur la session (PR #26). Évolution : cahier des charges v2, D2 et D4 mis à jour dans un commit dédié, migration V101 ajoutée sans modifier V1, deux relecteurs par exercice (PR #27), puis la note retenue et la note provisoire (#25).

**Bloqué :** \_\_ min. Les commits de l'évolution ont été faits sur une branche nommée `22-bug-presences-simultanees`, fusionnée par la PR #27. Je n'ai pas réécrit `main` : j'ai corrigé le titre et la description de la PR et fermé les issues à la main. Les emplacements `#<B>` et `#<E1>` sont restés dans plusieurs messages de commit, corrigés dans les documents par une PR dédiée. Flyway refusait une migration V2 sur les bases qui avaient déjà appliqué la démonstration V100 : la migration est devenue V101 (H22).

**IA :** Claude a proposé la traduction du bug, le test de concurrence, le verrou et le plan de migration. Vérifications : le test a été lancé sans le correctif pour constater l'échec, puis avec ; la migration V101 a été appliquée sur une base de démonstration déjà remplie, sans perte de données ; chaque règle modifiée a été relue dans le cahier des charges v2.

**Ce que j'ai sorti du périmètre pour absorber le changement, et pourquoi :** la clôture de session (#11) et le remplacement du lien (#12). Ce sont les deux exigences dont l'absence coûte le moins : sans clôture, le dépôt reste possible, dans l'esprit de Q12 ; sans remplacement, l'étudiant signale son erreur au formateur. J'ai gardé #9 et #10, demandées explicitement par le client (Q11, Q14).

---

## Étape 4 — Version finale

**Fait :** issues #25 (note retenue, intègre #13), #9 (exercices en attente) et #10 (présence manuelle) livrées, chacune par branche et PR. Contrat 2.0. CHANGELOG et README testés depuis un clone vierge. Backlog restant trié : #11 et #12 hors périmètre, #14 en Could. `[JALON] v1.0`.

**Bloqué :** \_\_ min. Des erreurs d'indentation dans `contrat.yaml`, détectées sur editor.swagger.io et corrigées. Des modifications de l'issue #10 faites sur `main` : le hook `commit-msg` a refusé le commit, et le travail a été déplacé sur la branche de l'issue sans rien pousser sur `main`.

**IA :** code des trois issues. Vérifications : `./mvnw test` après chaque issue, `npm run build`, chaque critère d'acceptation vérifié à la main avec les données de démonstration (notes provisoires des étudiants 5 à 8, attribution déclenchée par l'ajout manuel en promotion B).

---

## Étape 5 — Épreuve Git

**Fait :** non réalisée.

**Bloqué :** faute de temps avant 18h00, j'ai privilégié la fin de l'étape 3. C'était une erreur de priorité : l'épreuve valait 17 points pour une vingtaine de minutes.

**IA :** sans objet.

---

## Étape 6 — Soumission

**Fait :** SOUMISSION.md déposé sur la plateforme avant 18h00, avec le hash de `main` à ce moment-là. Le travail décrit aux étapes 3 et 4 a été terminé ensuite, à la demande du formateur.

**Ce que je referais autrement avec une journée de plus :** faire l'épreuve Git dès la fin de l'étape 2, parce qu'elle est courte et très rentable. Vérifier `git branch` avant chaque premier commit d'une issue. Remplacer les emplacements comme `#<B>` avant de lancer une commande, pas après. Numéroter les données de démonstration dans un espace de versions séparé dès le départ, pour ne pas bloquer une migration future.

| 2.1 | Après la soumission, issue #11 | EF10 (clôture) réintégrée dans le périmètre à la demande du formateur |
