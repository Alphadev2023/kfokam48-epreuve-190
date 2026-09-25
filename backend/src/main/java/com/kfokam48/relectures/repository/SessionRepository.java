package com.kfokam48.relectures.repository;

import com.kfokam48.relectures.domain.SessionCours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionCours, Long> {

    boolean existsByCode(String code);

    List<SessionCours> findByPromotionIdOrderByOuvertureAtDesc(Long promotionId);

    Optional<SessionCours> findByCode(String code);
}