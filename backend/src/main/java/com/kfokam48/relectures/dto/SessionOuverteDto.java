package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.SessionCours;

import java.time.Instant;

public record SessionOuverteDto(Long id, String code, Instant ouvertureAt, Instant expirationAt) {

    public static SessionOuverteDto depuis(SessionCours session) {
        return new SessionOuverteDto(session.getId(), session.getCode(),
                session.getOuvertureAt(), session.getExpirationAt());
    }
}