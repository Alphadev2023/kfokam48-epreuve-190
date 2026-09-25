package com.kfokam48.relectures.repository;

import com.kfokam48.relectures.domain.SessionCours;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionCours, Long> {

    boolean existsByCode(String code);

    List<SessionCours> findByPromotionIdOrderByOuvertureAtDesc(Long promotionId);

    Optional<SessionCours> findByCode(String code);

    /** ENF8 : verrou pessimiste, les operations qui modifient une meme session sont traitees l'une apres l'autre. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SessionCours s where s.code = :code")
    Optional<SessionCours> findByCodeAvecVerrou(@Param("code") String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SessionCours s where s.id = :id")
    Optional<SessionCours> findByIdAvecVerrou(@Param("id") Long id);
}