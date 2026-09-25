package com.kfokam48.relectures.repository;

import com.kfokam48.relectures.domain.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    Optional<Relecture> findByExerciceId(Long exerciceId);

    /** RG13 : nombre de relectures attribuees a chaque relecteur dans la session. */
    @Query("select r.relecteur.id, count(r) from Relecture r "
            + "where r.exercice.session.id = :sessionId group by r.relecteur.id")
    List<Object[]> compterParRelecteurDansSession(@Param("sessionId") Long sessionId);
}