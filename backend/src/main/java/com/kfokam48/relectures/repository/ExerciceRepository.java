package com.kfokam48.relectures.repository;

import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.StatutExercice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndAuteurId(Long sessionId, Long auteurId);

    List<Exercice> findByAuteurIdOrderByDeposeAtDesc(Long auteurId);

    List<Exercice> findBySessionIdAndStatut(Long sessionId, StatutExercice statut);
}