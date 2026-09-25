package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.Presence;
import com.kfokam48.relectures.domain.SourcePresence;

public record PresenceDto(Long id, Long sessionId, Long etudiantId, SourcePresence source) {

    public static PresenceDto depuis(Presence presence) {
        return new PresenceDto(presence.getId(), presence.getSession().getId(),
                presence.getEtudiant().getId(), presence.getSource());
    }
}