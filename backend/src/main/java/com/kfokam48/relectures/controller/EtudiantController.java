package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.dto.ExerciceRecuDto;
import com.kfokam48.relectures.service.ExerciceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final ExerciceService exerciceService;

    public EtudiantController(ExerciceService exerciceService) {
        this.exerciceService = exerciceService;
    }

    /** [AJOUT] Exercices d'un etudiant, sans le nom du relecteur (Q8, RG18). */
    @GetMapping("/{id}/exercices")
    public List<ExerciceRecuDto> exercices(@PathVariable Long id) {
        return exerciceService.exercicesDe(id);
    }
}