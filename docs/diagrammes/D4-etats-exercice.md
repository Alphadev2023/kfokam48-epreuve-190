# D4 — Cycle de vie d'un exercice (version 2.2)

Les états correspondent à la colonne `exercice.statut` (D2). L'exercice attend `relecteurs_attendus` relecteurs : 2, ou 1 pour les exercices déposés avant l'étape 3 (RG22).

```mermaid
stateDiagram-v2
    [*] --> EN_ATTENTE_ATTRIBUTION : dépôt, moins de candidats que de relecteurs attendus (RG14)
    [*] --> EN_ATTENTE_RELECTURE : dépôt, tous les relecteurs tirés (RG12, RG13)
    EN_ATTENTE_ATTRIBUTION --> EN_ATTENTE_ATTRIBUTION : relecteur tiré ou relecture rendue, ou lien remplacé tant qu'aucune relecture n'est rendue (RG16)
    EN_ATTENTE_ATTRIBUTION --> EN_ATTENTE_RELECTURE : le relecteur manquant est tiré à l'arrivée d'un étudiant (RG14)
    EN_ATTENTE_RELECTURE --> EN_ATTENTE_RELECTURE : une relecture rendue sur deux (note provisoire, RG21), ou lien remplacé tant qu'aucune n'est rendue (RG16)
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

**Note provisoire (RG21) :** dès qu'une relecture est rendue, l'exercice a une note retenue, provisoire tant qu'il n'est pas RELU. C'est une information calculée, pas un état à part.

**Remplacement du lien (EF11, RG16) :** possible tant qu'aucune relecture n'est rendue et que la séance n'est pas clôturée. Il ne change pas l'état.

**Effet de la clôture (EF10, RG10, RG20) :** plus de remplacement de lien ni de nouvelle présence, donc plus d'attribution différée. Un exercice encore EN_ATTENTE_ATTRIBUTION y reste, visible du formateur. Une relecture déjà attribuée peut toujours être rendue.
