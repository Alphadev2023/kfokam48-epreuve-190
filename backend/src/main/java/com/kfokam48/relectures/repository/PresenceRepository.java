package com.kfokam48.relectures.repository;

import com.kfokam48.relectures.domain.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    /** RG13 : le vivier du tirage, les etudiants presents a la session. */
    @Query("select p.etudiant.id from Presence p where p.session.id = :sessionId")
    List<Long> findEtudiantIdsBySessionId(@Param("sessionId") Long sessionId);

    /** EF7 : nombre de presences par etudiant de la promotion. */
    @Query("select p.etudiant.id, count(p) from Presence p "
            + "where p.etudiant.promotion.id = :promotionId group by p.etudiant.id")
    List<Object[]> compterParEtudiantDeLaPromotion(@Param("promotionId") Long promotionId);

    @Query("select p from Presence p join fetch p.etudiant e where p.session.id = :sessionId order by e.nom")
    List<Presence> findBySessionIdAvecEtudiant(@Param("sessionId") Long sessionId);
}