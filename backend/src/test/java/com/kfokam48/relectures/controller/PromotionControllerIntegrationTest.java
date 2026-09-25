package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.domain.Promotion;
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
class PromotionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotionRepository;

    @Test
    void listeLesPromotionsTrieesParNom() throws Exception {
        promotionRepository.save(new Promotion("Promo Z"));
        promotionRepository.save(new Promotion("Promo A"));

        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Promo A"))
                .andExpect(jsonPath("$[1].nom").value("Promo Z"));
    }

    @Test
    void uneRouteInexistanteRenvoieLeFormatDErreurDuContrat() throws Exception {
        mockMvc.perform(get("/api/route-inexistante"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}