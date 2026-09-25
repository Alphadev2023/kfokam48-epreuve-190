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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RemplacementLienIntegrationTest {

    private static final String NOUVEAU_LIEN = "https://github.com/auteur/tp-corrige";

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

    private Etudiant auteur;
    private Etudiant relecteur;
    private Etudiant intrus;
    private SessionCours session;
    private Exercice exercice;
    private Relecture relecture;

    @BeforeEach
    void preparer() {
        Promotion promo = promotionRepository.save(new Promotion("Promo remplacement"));
        auteur = etudiantRepository.save(new Etudiant("Auteur Aminata", promo));
        relecteur = etudiantRepository.save(new Etudiant("Relecteur Brice", promo));
        Etudiant second = etudiantRepository.save(new Etudiant("Second Carine", promo));
        intrus = etudiantRepository.save(new Etudiant("Intrus Djomo", promo));

        Instant maintenant = Instant.now();
        session = sessionRepository.save(SessionCours.ouvrir("Seance remplacement", promo, "REMPL2",
                maintenant, Duration.ofMinutes(15)));
        exercice = exerciceRepository.save(
                Exercice.deposer(session, auteur, "https://github.com/auteur/tp-errone", maintenant));
        relecture = relectureRepository.save(Relecture.attribuer(exercice, relecteur, maintenant));
        relectureRepository.save(Relecture.attribuer(exercice, second, maintenant));
        exercice.relecteursAttribues(2);
    }

    private ResultActions remplacer(Long exerciceId, String lien, Long appelantId) throws Exception {
        MockHttpServletRequestBuilder requete = put("/api/exercices/{id}", exerciceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lien\":\"" + lien + "\"}");
        if (appelantId != null) {
            requete.header("X-Etudiant-Id", appelantId);
        }
        return mockMvc.perform(requete);
    }

    @Test
    void ef11_lAuteurRemplaceSonLien_200_etLeRelecteurVoitLeNouveau() throws Exception {
        remplacer(exercice.getId(), NOUVEAU_LIEN, auteur.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(exercice.getId()))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTURE"));

        mockMvc.perform(get("/api/etudiants/{id}/relectures", relecteur.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lien").value(NOUVEAU_LIEN));
    }

    @Test
    void unAutreEtudiant_403_PasAuteur() throws Exception {
        remplacer(exercice.getId(), NOUVEAU_LIEN, intrus.getId())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PAS_AUTEUR"));
    }

    @Test
    void rg11_lienInvalide_400() throws Exception {
        remplacer(exercice.getId(), "ftp://serveur/tp", auteur.getId())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
    }

    @Test
    void rg16_apresUneRelectureRendue_409() throws Exception {
        relecture.rendre(14, "Deja relu.", Instant.now());

        remplacer(exercice.getId(), NOUVEAU_LIEN, auteur.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_RENDUE"));
    }

    @Test
    void rg10_seanceCloturee_409() throws Exception {
        session.cloturer(Instant.now());

        remplacer(exercice.getId(), NOUVEAU_LIEN, auteur.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    void exerciceInconnu_404() throws Exception {
        remplacer(999999L, NOUVEAU_LIEN, auteur.getId())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EXERCICE_INCONNU"));
    }

    @Test
    void sansEnTeteXEtudiantId_400() throws Exception {
        remplacer(exercice.getId(), NOUVEAU_LIEN, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }
}