package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.domain.Etudiant;
import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.Promotion;
import com.kfokam48.relectures.domain.Relecture;
import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.domain.StatutExercice;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RelectureControllerIntegrationTest {

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
    private Etudiant second;
    private Etudiant intrus;
    private Exercice exercice;
    private Relecture relecture;
    private Relecture relectureDuSecond;

    /** RG12 (v2) : l'exercice a deux relecteurs, relecteur et second. */
    @BeforeEach
    void preparer() {
        Promotion promo = promotionRepository.save(new Promotion("Promo relecture"));
        auteur = etudiantRepository.save(new Etudiant("Auteur Aminata", promo));
        relecteur = etudiantRepository.save(new Etudiant("Relecteur Brice", promo));
        second = etudiantRepository.save(new Etudiant("Second Carine", promo));
        intrus = etudiantRepository.save(new Etudiant("Intrus Djomo", promo));
        SessionCours session = sessionRepository.save(SessionCours.ouvrir("Seance relecture", promo, "RELEC2",
                Instant.now(), Duration.ofMinutes(15)));
        exercice = exerciceRepository.save(Exercice.deposer(session, auteur, "https://github.com/auteur/tp", Instant.now()));
        relecture = relectureRepository.save(Relecture.attribuer(exercice, relecteur, Instant.now()));
        relectureDuSecond = relectureRepository.save(Relecture.attribuer(exercice, second, Instant.now()));
        exercice.relecteursAttribues(2);
    }

    private ResultActions rendre(Long relectureId, String note, Long appelantId) throws Exception {
        MockHttpServletRequestBuilder requete = post("/api/relectures/{id}", relectureId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\":" + note + ",\"commentaire\":\"Bon travail, tests a completer.\"}");
        if (appelantId != null) {
            requete.header("X-Etudiant-Id", appelantId);
        }
        return mockMvc.perform(requete);
    }

    @Test
    void premiereRelecture_200_exerciceResteEnAttenteDeLaSeconde() throws Exception {
        rendre(relecture.getId(), "15", relecteur.getId()).andExpect(status().isOk());

        assertThat(exerciceRepository.findById(exercice.getId()).orElseThrow().getStatut())
                .isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);

        mockMvc.perform(get("/api/etudiants/{id}/exercices", auteur.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].note").value(15))
                .andExpect(jsonPath("$[0].relecteurNom").doesNotExist())
                .andExpect(jsonPath("$[0].relecteurId").doesNotExist());
    }

    @Test
    void rg21_lesDeuxRelecturesRendues_exerciceRelu() throws Exception {
        rendre(relecture.getId(), "13", relecteur.getId()).andExpect(status().isOk());
        rendre(relectureDuSecond.getId(), "16", second.getId()).andExpect(status().isOk());

        assertThat(exerciceRepository.findById(exercice.getId()).orElseThrow().getStatut())
                .isEqualTo(StatutExercice.RELU);
    }

    @Test
    void rg3_noteHorsBornesOuDecimale_400NoteInvalide() throws Exception {
        rendre(relecture.getId(), "21", relecteur.getId())
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
        rendre(relecture.getId(), "-1", relecteur.getId())
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
        rendre(relecture.getId(), "12.5", relecteur.getId())
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
    }

    @Test
    void rg15_secondEnvoi_409EtNoteInchangee() throws Exception {
        rendre(relecture.getId(), "15", relecteur.getId()).andExpect(status().isOk());

        rendre(relecture.getId(), "20", relecteur.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));

        assertThat(relectureRepository.findById(relecture.getId()).orElseThrow().getNote()).isEqualTo(15);
    }

    @Test
    void rg2_lAuteurTenteDeSeRelire_403AutoRelecture() throws Exception {
        rendre(relecture.getId(), "20", auteur.getId())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTO_RELECTURE"));
    }

    @Test
    void unEtudiantQuiNEstPasCeRelecteur_403() throws Exception {
        rendre(relecture.getId(), "10", intrus.getId())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RELECTEUR_NON_ATTRIBUE"));
    }

    @Test
    void relectureInconnue_400() throws Exception {
        rendre(999999L, "10", relecteur.getId())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RELECTURE_INCONNUE"));
    }

    @Test
    void leRelecteurVoitLaRelectureAFaireAvecLeLien() throws Exception {
        mockMvc.perform(get("/api/etudiants/{id}/relectures", relecteur.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(relecture.getId()))
                .andExpect(jsonPath("$[0].lien").value("https://github.com/auteur/tp"))
                .andExpect(jsonPath("$[0].rendue").value(false));
    }
}