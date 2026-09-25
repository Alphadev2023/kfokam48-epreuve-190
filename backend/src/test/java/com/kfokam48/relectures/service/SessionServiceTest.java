package com.kfokam48.relectures.service;

import com.kfokam48.relectures.domain.Promotion;
import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.dto.OuvrirSessionRequete;
import com.kfokam48.relectures.dto.SessionOuverteDto;
import com.kfokam48.relectures.exception.MetierException;
import com.kfokam48.relectures.repository.PromotionRepository;
import com.kfokam48.relectures.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionServiceTest {

    private static final Instant MAINTENANT = Instant.parse("2026-09-25T08:00:00Z");

    private PromotionRepository promotionRepository;
    private SessionRepository sessionRepository;
    private GenerateurCode generateurCode;
    private SessionService service;

    @BeforeEach
    void preparer() {
        promotionRepository = mock(PromotionRepository.class);
        sessionRepository = mock(SessionRepository.class);
        generateurCode = mock(GenerateurCode.class);
        Clock horlogeFixe = Clock.fixed(MAINTENANT, ZoneOffset.UTC);
        service = new SessionService(promotionRepository, sessionRepository, generateurCode, horlogeFixe);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(new Promotion("Promo test")));
        when(sessionRepository.save(any(SessionCours.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    @Test
    void rg1_leCodeExpireQuinzeMinutesApresLOuverture() {
        when(generateurCode.generer()).thenReturn("ABCDEF");

        SessionOuverteDto session = service.ouvrir(new OuvrirSessionRequete("Seance Git", 1L));

        assertThat(session.ouvertureAt()).isEqualTo(MAINTENANT);
        assertThat(Duration.between(session.ouvertureAt(), session.expirationAt()))
                .isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void rg19_unCodeDejaUtiliseEstRegenere() {
        when(generateurCode.generer()).thenReturn("DEJAPR", "NOUVEA");
        when(sessionRepository.existsByCode("DEJAPR")).thenReturn(true);
        when(sessionRepository.existsByCode("NOUVEA")).thenReturn(false);

        SessionOuverteDto session = service.ouvrir(new OuvrirSessionRequete("Seance Git", 1L));

        assertThat(session.code()).isEqualTo("NOUVEA");
    }

    @Test
    void unePromotionInconnueEstRefuseeAvecLeCodeDuContrat() {
        when(promotionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ouvrir(new OuvrirSessionRequete("Seance", 99L)))
                .isInstanceOf(MetierException.class)
                .extracting("code")
                .isEqualTo("PROMOTION_INCONNUE");
    }

    @Test
    void rg19_leGenerateurNUtiliseQueDesCaracteresNonAmbigus() {
        GenerateurCode vraiGenerateur = new GenerateurCode();
        for (int i = 0; i < 500; i++) {
            assertThat(vraiGenerateur.generer()).matches("^[A-HJ-NP-Z2-9]{6}$");
        }
    }
}