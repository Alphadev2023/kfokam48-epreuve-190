package com.kfokam48.relectures.repository;

import com.kfokam48.relectures.domain.Relecture;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    List<Relecture> findByExerciceId(Long exerciceId);

    List<Relecture> findByRelecteurId(Long relecteurId);

    List<Relecture> findByExerciceIdIn(Collection<Long> exerciceIds);

    long countByExerciceIdAndRendueAtIsNotNull(Long exerciceId);

    /** RG12 : relecteurs deja attribues a l'exercice, a exclure du tirage suivant. */
    @Query("select r.relecteur.id from Relecture r where r.exercice.id = :exerciceId")
    List<Long> findRelecteurIdsByExerciceId(@Param("exerciceId") Long exerciceId);

    /** ENF8 : un double envoi de la meme relecture est traite l'un apres l'autre. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Relecture r where r.id = :id")
    Optional<Relecture> findByIdAvecVerrou(@Param("id") Long id);

    /** RG13 : nombre de relectures attribuees a chaque relecteur dans la session. */
    @Query("select r.relecteur.id, count(r) from Relecture r "
            + "where r.exercice.session.id = :sessionId group by r.relecteur.id")
    List<Object[]> compterParRelecteurDansSession(@Param("sessionId") Long sessionId);

    /**
     * RG17, RG21 : pour chaque exercice note de la promotion, auteur, exercice, relecteurs attendus,
     * moyenne des notes rendues et nombre de relectures rendues. Une seule requete pour tout le tableau (ENF2).
     */
    @Query("select e.auteur.id, e.id, e.relecteursAttendus, avg(r.note), count(r) "
            + "from Relecture r join r.exercice e "
            + "where e.auteur.promotion.id = :promotionId and r.rendueAt is not null "
            + "group by e.auteur.id, e.id, e.relecteursAttendus")
    List<Object[]> notesRenduesParExerciceDeLaPromotion(@Param("promotionId") Long promotionId);

    /** EF7 : relectures attribuees et pas encore rendues, par relecteur. */
    @Query("select r.relecteur.id, count(r) from Relecture r "
            + "where r.relecteur.promotion.id = :promotionId and r.rendueAt is null "
            + "group by r.relecteur.id")
    List<Object[]> compterEnAttenteParRelecteurDeLaPromotion(@Param("promotionId") Long promotionId);
}