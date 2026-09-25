# D4 — Cycle de vie d'un exercice (version 2)

Les états correspondent à la colonne `exercice.statut` (D2). **Version 2 (étape 3) :** l'exercice attend `relecteurs_attendus` relecteurs, c'est-à-dire 2, ou 1 pour les exercices déposés avant le changement (RG22).

```mermaid
stateDiagram-v2
    [*] --> EN_ATTENTE_ATTRIBUTION : dépôt, moins de candidats que de relecteurs attendus (RG14)
    [*] --> EN_ATTENTE_RELECTURE : dépôt, tous les relecteurs tirés (RG12, RG13)
    EN_ATTENTE_ATTRIBUTION --> EN_ATTENTE_ATTRIBUTION : un relecteur tiré sur deux, ou une relecture rendue (note provisoire, RG21)
    EN_ATTENTE_ATTRIBUTION --> EN_ATTENTE_RELECTURE : le relecteur manquant est tiré à l'arrivée d'un étudiant (RG14)
    EN_ATTENTE_RELECTURE --> EN_ATTENTE_RELECTURE : une relecture rendue sur deux (note provisoire, RG21)
    EN_ATTENTE_RELECTURE --> RELU : toutes les relectures attendues rendues (RG21)
    RELU --> [*]

    note right of EN_ATTENTE_RELECTURE
        Visible dans la liste des exercices
        en attente du formateur (Q11, EF8)
    end note

    note right of RELU
        Note retenue définitive : moyenne
        des relectures rendues (RG21, RG15)
    end note
```

**Note provisoire (RG21) :** dès qu'une relecture est rendue, l'exercice a une note retenue, provisoire tant qu'il n'est pas RELU. Ce n'est pas un état à part : c'est une information calculée à partir du nombre de relectures rendues.

**Hors périmètre depuis l'étape 3 :** le remplacement du lien (EF11) et la clôture de session (EF10) ne figurent plus dans ce cycle de vie.
