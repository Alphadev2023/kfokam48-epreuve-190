package com.kfokam48.relectures.dto;

import jakarta.validation.constraints.NotBlank;

public record RemplacerLienRequete(
        @NotBlank(message = "CHAMP_MANQUANT")
        String lien
) {
}