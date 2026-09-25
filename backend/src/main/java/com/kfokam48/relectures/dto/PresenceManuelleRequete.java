package com.kfokam48.relectures.dto;

import jakarta.validation.constraints.NotNull;

public record PresenceManuelleRequete(
        @NotNull(message = "CHAMP_MANQUANT")
        Long etudiantId
) {
}