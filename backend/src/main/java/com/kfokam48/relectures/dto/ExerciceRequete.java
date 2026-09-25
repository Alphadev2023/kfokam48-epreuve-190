package com.kfokam48.relectures.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExerciceRequete(
        @NotNull(message = "CHAMP_MANQUANT")
        Long sessionId,

        @NotNull(message = "CHAMP_MANQUANT")
        Long etudiantId,

        @NotBlank(message = "CHAMP_MANQUANT")
        String lien
) {
}