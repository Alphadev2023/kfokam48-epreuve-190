package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.Presence;
import com.kfokam48.relectures.domain.SourcePresence;

import java.time.Instant;

public record PresenceDetailDto(Long etudiantId, String nom, SourcePresence source, Instant marqueeAt) {

    public static PresenceDetailDto depuis(Presence presence) {
        return new PresenceDetailDto(presence.getEtudiant().getId(), presence.getEtudiant().getNom(),
                presence.getSource(), presence.getMarqueeAt());
    }
}