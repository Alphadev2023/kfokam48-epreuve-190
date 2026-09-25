package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.domain.Etudiant;
import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.Presence;
import com.kfokam48.relectures.domain.Promotion;
import com.kfokam48.relectures.domain.Relecture;
import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.ExerciceRepository;
import com.kfokam48.relectures.repository.PresenceRepository;
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

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TableauIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PresenceRepository presenceRepository;

    @Autowired
    private ExerciceRepository exerciceRepository;

    @Autowired
    private RelectureRepository relectureRepository;

    private Promotion promo;

    /**
     * Alpha : 2 presences, 2 exercices notes 15 et 16 (moyenne 15.5), 1 relecture a rendre.
     * Beta  : 2 presences, 1 exercice pas encore relu (moyenne null), 0 relecture a rendre.
     * Gamma : rien.
     */
    @BeforeEach
    void preparer() {
        promo = promotionRepository.save(new Promotion("Promo tableau"));
        Etudiant alpha = etudiantRepository.save(new Etudiant("Alpha Aminata", promo));
        Etudiant beta = etudiantRepository.save(new Etudiant("Beta Brice", promo));
        etudiantRepository.save(new Etudiant("Gamma Carine", promo));

        Instant t = Instant.now();
        SessionCours s1 = sessionRepository.save(SessionCours.ouvrir("S1", promo, "TABLO2", t, Duration.ofMinutes(15)));
        SessionCours s2 = sessionRepository.save(SessionCours.ouvrir("S2", promo, "TABLO3", t, Duration.ofMinutes(15)));
        for (SessionCours s : new SessionCours[]{s1, s2}) {
            presenceRepository.save(Presence.parEtudiant(s, alpha, t));
            presenceRepository.save(Presence.parEtudiant(s, beta, t));
        }

        Exercice alpha1 = exerciceRepository.save(Exercice.deposer(s1, alpha, "https://x.cm/a1", t));
        Exercice alpha2 = exerciceRepository.save(Exercice.deposer(s2, alpha, "https://x.cm/a2", t));
        Exercice beta1 = exerciceRepository.save(Exercice.deposer(s1, beta, "https://x.cm/b1", t));

        Relecture r1 = relectureRepository.save(Relecture.attribuer(alpha1, beta, t));
        r1.rendre(15, "ok", t);
        Relecture r2 = relectureRepository.save(Relecture.attribuer(alpha2, beta, t));
        r2.rendre(16, "ok", t);
        relectureRepository.save(Relecture.attribuer(beta1, alpha, t));
    }

    @Test
    void leTableauAgregePresencesExercicesMoyenneEtRelecturesEnAttente() throws Exception {
        mockMvc.perform(get("/api/tableau").param("promotionId", promo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))

                .andExpect(jsonPath("$[0].nom").value("Alpha Aminata"))
                .andExpect(jsonPath("$[0].presences").value(2))
                .andExpect(jsonPath("$[0].exercicesDeposes").value(2))
                .andExpect(jsonPath("$[0].moyenne").value(15.5))
                .andExpect(jsonPath("$[0].relecturesEnAttente").value(1))

                .andExpect(jsonPath("$[1].nom").value("Beta Brice"))
                .andExpect(jsonPath("$[1].exercicesDeposes").value(1))
                .andExpect(jsonPath("$[1]", hasKey("moyenne")))
                .andExpect(jsonPath("$[1].moyenne").value(nullValue()))
                .andExpect(jsonPath("$[1].relecturesEnAttente").value(0))

                .andExpect(jsonPath("$[2].nom").value("Gamma Carine"))
                .andExpect(jsonPath("$[2].presences").value(0));
    }

    @Test
    void promotionInconnue_404() throws Exception {
        mockMvc.perform(get("/api/tableau").param("promotionId", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}