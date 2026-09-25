package com.kfokam48.relectures.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OuvrirSessionRequete(
        @NotBlank(message = "CHAMP_MANQUANT")
        @Size(max = 200, message = "TITRE_TROP_LONG")
        String titre,

        @NotNull(message = "CHAMP_MANQUANT")
        Long promotionId
) {
}