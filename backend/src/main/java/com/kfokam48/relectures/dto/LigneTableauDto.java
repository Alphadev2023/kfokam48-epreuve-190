package com.kfokam48.relectures.dto;

public record LigneTableauDto(Long etudiantId, String nom, int presences, int exercicesDeposes,
                              Double moyenne, boolean moyenneProvisoire, int relecturesEnAttente) {
}