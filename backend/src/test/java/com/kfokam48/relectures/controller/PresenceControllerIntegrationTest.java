package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.domain.Etudiant;
import com.kfokam48.relectures.domain.Promotion;
import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.repository.EtudiantRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** B6 : chaque branche du diagramme D3 et chaque code HTTP du contrat. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PresenceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private SessionRepository sessionRepository;

    private Etudiant etudiant;
    private Etudiant etudiantAutrePromo;

    @BeforeEach
    void preparer() {
        Promotion promo = promotionRepository.save(new Promotion("Promo presence"));
        Promotion autre = promotionRepository.save(new Promotion("Promo voisine"));
        etudiant = etudiantRepository.save(new Etudiant("Fotso Aminata", promo));
        etudiantAutrePromo = etudiantRepository.save(new Etudiant("Voisin Brice", autre));

        Instant maintenant = Instant.now();
        sessionRepository.save(SessionCours.ouvrir("Seance en cours", promo, "VALID2",
                maintenant, Duration.ofMinutes(15)));
        sessionRepository.save(SessionCours.ouvrir("Seance passee", promo, "EXPIR2",
                maintenant.minus(Duration.ofHours(1)), Duration.ofMinutes(15)));
    }

    private ResultActions marquer(String code, Long etudiantId) throws Exception {
        return mockMvc.perform(post("/api/presences")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"etudiantId\":" + etudiantId + "}"));
    }

    @Test
    void casNominal_201AvecSourceEtudiant() throws Exception {
        marquer("VALID2", etudiant.getId())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sessionId").isNumber())
                .andExpect(jsonPath("$.etudiantId").value(etudiant.getId()))
                .andExpect(jsonPath("$.source").value("ETUDIANT"));
    }

    @Test
    void leCodeEstAccepteEnMinusculesEtAvecDesEspaces() throws Exception {
        marquer("  valid2 ", etudiant.getId()).andExpect(status().isCreated());
    }

    @Test
    void codeInconnu_400() throws Exception {
        marquer("ZZZZZZ", etudiant.getId())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODE_INCONNU"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void codeExpire_410_RG1() throws Exception {
        marquer("EXPIR2", etudiant.getId())
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
    }

    @Test
    void dejaPresent_409_RG5() throws Exception {
        marquer("VALID2", etudiant.getId()).andExpect(status().isCreated());

        marquer("VALID2", etudiant.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }

    @Test
    void etudiantDUneAutrePromotion_400_RG6() throws Exception {
        marquer("VALID2", etudiantAutrePromo.getId())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ETUDIANT_HORS_PROMOTION"));
    }

    @Test
    void codeManquant_400() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + etudiant.getId() + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"));
    }
}