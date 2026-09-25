-- Donnees de demonstration (profil demo uniquement).
-- Promotion A : 60 etudiants et deux seances passees, avec des relectures dans tous les etats (ENF2, ENF4).
-- Promotion B : petite promotion pour la demonstration manuelle, dont un exercice EN_ATTENTE_ATTRIBUTION (RG14).

INSERT INTO promotion (id, nom) VALUES
                                    (1, 'Promotion A - Fullstack 2026'),
                                    (2, 'Promotion B - Demonstration');

-- 60 etudiants : 20 noms x 3 prenoms, tous distincts
INSERT INTO etudiant (id, nom, promotion_id)
SELECT g,
       (ARRAY['Abena','Bello','Diallo','Essomba','Fotso','Kamga','Mbarga','Ngono','Onana','Tchoua',
        'Ateba','Biya','Djomo','Eto','Fouda','Manga','Nkoulou','Owona','Talla','Zambo'])[((g - 1) % 20) + 1]
       || ' ' ||
       (ARRAY['Aminata','Brice','Carine'])[((g - 1) / 20) + 1],
       1
FROM generate_series(1, 60) AS g;

INSERT INTO etudiant (id, nom, promotion_id) VALUES
                                                 (61, 'Ndiaye Awa', 2),
                                                 (62, 'Sow Mamadou', 2),
                                                 (63, 'Camara Fatou', 2),
                                                 (64, 'Barry Ousmane', 2),
                                                 (65, 'Keita Mariama', 2),
                                                 (66, 'Toure Ibrahim', 2);

-- Sessions : codes deja expires (ouverture dans le passe)
INSERT INTO session_cours (id, titre, promotion_id, code, ouverture_at, expiration_at, statut, cloture_at) VALUES
                                                                                                               (1, 'Seance 1 - Git et GitHub', 1, 'HSTAA2',
                                                                                                                now() - INTERVAL '14 days', now() - INTERVAL '14 days' + INTERVAL '15 minutes',
                                                                                                                'CLOTUREE', now() - INTERVAL '14 days' + INTERVAL '3 hours'),
                                                                                                               (2, 'Seance 2 - Spring Boot', 1, 'HSTBB3',
                                                                                                                now() - INTERVAL '1 day', now() - INTERVAL '1 day' + INTERVAL '15 minutes',
                                                                                                                'OUVERTE', NULL),
                                                                                                               (3, 'Seance de demonstration', 2, 'HSTCC4',
                                                                                                                now() - INTERVAL '2 hours', now() - INTERVAL '2 hours' + INTERVAL '15 minutes',
                                                                                                                'OUVERTE', NULL);

-- Seance 1 : etudiants 1 a 54 par code, etudiant 55 ajoute par le formateur (Q14)
INSERT INTO presence (session_id, etudiant_id, source, marquee_at)
SELECT 1, g, 'ETUDIANT', now() - INTERVAL '14 days' + g * INTERVAL '5 seconds'
FROM generate_series(1, 54) AS g;
INSERT INTO presence (session_id, etudiant_id, source, marquee_at)
VALUES (1, 55, 'FORMATEUR', now() - INTERVAL '14 days' + INTERVAL '30 minutes');

-- Seance 2 : etudiants 1 a 50
INSERT INTO presence (session_id, etudiant_id, source, marquee_at)
SELECT 2, g, 'ETUDIANT', now() - INTERVAL '1 day' + g * INTERVAL '5 seconds'
FROM generate_series(1, 50) AS g;

-- Seance 1 : exercices des etudiants 1 a 50 ; relus pour 1 a 45, en attente pour 46 a 50
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_at)
SELECT 1, g,
       'https://github.com/demo-kf48/etudiant-' || g || '/seance-1',
       CASE WHEN g <= 45 THEN 'RELU' ELSE 'EN_ATTENTE_RELECTURE' END,
       now() - INTERVAL '14 days' + INTERVAL '1 hour'
FROM generate_series(1, 50) AS g;

-- Relecteur de l'exercice de i : i + 1 (50 -> 1), jamais l'auteur (RG2), un seul par exercice (RG12)
INSERT INTO relecture (exercice_id, relecteur_id, note, commentaire, attribuee_at, rendue_at)
SELECT e.id,
       (e.etudiant_id % 50) + 1,
       CASE WHEN e.etudiant_id <= 45 THEN ((e.etudiant_id * 7) % 11) + 10 END,
       CASE WHEN e.etudiant_id <= 45 THEN 'Code lisible, historique Git propre. Pensez a documenter le README.' END,
       e.depose_at,
       CASE WHEN e.etudiant_id <= 45 THEN e.depose_at + INTERVAL '1 day' END
FROM exercice e
WHERE e.session_id = 1;

-- Seance 2 : exercices des etudiants 1 a 40 ; relus pour 1 a 25
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_at)
SELECT 2, g,
       'https://github.com/demo-kf48/etudiant-' || g || '/seance-2',
       CASE WHEN g <= 25 THEN 'RELU' ELSE 'EN_ATTENTE_RELECTURE' END,
       now() - INTERVAL '1 day' + INTERVAL '1 hour'
FROM generate_series(1, 40) AS g;

-- Relecteur de l'exercice de i : i + 2 (39 -> 1, 40 -> 2)
INSERT INTO relecture (exercice_id, relecteur_id, note, commentaire, attribuee_at, rendue_at)
SELECT e.id,
       ((e.etudiant_id + 1) % 40) + 1,
    CASE WHEN e.etudiant_id <= 25 THEN ((e.etudiant_id * 5) % 13) + 8 END,
       CASE WHEN e.etudiant_id <= 25 THEN 'Les couches sont bien separees. Ajoutez un test sur le cas d''erreur.' END,
       e.depose_at,
       CASE WHEN e.etudiant_id <= 25 THEN e.depose_at + INTERVAL '2 hours' END
FROM exercice e
WHERE e.session_id = 2;

-- Promotion B : seule Ndiaye Awa est presente, son exercice attend un relecteur (RG14)
INSERT INTO presence (session_id, etudiant_id, source, marquee_at)
VALUES (3, 61, 'ETUDIANT', now() - INTERVAL '2 hours' + INTERVAL '1 minute');
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_at)
VALUES (3, 61, 'https://github.com/demo-kf48/ndiaye-awa/demo', 'EN_ATTENTE_ATTRIBUTION',
        now() - INTERVAL '2 hours' + INTERVAL '10 minutes');

-- Les identifiants explicites ci-dessus imposent de recaler les sequences
SELECT setval(pg_get_serial_sequence('promotion', 'id'), (SELECT MAX(id) FROM promotion));
SELECT setval(pg_get_serial_sequence('etudiant', 'id'), (SELECT MAX(id) FROM etudiant));
SELECT setval(pg_get_serial_sequence('session_cours', 'id'), (SELECT MAX(id) FROM session_cours));