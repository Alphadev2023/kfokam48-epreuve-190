package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.dto.PresenceDto;
import com.kfokam48.relectures.dto.PresenceRequete;
import com.kfokam48.relectures.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    /** [IMPOSE] EF3 : 201, 400 CODE_INCONNU, 409 DEJA_PRESENT, 410 CODE_EXPIRE. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PresenceDto marquer(@Valid @RequestBody PresenceRequete requete) {
        return presenceService.marquer(requete);
    }
}