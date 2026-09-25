package com.kfokam48.relectures.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** RG3 : note entiere de 0 a 20. Une valeur decimale est rejetee par Jackson (accept-float-as-int=false). */
public record RelectureRequete(
        @NotNull(message = "NOTE_INVALIDE")
        @Min(value = 0, message = "NOTE_INVALIDE")
        @Max(value = 20, message = "NOTE_INVALIDE")
        Integer note,

        @NotBlank(message = "CHAMP_MANQUANT")
        @Size(max = 2000, message = "COMMENTAIRE_TROP_LONG")
        String commentaire
) {
}