package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.dto.OuvrirSessionRequete;
import com.kfokam48.relectures.dto.PresenceDetailDto;
import com.kfokam48.relectures.dto.PresenceDto;
import com.kfokam48.relectures.dto.PresenceManuelleRequete;
import com.kfokam48.relectures.dto.SessionDto;
import com.kfokam48.relectures.dto.SessionOuverteDto;
import com.kfokam48.relectures.service.PresenceService;
import com.kfokam48.relectures.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final PresenceService presenceService;

    public SessionController(SessionService sessionService, PresenceService presenceService) {
        this.sessionService = sessionService;
        this.presenceService = presenceService;
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

    /** [AJOUT] EF9 (Q14) : presence ajoutee par le formateur, source FORMATEUR. */
    @PostMapping("/{id}/presences")
    @ResponseStatus(HttpStatus.CREATED)
    public PresenceDto ajouterPresence(@PathVariable Long id, @Valid @RequestBody PresenceManuelleRequete requete) {
        return presenceService.ajouterParFormateur(id, requete.etudiantId());
    }

    /** [AJOUT] Q14 : presences de la session avec leur source. */
    @GetMapping("/{id}/presences")
    public List<PresenceDetailDto> presences(@PathVariable Long id) {
        return presenceService.presencesDeLaSession(id);
    }
}