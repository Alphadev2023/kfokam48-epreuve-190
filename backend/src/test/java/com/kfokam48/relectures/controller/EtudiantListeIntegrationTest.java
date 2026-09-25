package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.domain.Etudiant;
import com.kfokam48.relectures.domain.Promotion;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EtudiantListeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Test
    void laListeContientLesEtudiantsDeLaPromotionTriesParNom() throws Exception {
        Promotion promo = promotionRepository.save(new Promotion("Promo liste"));
        Promotion autre = promotionRepository.save(new Promotion("Autre promo"));
        etudiantRepository.save(new Etudiant("Zambo Carine", promo));
        etudiantRepository.save(new Etudiant("Abena Brice", promo));
        etudiantRepository.save(new Etudiant("Intrus Hors Promo", autre));

        mockMvc.perform(get("/api/promotions/{id}/etudiants", promo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nom").value("Abena Brice"))
                .andExpect(jsonPath("$[1].nom").value("Zambo Carine"));
    }

    @Test
    void unePromotionInconnueRenvoie404() throws Exception {
        mockMvc.perform(get("/api/promotions/{id}/etudiants", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}