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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SessionClotureIntegrationTest {

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

    private Etudiant relecteur;
    private Etudiant retardataire;
    private SessionCours session;
    private Relecture relecture;

    /** Le code est encore valide : sans cloture, un marquage serait accepte. */
    @BeforeEach
    void preparer() {
        Promotion promo = promotionRepository.save(new Promotion("Promo cloture"));
        Etudiant auteur = etudiantRepository.save(new Etudiant("Auteur Aminata", promo));
        relecteur = etudiantRepository.save(new Etudiant("Relecteur Brice", promo));
        retardataire = etudiantRepository.save(new Etudiant("Retard Carine", promo));

        Instant maintenant = Instant.now();
        session = sessionRepository.save(SessionCours.ouvrir("Seance a cloturer", promo, "CLOTU2",
                maintenant, Duration.ofMinutes(15)));
        presenceRepository.save(Presence.parEtudiant(session, auteur, maintenant));
        presenceRepository.save(Presence.parEtudiant(session, relecteur, maintenant));
        Exercice exercice = exerciceRepository.save(
                Exercice.deposer(session, auteur, "https://github.com/auteur/tp", maintenant));
        relecture = relectureRepository.save(Relecture.attribuer(exercice, relecteur, maintenant));
    }

    private ResultActions cloturer(Long sessionId) throws Exception {
        return mockMvc.perform(post("/api/sessions/{id}/cloture", sessionId));
    }

    @Test
    void cloture_200_statutCloturee() throws Exception {
        cloturer(session.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("CLOTUREE"));
    }

    @Test
    void secondeCloture_409() throws Exception {
        cloturer(session.getId()).andExpect(status().isOk());
        cloturer(session.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_DEJA_CLOTUREE"));
    }

    @Test
    void sessionInconnue_404() throws Exception {
        cloturer(999999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }

    @Test
    void rg4_apresCloture_marquageParCode_410() throws Exception {
        cloturer(session.getId()).andExpect(status().isOk());

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"CLOTU2\",\"etudiantId\":" + retardataire.getId() + "}"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
    }

    @Test
    void h11_apresCloture_presenceManuelle_409() throws Exception {
        cloturer(session.getId()).andExpect(status().isOk());

        mockMvc.perform(post("/api/sessions/{id}/presences", session.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + retardataire.getId() + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    void rg10_apresCloture_depot_409() throws Exception {
        cloturer(session.getId()).andExpect(status().isOk());

        mockMvc.perform(post("/api/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + session.getId() + ",\"etudiantId\":" + relecteur.getId()
                                + ",\"lien\":\"https://github.com/relecteur/tp\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    void rg20_apresCloture_uneRelectureAttribueePeutEncoreEtreRendue() throws Exception {
        cloturer(session.getId()).andExpect(status().isOk());

        mockMvc.perform(post("/api/relectures/{id}", relecture.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Etudiant-Id", relecteur.getId())
                        .content("{\"note\":14,\"commentaire\":\"Rendu apres la cloture.\"}"))
                .andExpect(status().isOk());
    }
}