# D1 — Cas d'utilisation

Mermaid n'a pas de diagramme de cas d'utilisation natif : les acteurs sont en rectangles, les cas d'utilisation en ovales à l'intérieur du système.

```mermaid
flowchart LR
    formateur["Formateur"]
    etudiant["Étudiant"]
    relecteur["Relecteur<br/>(étudiant désigné)"]
    systeme["Système"]

    subgraph app["KF48 Présences & Relectures"]
        direction TB
        uc1(["EF1 Ouvrir une session et obtenir le code"])
        uc9(["EF9 Ajouter une présence à la main"])
        uc10(["EF10 Clôturer une session"])
        uc7(["EF7 Consulter le tableau"])
        uc8(["EF8 Voir les exercices en attente"])
        uc2(["EF2 Se choisir dans la liste"])
        uc3(["EF3 Marquer sa présence"])
        uc4(["EF4 Déposer son exercice"])
        uc11(["EF11 Remplacer le lien"])
        uc12(["EF12 Voir sa note et le commentaire"])
        uc6(["EF6 Rendre une relecture"])
        uc5(["EF5 Attribuer un relecteur au hasard"])
    end

    formateur --- uc1
    formateur --- uc9
    formateur --- uc10
    formateur --- uc7
    formateur --- uc8

    etudiant --- uc2
    etudiant --- uc3
    etudiant --- uc4
    etudiant --- uc11
    etudiant --- uc12

    relecteur --- uc6
    systeme --- uc5

    relecteur -.->|est un| etudiant
    uc4 -.->|déclenche| uc5
    uc3 -.->|peut déclencher RG14| uc5
```

Le relecteur n'est pas un acteur distinct : c'est un étudiant désigné sur une relecture (section 2 du cahier des charges).
