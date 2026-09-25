package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.StatutExercice;

import java.util.List;

/** EF8 (Q11) : vue formateur uniquement. */
public record ExerciceEnAttenteDto(Long exerciceId, Long sessionId, String sessionTitre,
                                   Long auteurId, String auteurNom, StatutExercice statut,
                                   int relecteursAttendus, List<RelecteurSuiviDto> relecteurs) {
}