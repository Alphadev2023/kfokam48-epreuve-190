package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.Relecture;
import com.kfokam48.relectures.domain.StatutExercice;

import java.util.List;

/** RG18 (Q8) : jamais le nom d'un relecteur. */
public record ExerciceRecuDto(Long id, Long sessionId, String sessionTitre, String lien,
                              StatutExercice statut, Integer note, String commentaire) {

    /** Transitoire : l'evolution 2 remplace note et commentaire par la note retenue et les commentaires (RG21). */
    public static ExerciceRecuDto depuis(Exercice exercice, List<Relecture> relectures) {
        Relecture rendue = relectures.stream().filter(Relecture::estRendue).findFirst().orElse(null);
        return new ExerciceRecuDto(exercice.getId(), exercice.getSession().getId(),
                exercice.getSession().getTitre(), exercice.getLien(), exercice.getStatut(),
                rendue == null ? null : rendue.getNote(),
                rendue == null ? null : rendue.getCommentaire());
    }
}