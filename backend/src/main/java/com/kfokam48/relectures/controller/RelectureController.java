package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.dto.RelectureRequete;
import com.kfokam48.relectures.service.RelectureService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

    private final RelectureService relectureService;

    public RelectureController(RelectureService relectureService) {
        this.relectureService = relectureService;
    }

    /** [IMPOSE] EF6 : 200, 400 NOTE_INVALIDE, 403 AUTO_RELECTURE, 409 RELECTURE_DEJA_RENDUE. */
    @PostMapping("/{id}")
    public void rendre(@PathVariable Long id,
                       @Valid @RequestBody RelectureRequete requete,
                       @RequestHeader(name = "X-Etudiant-Id", required = false) Long appelantId) {
        relectureService.rendre(id, requete, appelantId);
    }
}