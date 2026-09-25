package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.domain.Etudiant;
import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.Presence;
import com.kfokam48.relectures.domain.Promotion;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PresenceManuelleIntegrationTest {

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

    private Etudiant auteur;
    private Etudiant oublie;
    private Etudiant voisin;
    private SessionCours sessionCodeExpire;

    @BeforeEach
    void preparer() {
        Promotion promo = promotionRepository.save(new Promotion("Promo manuelle"));
        Promotion autre = promotionRepository.save(new Promotion("Promo voisine manuelle"));
        auteur = etudiantRepository.save(new Etudiant("Auteur Aminata", promo));
        oublie = etudiantRepository.save(new Etudiant("Telephone Brice", promo));
        voisin = etudiantRepository.save(new Etudiant("Voisin Carine", autre));

        // Le code a expire il y a longtemps : l'etudiant ne peut plus pointer lui-meme
        sessionCodeExpire = sessionRepository.save(SessionCours.ouvrir("Seance passee", promo, "MANUE2",
                Instant.now().minus(Duration.ofHours(2)), Duration.ofMinutes(15)));
    }

    private ResultActions ajouter(Long sessionId, Long etudiantId) throws Exception {
        return mockMvc.perform(post("/api/sessions/{id}/presences", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"etudiantId\":" + etudiantId + "}"));
    }

    @Test
    void rg7_apresExpirationDuCode_201AvecSourceFormateurVisibleDansLaListe() throws Exception {
        ajouter(sessionCodeExpire.getId(), oublie.getId())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("FORMATEUR"));

        mockMvc.perform(get("/api/sessions/{id}/presences", sessionCodeExpire.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Telephone Brice"))
                .andExpect(jsonPath("$[0].source").value("FORMATEUR"));
    }

    @Test
    void rg5_dejaPresent_409() throws Exception {
        ajouter(sessionCodeExpire.getId(), oublie.getId()).andExpect(status().isCreated());
        ajouter(sessionCodeExpire.getId(), oublie.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }

    @Test
    void rg6_etudiantDUneAutrePromotion_400() throws Exception {
        ajouter(sessionCodeExpire.getId(), voisin.getId())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ETUDIANT_HORS_PROMOTION"));
    }

    @Test
    void sessionInconnue_404() throws Exception {
        ajouter(999999L, oublie.getId())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }

    @Test
    void rg14_lAjoutManuelDeclencheLeTirageDesRelecteursManquants() throws Exception {
        presenceRepository.save(Presence.parEtudiant(sessionCodeExpire, auteur, Instant.now()));
        Exercice exercice = exerciceRepository.save(
                Exercice.deposer(sessionCodeExpire, auteur, "https://x.cm/tp", Instant.now()));

        ajouter(sessionCodeExpire.getId(), oublie.getId()).andExpect(status().isCreated());

        assertThat(relectureRepository.findByExerciceId(exercice.getId()))
                .extracting(r -> r.getRelecteur().getId())
                .containsExactly(oublie.getId());
    }
}