package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.dto.ExerciceDeposeDto;
import com.kfokam48.relectures.dto.ExerciceRequete;
import com.kfokam48.relectures.dto.RemplacerLienRequete;
import com.kfokam48.relectures.service.ExerciceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ExerciceService exerciceService;

    public ExerciceController(ExerciceService exerciceService) {
        this.exerciceService = exerciceService;
    }

    /** [IMPOSE] EF4 : 201 { id, statut }, 400 LIEN_INVALIDE, 409 EXERCICE_DEJA_DEPOSE. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciceDeposeDto deposer(@Valid @RequestBody ExerciceRequete requete) {
        return exerciceService.deposer(requete);
    }

    /** [AJOUT] EF11 : l'auteur, identifie par X-Etudiant-Id (Q1), remplace son lien. */
    @PutMapping("/{id}")
    public ExerciceDeposeDto remplacerLien(@PathVariable Long id,
                                           @Valid @RequestBody RemplacerLienRequete requete,
                                           @RequestHeader("X-Etudiant-Id") Long appelantId) {
        return exerciceService.remplacerLien(id, requete.lien(), appelantId);
    }
}