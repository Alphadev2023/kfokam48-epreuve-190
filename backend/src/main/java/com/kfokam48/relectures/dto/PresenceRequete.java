package com.kfokam48.relectures.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresenceRequete(
        @NotBlank(message = "CHAMP_MANQUANT")
        String code,

        @NotNull(message = "CHAMP_MANQUANT")
        Long etudiantId
) {
}