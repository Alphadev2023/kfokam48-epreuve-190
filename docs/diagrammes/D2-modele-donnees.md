# D2 — Modèle de données (version 2)

Ce diagramme décrit le schéma obtenu après les migrations `V1__schema.sql` et `V101__deux_relecteurs.sql`. Il est mis à jour à chaque migration qui touche au schéma.

**Version 2 (étape 3) :** un exercice a désormais jusqu'à deux relectures, une par relecteur distinct. La colonne `relecteurs_attendus` distingue les exercices déposés avant le changement (1) de ceux déposés après (2).

```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : regroupe
    PROMOTION ||--o{ SESSION_COURS : planifie
    SESSION_COURS ||--o{ PRESENCE : enregistre
    ETUDIANT ||--o{ PRESENCE : marque
    SESSION_COURS ||--o{ EXERCICE : recoit
    ETUDIANT ||--o{ EXERCICE : depose
    EXERCICE ||--o{ RELECTURE : "est relu par (0 a 2)"
    ETUDIANT ||--o{ RELECTURE : relit

    PROMOTION {
        bigint id PK
        varchar nom UK "100, non nul"
    }
    ETUDIANT {
        bigint id PK
        varchar nom "120, non nul"
        bigint promotion_id FK "non nul"
    }
    SESSION_COURS {
        bigint id PK
        varchar titre "200, non nul"
        bigint promotion_id FK "non nul"
        varchar code UK "6 caracteres, RG19"
        timestamptz ouverture_at "non nul"
        timestamptz expiration_at "ouverture + 15 min, RG1"
        varchar statut "OUVERTE ou CLOTUREE"
        timestamptz cloture_at "nul tant que ouverte"
    }
    PRESENCE {
        bigint id PK
        bigint session_id FK "non nul"
        bigint etudiant_id FK "non nul"
        varchar source "ETUDIANT ou FORMATEUR, RG7"
        timestamptz marquee_at "non nul"
    }
    EXERCICE {
        bigint id PK
        bigint session_id FK "non nul"
        bigint etudiant_id FK "auteur, non nul"
        varchar lien "500, http ou https, RG11"
        varchar statut "voir D4"
        integer relecteurs_attendus "1 avant V101, 2 apres, RG12 et RG22"
        timestamptz depose_at "non nul"
        timestamptz modifie_at "nul si jamais remplace"
    }
    RELECTURE {
        bigint id PK
        bigint exercice_id FK "non nul"
        bigint relecteur_id FK "etudiant, different de l'auteur, RG2"
        integer note "0 a 20, nul tant que non rendue, RG3"
        varchar commentaire "2000, nul tant que non rendue"
        timestamptz attribuee_at "non nul"
        timestamptz rendue_at "nul tant que non rendue, RG15"
    }
```

**Contraintes non représentables dans le diagramme, présentes dans les migrations :**

| Table           | Contrainte                                                                                       | Règle      | Migration                                    |
| --------------- | ------------------------------------------------------------------------------------------------ | ---------- | -------------------------------------------- |
| `presence`      | `UNIQUE (session_id, etudiant_id)`                                                               | RG5        | V1                                           |
| `presence`      | `CHECK (source IN ('ETUDIANT','FORMATEUR'))`                                                     | RG7        | V1                                           |
| `exercice`      | `UNIQUE (session_id, etudiant_id)`                                                               | RG8        | V1                                           |
| `exercice`      | `CHECK (statut IN ('EN_ATTENTE_ATTRIBUTION','EN_ATTENTE_RELECTURE','RELU'))`                     | D4         | V1                                           |
| `exercice`      | `CHECK (relecteurs_attendus BETWEEN 1 AND 2)`                                                    | RG12, RG22 | V101                                         |
| `relecture`     | `UNIQUE (exercice_id, relecteur_id)` : un même relecteur ne relit pas deux fois le même exercice | RG12       | V101 (remplace `UNIQUE (exercice_id)` de V1) |
| `relecture`     | `CHECK (note BETWEEN 0 AND 20)`                                                                  | RG3        | V1                                           |
| `session_cours` | `CHECK (statut IN ('OUVERTE','CLOTUREE'))`                                                       | H1         | V1                                           |
| toutes          | index sur chaque clé étrangère                                                                   | ENF7       | V1                                           |

La table s'appelle `session_cours` et non `session`, parce que `SESSION` est un mot réservé dans certains moteurs SQL, dont H2 utilisé pour les tests.
