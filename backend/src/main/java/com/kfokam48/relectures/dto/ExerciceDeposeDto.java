package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.StatutExercice;

public record ExerciceDeposeDto(Long id, StatutExercice statut) {

    public static ExerciceDeposeDto depuis(Exercice exercice) {
        return new ExerciceDeposeDto(exercice.getId(), exercice.getStatut());
    }
}