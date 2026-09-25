# D4 — Cycle de vie d'un exercice

Les états correspondent à la colonne `exercice.statut` (D2).

```mermaid
stateDiagram-v2
    [*] --> EN_ATTENTE_ATTRIBUTION : dépôt, aucun autre étudiant présent (RG14)
    [*] --> EN_ATTENTE_RELECTURE : dépôt, relecteur tiré au hasard (RG12, RG13)
    EN_ATTENTE_ATTRIBUTION --> EN_ATTENTE_RELECTURE : un autre étudiant devient présent (RG14)
    EN_ATTENTE_ATTRIBUTION --> EN_ATTENTE_ATTRIBUTION : lien remplacé (RG10)
    EN_ATTENTE_RELECTURE --> EN_ATTENTE_RELECTURE : lien remplacé (RG10, RG16)
    EN_ATTENTE_RELECTURE --> RELU : relecture rendue, note entière 0 à 20 (RG3, RG15)
    RELU --> [*]

    note right of EN_ATTENTE_RELECTURE
        Visible dans la liste des exercices
        en attente du formateur (Q11, EF8)
    end note

    note right of RELU
        Définitif : ni la note ni le lien
        ne peuvent plus changer (Q15, RG16)
    end note
```

**Effet de la clôture de session :** plus aucun remplacement de lien (RG10) et plus aucune nouvelle présence, donc plus d'attribution différée. Un exercice encore EN_ATTENTE_ATTRIBUTION y reste et demeure visible du formateur. Une relecture déjà attribuée peut toujours être rendue (RG20).
