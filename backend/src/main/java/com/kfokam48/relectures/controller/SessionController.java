package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.dto.OuvrirSessionRequete;
import com.kfokam48.relectures.dto.SessionDto;
import com.kfokam48.relectures.dto.SessionOuverteDto;
import com.kfokam48.relectures.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /** [IMPOSE] EF1 : 201 { id, code, ouvertureAt, expirationAt }, 400 sinon. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionOuverteDto ouvrir(@Valid @RequestBody OuvrirSessionRequete requete) {
        return sessionService.ouvrir(requete);
    }

    /** [AJOUT] Sessions d'une promotion, la plus recente d'abord. */
    @GetMapping
    public List<SessionDto> lister(@RequestParam Long promotionId) {
        return sessionService.listerParPromotion(promotionId);
    }
}