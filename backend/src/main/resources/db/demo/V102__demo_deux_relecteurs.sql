-- V102 : demonstration de la regle a deux relecteurs (profil demo uniquement).
-- Seance 3 de la promotion A : 20 presents, 12 exercices a deux relecteurs.
--   etudiants 1 a 4  : deux relectures rendues (note retenue definitive)
--   etudiants 5 a 8  : une seule relecture rendue (note provisoire)
--   etudiants 9 a 12 : aucune relecture rendue
-- La session est retrouvee par son code : aucun identifiant explicite, donc aucun conflit
-- avec les sessions deja creees sur une base existante.

INSERT INTO session_cours (titre, promotion_id, code, ouverture_at, expiration_at, statut, cloture_at)
VALUES ('Seance 3 - Tests (deux relecteurs)', 1, 'HSTDD5',
        now() - INTERVAL '3 hours', now() - INTERVAL '3 hours' + INTERVAL '15 minutes', 'OUVERTE', NULL);

INSERT INTO presence (session_id, etudiant_id, source, marquee_at)
SELECT (SELECT id FROM session_cours WHERE code = 'HSTDD5'), g, 'ETUDIANT',
       now() - INTERVAL '3 hours' + g * INTERVAL '5 seconds'
FROM generate_series(1, 20) AS g;

INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_at, relecteurs_attendus)
SELECT (SELECT id FROM session_cours WHERE code = 'HSTDD5'), g,
       'https://github.com/demo-kf48/etudiant-' || g || '/seance-3',
       CASE WHEN g <= 4 THEN 'RELU' ELSE 'EN_ATTENTE_RELECTURE' END,
       now() - INTERVAL '2 hours', 2
FROM generate_series(1, 12) AS g;

-- Premier relecteur : l'etudiant i + 1 ; rendu pour les exercices des etudiants 1 a 8
INSERT INTO relecture (exercice_id, relecteur_id, note, commentaire, attribuee_at, rendue_at)
SELECT e.id,
       (e.etudiant_id % 20) + 1,
       CASE WHEN e.etudiant_id <= 8 THEN ((e.etudiant_id * 3) % 9) + 11 END,
       CASE WHEN e.etudiant_id <= 8 THEN 'Structure claire, quelques tests manquent.' END,
       e.depose_at,
       CASE WHEN e.etudiant_id <= 8 THEN e.depose_at + INTERVAL '30 minutes' END
FROM exercice e
WHERE e.session_id = (SELECT id FROM session_cours WHERE code = 'HSTDD5');

-- Second relecteur : l'etudiant i + 2 ; rendu pour les exercices des etudiants 1 a 4 seulement
INSERT INTO relecture (exercice_id, relecteur_id, note, commentaire, attribuee_at, rendue_at)
SELECT e.id,
       ((e.etudiant_id + 1) % 20) + 1,
    CASE WHEN e.etudiant_id <= 4 THEN ((e.etudiant_id * 5) % 7) + 12 END,
       CASE WHEN e.etudiant_id <= 4 THEN 'Bonne separation des couches.' END,
       e.depose_at,
       CASE WHEN e.etudiant_id <= 4 THEN e.depose_at + INTERVAL '45 minutes' END
FROM exercice e
WHERE e.session_id = (SELECT id FROM session_cours WHERE code = 'HSTDD5');