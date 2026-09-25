package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.Etudiant;

public record EtudiantResumeDto(Long id, String nom) {

    public static EtudiantResumeDto depuis(Etudiant etudiant) {
        return new EtudiantResumeDto(etudiant.getId(), etudiant.getNom());
    }
}