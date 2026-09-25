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
