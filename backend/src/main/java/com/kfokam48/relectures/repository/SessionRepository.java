package com.kfokam48.relectures.repository;

import com.kfokam48.relectures.domain.SessionCours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<SessionCours, Long> {

    boolean existsByCode(String code);

    List<SessionCours> findByPromotionIdOrderByOuvertureAtDesc(Long promotionId);
}