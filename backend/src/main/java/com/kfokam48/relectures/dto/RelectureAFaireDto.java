package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.Relecture;

public record RelectureAFaireDto(Long id, Long exerciceId, String sessionTitre, String lien,
                                 boolean rendue, Integer note, String commentaire) {

    public static RelectureAFaireDto depuis(Relecture relecture) {
        return new RelectureAFaireDto(relecture.getId(), relecture.getExercice().getId(),
                relecture.getExercice().getSession().getTitre(), relecture.getExercice().getLien(),
                relecture.estRendue(), relecture.getNote(), relecture.getCommentaire());
    }
}