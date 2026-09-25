package com.kfokam48.relectures.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfokam48.relectures.domain.Etudiant;
import com.kfokam48.relectures.domain.Presence;
import com.kfokam48.relectures.domain.Promotion;
import com.kfokam48.relectures.domain.Relecture;
import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.domain.StatutExercice;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AttributionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private Etudiant camarade;
    private Etudiant troisieme;
    private Etudiant quatrieme;
    private SessionCours session;

    @BeforeEach
    void preparer() {
        Promotion promo = promotionRepository.save(new Promotion("Promo attribution"));
        auteur = etudiantRepository.save(new Etudiant("Auteur Aminata", promo));
        camarade = etudiantRepository.save(new Etudiant("Camarade Brice", promo));
        troisieme = etudiantRepository.save(new Etudiant("Troisieme Carine", promo));
        quatrieme = etudiantRepository.save(new Etudiant("Quatrieme Djomo", promo));
        session = sessionRepository.save(SessionCours.ouvrir("Seance attribution", promo, "ATTRB2",
                Instant.now(), Duration.ofMinutes(15)));
        presenceRepository.save(Presence.parEtudiant(session, auteur, Instant.now()));
    }

    private Long deposer(Etudiant etudiant) throws Exception {
        String reponse = mockMvc.perform(post("/api/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + session.getId() + ",\"etudiantId\":" + etudiant.getId()
                                + ",\"lien\":\"https://github.com/demo/tp\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(reponse).get("id").asLong();
    }

    private void marquerPresence(Etudiant etudiant) throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ATTRB2\",\"etudiantId\":" + etudiant.getId() + "}"))
                .andExpect(status().isCreated());
    }

    private List<Long> relecteursDe(Long exerciceId) {
        return relectureRepository.findByExerciceId(exerciceId).stream()
                .map(Relecture::getRelecteur)
                .map(Etudiant::getId)
                .toList();
    }

    @Test
    void rg14_seulPresent_puisLesRelecteursSontCompletesUnParUnAChaqueArrivee() throws Exception {
        Long exerciceId = deposer(auteur);
        assertThat(relecteursDe(exerciceId)).isEmpty();
        assertThat(exerciceRepository.findById(exerciceId).orElseThrow().getStatut())
                .isEqualTo(StatutExercice.EN_ATTENTE_ATTRIBUTION);

        marquerPresence(camarade);
        assertThat(relecteursDe(exerciceId)).containsExactly(camarade.getId());
        assertThat(exerciceRepository.findById(exerciceId).orElseThrow().getStatut())
                .isEqualTo(StatutExercice.EN_ATTENTE_ATTRIBUTION);

        marquerPresence(troisieme);
        assertThat(relecteursDe(exerciceId)).containsExactlyInAnyOrder(camarade.getId(), troisieme.getId());
        assertThat(exerciceRepository.findById(exerciceId).orElseThrow().getStatut())
                .isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);
    }

    @Test
    void rg12_rg2_avecTroisAutresPresents_deuxRelecteursDistinctsHorsAuteurSontTiresAuDepot() throws Exception {
        presenceRepository.save(Presence.parEtudiant(session, camarade, Instant.now()));
        presenceRepository.save(Presence.parEtudiant(session, troisieme, Instant.now()));
        presenceRepository.save(Presence.parEtudiant(session, quatrieme, Instant.now()));

        mockMvc.perform(post("/api/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + session.getId() + ",\"etudiantId\":" + auteur.getId()
                                + ",\"lien\":\"https://github.com/demo/tp\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTURE"));

        Long exerciceId = exerciceRepository.findByAuteurIdOrderByDeposeAtDesc(auteur.getId()).get(0).getId();
        List<Long> relecteurs = relecteursDe(exerciceId);
        assertThat(relecteurs)
                .hasSize(2)
                .doesNotHaveDuplicates()
                .doesNotContain(auteur.getId())
                .isSubsetOf(camarade.getId(), troisieme.getId(), quatrieme.getId());
    }
}