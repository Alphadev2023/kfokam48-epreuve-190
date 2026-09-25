package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.StatutExercice;

public record ExerciceRecuDto(Long id, Long sessionId, String sessionTitre, String lien,
                              StatutExercice statut, Integer note, String commentaire) {

    public static ExerciceRecuDto depuis(Exercice exercice) {
        return new ExerciceRecuDto(exercice.getId(), exercice.getSession().getId(),
                exercice.getSession().getTitre(), exercice.getLien(), exercice.getStatut(), null, null);
    }
}