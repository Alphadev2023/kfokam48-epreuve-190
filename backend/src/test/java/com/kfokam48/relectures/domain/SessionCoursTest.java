package com.kfokam48.relectures.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SessionCoursTest {

    private static final Instant OUVERTURE = Instant.parse("2026-09-25T08:00:00Z");

    private final SessionCours session = SessionCours.ouvrir("Seance", new Promotion("P"), "ABCDEF",
            OUVERTURE, Duration.ofMinutes(15));

    @Test
    void rg1_leCodeEstValideAvantQuinzeMinutes() {
        assertThat(session.codeValideA(OUVERTURE.plus(Duration.ofMinutes(14)).plusSeconds(59))).isTrue();
    }

    @Test
    void rg1_leCodeExpireAQuinzeMinutesPile() {
        assertThat(session.codeValideA(OUVERTURE.plus(Duration.ofMinutes(15)))).isFalse();
    }

    @Test
    void rg1_leCodeResteExpireEnsuite() {
        assertThat(session.codeValideA(OUVERTURE.plus(Duration.ofHours(2)))).isFalse();
    }
}