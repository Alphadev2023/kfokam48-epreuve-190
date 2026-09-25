package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.domain.Etudiant;
import com.kfokam48.relectures.domain.Presence;
import com.kfokam48.relectures.domain.Promotion;
import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.PresenceRepository;
import com.kfokam48.relectures.repository.PromotionRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExerciceControllerIntegrationTest {

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

    private Etudiant present;
    private Etudiant absent;
    private SessionCours sessionEnCours;
    private SessionCours sessionCodeExpire;

    @BeforeEach
    void preparer() {
        Promotion promo = promotionRepository.save(new Promotion("Promo depot"));
        present = etudiantRepository.save(new Etudiant("Kamga Aminata", promo));
        absent = etudiantRepository.save(new Etudiant("Onana Brice", promo));

        Instant maintenant = Instant.now();
        sessionEnCours = sessionRepository.save(SessionCours.ouvrir("En cours", promo, "EXERC2",
                maintenant, Duration.ofMinutes(15)));
        sessionCodeExpire = sessionRepository.save(SessionCours.ouvrir("Code expire, non cloturee", promo, "EXPIR3",
                maintenant.minus(Duration.ofHours(3)), Duration.ofMinutes(15)));

        presenceRepository.save(Presence.parEtudiant(sessionEnCours, present, maintenant));
        presenceRepository.save(Presence.parEtudiant(sessionCodeExpire, present, maintenant.minus(Duration.ofHours(3))));
    }

    private ResultActions deposer(Long sessionId, Long etudiantId, String lien) throws Exception {
        return mockMvc.perform(post("/api/exercices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":" + sessionId + ",\"etudiantId\":" + etudiantId
                        + ",\"lien\":\"" + lien + "\"}"));
    }

    @Test
    void casNominal_201_seulPresentDoncEnAttenteDAttribution() throws Exception {
        deposer(sessionEnCours.getId(), present.getId(), "https://github.com/kamga/tp1")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_ATTRIBUTION"));
    }

    @Test
    void lienInvalide_400_RG11() throws Exception {
        deposer(sessionEnCours.getId(), present.getId(), "ftp://serveur/tp")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
    }

    @Test
    void secondDepot_409_RG8() throws Exception {
        deposer(sessionEnCours.getId(), present.getId(), "https://github.com/kamga/tp1")
                .andExpect(status().isCreated());

        deposer(sessionEnCours.getId(), present.getId(), "https://github.com/kamga/tp1-bis")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EXERCICE_DEJA_DEPOSE"));
    }

    @Test
    void etudiantAbsent_409_RG9() throws Exception {
        deposer(sessionEnCours.getId(), absent.getId(), "https://github.com/onana/tp1")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ETUDIANT_NON_PRESENT"));
    }

    @Test
    void depotAccepteApresExpirationDuCodeTantQueNonCloturee_RG10() throws Exception {
        deposer(sessionCodeExpire.getId(), present.getId(), "https://github.com/kamga/tp-soir")
                .andExpect(status().isCreated());
    }

    @Test
    void sessionInconnue_400() throws Exception {
        deposer(999999L, present.getId(), "https://github.com/kamga/tp1")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }

    @Test
    void lExerciceDeposeApparaitDansLaListeDeLEtudiant() throws Exception {
        deposer(sessionEnCours.getId(), present.getId(), "https://github.com/kamga/tp1")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/etudiants/{id}/exercices", present.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lien").value("https://github.com/kamga/tp1"))
                .andExpect(jsonPath("$[0].sessionTitre").value("En cours"))
                .andExpect(jsonPath("$[0].note").doesNotExist());
    }
}