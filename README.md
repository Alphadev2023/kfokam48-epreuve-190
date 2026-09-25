# KF48 Présences & Relectures

Application de présence par code et de relecture entre pairs pour la formation KFOKAM48.
Analyse complète : [docs/CAHIER_DES_CHARGES.md](docs/CAHIER_DES_CHARGES.md) · Contrat d'API : [api/contrat.yaml](api/contrat.yaml)

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

Des données de démonstration sont chargées au démarrage : « Promotion A » (60 étudiants, deux séances, relectures dans tous les états) et « Promotion B » (petite promotion pour tester à la main).

Pour repartir d'une base vide : `docker compose down -v`.

## Lancer les tests (sans base locale)

```bash
cd backend
./mvnw test        # Windows : .\mvnw.cmd test
```

## Développement sans Docker pour le code

```bash
docker compose up db                                           # PostgreSQL seul, port 5433
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
cd frontend && npm install && npm run dev                      # http://localhost:5173
```
