package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.domain.Etudiant;
import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.Promotion;
import com.kfokam48.relectures.domain.Relecture;
import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.ExerciceRepository;
import com.kfokam48.relectures.repository.PromotionRepository;
import com.kfokam48.relectures.repository.RelectureRepository;
import com.kfokam48.relectures.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExercicesEnAttenteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ExerciceRepository exerciceRepository;

    @Autowired
    private RelectureRepository relectureRepository;

    private Promotion promo;

    /**
     * Exercice d'Alpha : relu par Beta (rendue) et Gamma (en attente), EN_ATTENTE_RELECTURE.
     * Exercice de Beta : aucun relecteur, EN_ATTENTE_ATTRIBUTION.
     * Exercice de Gamma : deux relectures rendues, RELU, absent de la liste.
     */
    @BeforeEach
    void preparer() {
        promo = promotionRepository.save(new Promotion("Promo suivi"));
        Etudiant alpha = etudiantRepository.save(new Etudiant("Alpha Aminata", promo));
        Etudiant beta = etudiantRepository.save(new Etudiant("Beta Brice", promo));
        Etudiant gamma = etudiantRepository.save(new Etudiant("Gamma Carine", promo));

        Instant t = Instant.now();
        SessionCours s = sessionRepository.save(SessionCours.ouvrir("Seance suivi", promo, "SUIVI2", t, Duration.ofMinutes(15)));

        Exercice exAlpha = exerciceRepository.save(Exercice.deposer(s, alpha, "https://x.cm/alpha", t));
        Exercice exBeta = exerciceRepository.save(Exercice.deposer(s, beta, "https://x.cm/beta", t.plusSeconds(1)));
        Exercice exGamma = exerciceRepository.save(Exercice.deposer(s, gamma, "https://x.cm/gamma", t.plusSeconds(2)));

        relectureRepository.save(Relecture.attribuer(exAlpha, beta, t)).rendre(14, "ok", t);
        relectureRepository.save(Relecture.attribuer(exAlpha, gamma, t.plusSeconds(1)));
        exAlpha.relecteursAttribues(2);

        relectureRepository.save(Relecture.attribuer(exGamma, alpha, t)).rendre(12, "ok", t);
        relectureRepository.save(Relecture.attribuer(exGamma, beta, t)).rendre(16, "ok", t);
        exGamma.relecteursAttribues(2);
        exGamma.relecturesRendues(2);
    }

    @Test
    void q11_lesExercicesNonRelusApparaissentAvecLEtatDeChaqueRelecteur() throws Exception {
        mockMvc.perform(get("/api/tableau/exercices-en-attente").param("promotionId", promo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].auteurNom").value("Alpha Aminata"))
                .andExpect(jsonPath("$[0].statut").value("EN_ATTENTE_RELECTURE"))
                .andExpect(jsonPath("$[0].relecteursAttendus").value(2))
                .andExpect(jsonPath("$[0].relecteurs[0].nom").value("Beta Brice"))
                .andExpect(jsonPath("$[0].relecteurs[0].rendue").value(true))
                .andExpect(jsonPath("$[0].relecteurs[1].nom").value("Gamma Carine"))
                .andExpect(jsonPath("$[0].relecteurs[1].rendue").value(false))

                .andExpect(jsonPath("$[1].auteurNom").value("Beta Brice"))
                .andExpect(jsonPath("$[1].statut").value("EN_ATTENTE_ATTRIBUTION"))
                .andExpect(jsonPath("$[1].relecteurs.length()").value(0));
    }

    @Test
    void promotionInconnue_404() throws Exception {
        mockMvc.perform(get("/api/tableau/exercices-en-attente").param("promotionId", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}