package com.kfokam48.relectures.repository;

import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.StatutExercice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndAuteurId(Long sessionId, Long auteurId);

    List<Exercice> findByAuteurIdOrderByDeposeAtDesc(Long auteurId);

    List<Exercice> findBySessionIdAndStatut(Long sessionId, StatutExercice statut);

    /** EF7 : nombre d'exercices deposes par etudiant de la promotion. */
    @Query("select e.auteur.id, count(e) from Exercice e "
            + "where e.auteur.promotion.id = :promotionId group by e.auteur.id")
    List<Object[]> compterParAuteurDeLaPromotion(@Param("promotionId") Long promotionId);
}