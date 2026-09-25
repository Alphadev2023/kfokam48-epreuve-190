# KF48 Présences & Relectures

Application de présence par code et de relecture entre pairs pour la formation KFOKAM48.

- Analyse : [docs/CAHIER_DES_CHARGES.md](docs/CAHIER_DES_CHARGES.md) et [docs/diagrammes/](docs/diagrammes/)
- Contrat d'API : [api/contrat.yaml](api/contrat.yaml)
- Historique des versions : [CHANGELOG.md](CHANGELOG.md)

**Frontend : React (Vite + TypeScript)**, parce que trois écrans simples ne justifient pas la structure d'Angular et que React donne le build le plus rapide à vérifier depuis un clone vierge.

**Backend :** Java 17, Spring Boot 3.3, PostgreSQL 16, migrations Flyway.

## Démarrer (Docker requis)

```bash
git clone https://github.com/Alphadev2023/kfokam48-epreuve-190.git
cd kfokam48-epreuve-190
docker compose up --build
```

- Application : http://localhost:3000
- API : http://localhost:8080/api/promotions

Le port 8080 doit être libre. Pour repartir d'une base vide : `docker compose down -v`.

## Données de démonstration

Chargées automatiquement au démarrage (profil `demo`) :

| Promotion             | Contenu                                  | À vérifier                                                                                                      |
| --------------------- | ---------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| Promotion A           | 60 étudiants, trois séances passées      | Tableau complet, moyennes, relectures à rendre                                                                  |
| Promotion A, séance 3 | Exercices à deux relecteurs              | Étudiants 1 à 4 : note définitive. Étudiants 5 à 8 (par exemple Fotso Aminata) : note **provisoire**            |
| Promotion B           | 6 étudiants, une séance de démonstration | Awa a déposé seule : son exercice attend des relecteurs. Ajouter une présence à la main déclenche l'attribution |

## Les trois écrans

- **Formateur** (`/formateur`) : ouvrir une séance et afficher son code, voir les présences d'une séance et en ajouter à la main, tableau de la promotion, exercices en attente.
- **Étudiant** (`/etudiant`) : se choisir dans la liste, marquer sa présence avec le code, déposer son exercice, voir sa note retenue et les commentaires.
- **Relecteur** (`/relecteur`) : voir les exercices à relire, rendre une note sur 20 et un commentaire.

## Lancer les tests (sans base locale)

```bash
cd backend
./mvnw test        # Windows : .\mvnw.cmd test
```

Les tests tournent sur une base H2 en mémoire. Ils couvrent notamment les règles de tirage (RG2, RG12, RG13), la note retenue (RG21), chaque code HTTP du contrat, et les marquages simultanés (ENF8).

## Développement sans Docker pour le code

```bash
docker compose up db                                           # PostgreSQL seul, port 5433
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
cd frontend && npm install && npm run dev                      # http://localhost:5173
```

## Hors périmètre de la version 1.0

La clôture de session, le remplacement du lien et le blocage après cinq codes erronés ne sont pas implémentés (issues #11, #12 et #14). La justification figure dans la section 3 du cahier des charges.
