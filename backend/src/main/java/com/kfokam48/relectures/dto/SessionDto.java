package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.domain.StatutSession;

import java.time.Instant;

public record SessionDto(Long id, String titre, Long promotionId, String code,
                         Instant ouvertureAt, Instant expirationAt, StatutSession statut) {

    public static SessionDto depuis(SessionCours session) {
        return new SessionDto(session.getId(), session.getTitre(), session.getPromotion().getId(),
                session.getCode(), session.getOuvertureAt(), session.getExpirationAt(), session.getStatut());
    }
}