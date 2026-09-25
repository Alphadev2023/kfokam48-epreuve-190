package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.dto.ExerciceEnAttenteDto;
import com.kfokam48.relectures.dto.LigneTableauDto;
import com.kfokam48.relectures.service.TableauService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tableau")
public class TableauController {

    private final TableauService tableauService;

    public TableauController(TableauService tableauService) {
        this.tableauService = tableauService;
    }

    /** [IMPOSE] EF7 : 200 recapitulatif par etudiant, 404 PROMOTION_INCONNUE. */
    @GetMapping
    public List<LigneTableauDto> tableau(@RequestParam Long promotionId) {
        return tableauService.tableau(promotionId);
    }

    /** [AJOUT] EF8 (Q11) : exercices encore en attente, les plus anciens d'abord. */
    @GetMapping("/exercices-en-attente")
    public List<ExerciceEnAttenteDto> exercicesEnAttente(@RequestParam Long promotionId) {
        return tableauService.exercicesEnAttente(promotionId);
    }
}