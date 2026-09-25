# D2 — Modèle de données

Ce diagramme décrit exactement la migration `backend/src/main/resources/db/migration/V1__schema.sql`. Toute nouvelle migration qui touche au schéma met ce fichier à jour dans le même commit.

```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : regroupe
    PROMOTION ||--o{ SESSION_COURS : planifie
    SESSION_COURS ||--o{ PRESENCE : enregistre
    ETUDIANT ||--o{ PRESENCE : marque
    SESSION_COURS ||--o{ EXERCICE : recoit
    ETUDIANT ||--o{ EXERCICE : depose
    EXERCICE ||--o| RELECTURE : "est relu par"
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
        timestamptz depose_at "non nul"
        timestamptz modifie_at "nul si jamais remplace"
    }
    RELECTURE {
        bigint id PK
        bigint exercice_id FK "UK, un seul relecteur, RG12"
        bigint relecteur_id FK "etudiant, different de l'auteur, RG2"
        smallint note "0 a 20, nul tant que non rendue, RG3"
        text commentaire "nul tant que non rendue"
        timestamptz attribuee_at "non nul"
        timestamptz rendue_at "nul tant que non rendue, RG15"
    }
```

**Contraintes non représentables dans le diagramme, présentes dans la migration :**

| Table           | Contrainte                                                                   | Règle |
| --------------- | ---------------------------------------------------------------------------- | ----- |
| `presence`      | `UNIQUE (session_id, etudiant_id)`                                           | RG5   |
| `presence`      | `CHECK (source IN ('ETUDIANT','FORMATEUR'))`                                 | RG7   |
| `exercice`      | `UNIQUE (session_id, etudiant_id)`                                           | RG8   |
| `exercice`      | `CHECK (statut IN ('EN_ATTENTE_ATTRIBUTION','EN_ATTENTE_RELECTURE','RELU'))` | D4    |
| `relecture`     | `UNIQUE (exercice_id)`                                                       | RG12  |
| `relecture`     | `CHECK (note BETWEEN 0 AND 20)`                                              | RG3   |
| `session_cours` | `CHECK (statut IN ('OUVERTE','CLOTUREE'))`                                   | H1    |
| toutes          | index sur chaque clé étrangère                                               | ENF7  |

La table s'appelle `session_cours` et non `session`, parce que `SESSION` est un mot réservé dans certains moteurs SQL, dont H2 utilisé pour les tests.
