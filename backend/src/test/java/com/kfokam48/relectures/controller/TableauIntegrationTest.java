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
     * Alpha : exercice a1 relu par Beta (15) et Gamma (17), note retenue 16 definitive ;
     *         exercice a2 relu par Beta seulement (13), note retenue 13 provisoire.
     *         Moyenne = (16 + 13) / 2 = 14.5, provisoire. Doit encore relire b1 : 1 relecture en attente.
     * Beta  : exercice b1 pas encore relu, moyenne null.
     * Gamma : rien depose.
     */
    @BeforeEach
    void preparer() {
        promo = promotionRepository.save(new Promotion("Promo tableau"));
        Etudiant alpha = etudiantRepository.save(new Etudiant("Alpha Aminata", promo));
        Etudiant beta = etudiantRepository.save(new Etudiant("Beta Brice", promo));
        Etudiant gamma = etudiantRepository.save(new Etudiant("Gamma Carine", promo));

        Instant t = Instant.now();
        SessionCours s1 = sessionRepository.save(SessionCours.ouvrir("S1", promo, "TABLO2", t, Duration.ofMinutes(15)));
        SessionCours s2 = sessionRepository.save(SessionCours.ouvrir("S2", promo, "TABLO3", t, Duration.ofMinutes(15)));
        for (SessionCours s : new SessionCours[]{s1, s2}) {
            presenceRepository.save(Presence.parEtudiant(s, alpha, t));
            presenceRepository.save(Presence.parEtudiant(s, beta, t));
        }

        Exercice a1 = exerciceRepository.save(Exercice.deposer(s1, alpha, "https://x.cm/a1", t));
        Exercice a2 = exerciceRepository.save(Exercice.deposer(s2, alpha, "https://x.cm/a2", t));
        Exercice b1 = exerciceRepository.save(Exercice.deposer(s1, beta, "https://x.cm/b1", t));

        relectureRepository.save(Relecture.attribuer(a1, beta, t)).rendre(15, "ok", t);
        relectureRepository.save(Relecture.attribuer(a1, gamma, t)).rendre(17, "ok", t);
        relectureRepository.save(Relecture.attribuer(a2, beta, t)).rendre(13, "ok", t);
        relectureRepository.save(Relecture.attribuer(a2, gamma, t));
        relectureRepository.save(Relecture.attribuer(b1, alpha, t));
    }

    @Test
    void laMoyenneEstCalculeeSurLesNotesRetenuesEtSignaleLeProvisoire() throws Exception {
        mockMvc.perform(get("/api/tableau").param("promotionId", promo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))

                .andExpect(jsonPath("$[0].nom").value("Alpha Aminata"))
                .andExpect(jsonPath("$[0].presences").value(2))
                .andExpect(jsonPath("$[0].exercicesDeposes").value(2))
                .andExpect(jsonPath("$[0].moyenne").value(14.5))
                .andExpect(jsonPath("$[0].moyenneProvisoire").value(true))
                .andExpect(jsonPath("$[0].relecturesEnAttente").value(1))

                .andExpect(jsonPath("$[1].nom").value("Beta Brice"))
                .andExpect(jsonPath("$[1].exercicesDeposes").value(1))
                .andExpect(jsonPath("$[1]", hasKey("moyenne")))
                .andExpect(jsonPath("$[1].moyenne").value(nullValue()))
                .andExpect(jsonPath("$[1].moyenneProvisoire").value(false))
                .andExpect(jsonPath("$[1].relecturesEnAttente").value(0))

                .andExpect(jsonPath("$[2].nom").value("Gamma Carine"))
                .andExpect(jsonPath("$[2].relecturesEnAttente").value(1));
    }

    @Test
    void promotionInconnue_404() throws Exception {
        mockMvc.perform(get("/api/tableau").param("promotionId", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}