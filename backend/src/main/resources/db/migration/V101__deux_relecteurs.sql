-- V101 : deux relecteurs par exercice (enveloppe, etape 3). Remplace RG12 issue de Q6.
-- Numerotee 101 et non 2 : les bases de demonstration ont deja applique V100, et Flyway
-- refuse d'appliquer une version inferieure a la derniere appliquee (H22, C9).
-- V1 n'est pas modifiee.

-- 1. Nombre de relecteurs attendus. Les exercices deja deposes l'ont ete sous l'ancienne regle :
--    ils gardent un seul relecteur et leur note reste definitive (H18, RG22).
ALTER TABLE exercice ADD COLUMN relecteurs_attendus INTEGER DEFAULT 2 NOT NULL;
UPDATE exercice SET relecteurs_attendus = 1;
ALTER TABLE exercice ADD CONSTRAINT ck_exercice_relecteurs_attendus CHECK (relecteurs_attendus BETWEEN 1 AND 2);

-- 2. Plusieurs relectures par exercice, jamais deux fois le meme relecteur (RG12).
--    La cle etrangere est retiree puis recreee pour que le changement d'unicite passe
--    aussi bien sous PostgreSQL que sous H2 (tests).
ALTER TABLE relecture DROP CONSTRAINT fk_relecture_exercice;
ALTER TABLE relecture DROP CONSTRAINT uk_relecture_exercice;
ALTER TABLE relecture ADD CONSTRAINT uk_relecture_exercice_relecteur UNIQUE (exercice_id, relecteur_id);
ALTER TABLE relecture ADD CONSTRAINT fk_relecture_exercice FOREIGN KEY (exercice_id) REFERENCES exercice (id);