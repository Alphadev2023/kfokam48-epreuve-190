package com.kfokam48.relectures.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfokam48.relectures.domain.Promotion;
import com.kfokam48.relectures.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
class SessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PromotionRepository promotionRepository;

    private Long promotionId;

    @BeforeEach
    void preparer() {
        promotionId = promotionRepository.save(new Promotion("Promo integration")).getId();
    }

    @Test
    void ouvrirUneSessionRenvoie201AvecUnCodeValableQuinzeMinutes() throws Exception {
        MvcResult resultat = mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Seance Git\",\"promotionId\":" + promotionId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.matchesPattern("^[A-HJ-NP-Z2-9]{6}$")))
                .andReturn();

        JsonNode corps = objectMapper.readTree(resultat.getResponse().getContentAsString());
        Instant ouverture = Instant.parse(corps.get("ouvertureAt").asText());
        Instant expiration = Instant.parse(corps.get("expirationAt").asText());
        assertThat(Duration.between(ouverture, expiration)).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void unTitreManquantRenvoie400ChampManquant() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promotionId\":" + promotionId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void unePromotionInconnueRenvoie400PromotionInconnue() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Seance\",\"promotionId\":999999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }

    @Test
    void laSessionOuverteApparaitDansLaListeDeLaPromotion() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Seance listee\",\"promotionId\":" + promotionId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/sessions").param("promotionId", promotionId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titre").value("Seance listee"))
                .andExpect(jsonPath("$[0].statut").value("OUVERTE"));
    }
}