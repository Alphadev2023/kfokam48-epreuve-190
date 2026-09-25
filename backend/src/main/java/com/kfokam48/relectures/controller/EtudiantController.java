package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.dto.ExerciceRecuDto;
import com.kfokam48.relectures.dto.RelectureAFaireDto;
import com.kfokam48.relectures.service.ExerciceService;
import com.kfokam48.relectures.service.RelectureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final ExerciceService exerciceService;
    private final RelectureService relectureService;

    public EtudiantController(ExerciceService exerciceService, RelectureService relectureService) {
        this.exerciceService = exerciceService;
        this.relectureService = relectureService;
    }

    /** [AJOUT] Exercices d'un etudiant, sans le nom du relecteur (Q8, RG18). */
    @GetMapping("/{id}/exercices")
    public List<ExerciceRecuDto> exercices(@PathVariable Long id) {
        return exerciceService.exercicesDe(id);
    }

    /** [AJOUT] Relectures attribuees a un etudiant (ecran relecteur). */
    @GetMapping("/{id}/relectures")
    public List<RelectureAFaireDto> relectures(@PathVariable Long id) {
        return relectureService.relecturesDe(id);
    }
}