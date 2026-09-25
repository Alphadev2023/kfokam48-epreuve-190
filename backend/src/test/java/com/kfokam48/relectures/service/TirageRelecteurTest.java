package com.kfokam48.relectures.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** B6 : test unitaire sur les regles RG2, RG13 et RG14. */
class TirageRelecteurTest {

    private static final Long AUTEUR = 1L;

    @Test
    void rg2_lAuteurNEstJamaisTireSur1000Tirages() {
        Random aleatoire = new Random(42);
        for (int i = 0; i < 1000; i++) {
            Optional<Long> relecteur = TirageRelecteur.choisir(AUTEUR, List.of(1L, 2L, 3L), Map.of(), aleatoire);
            assertThat(relecteur).isPresent().get().isNotEqualTo(AUTEUR);
        }
    }

    @Test
    void rg13_seulLeMoinsChargeEstRetenu() {
        Map<Long, Long> charges = Map.of(2L, 1L, 3L, 0L, 4L, 1L);
        for (int i = 0; i < 100; i++) {
            Optional<Long> relecteur = TirageRelecteur.choisir(AUTEUR, List.of(1L, 2L, 3L, 4L), charges, new Random(i));
            assertThat(relecteur).contains(3L);
        }
    }

    @Test
    void rg13_leTirageEstReellementAleatoireEntreCandidatsEgaux() {
        Set<Long> tires = new HashSet<>();
        Random aleatoire = new Random(7);
        for (int i = 0; i < 200; i++) {
            TirageRelecteur.choisir(AUTEUR, List.of(1L, 2L, 3L, 4L), Map.of(), aleatoire).ifPresent(tires::add);
        }
        assertThat(tires).containsExactlyInAnyOrder(2L, 3L, 4L);
    }

    @Test
    void rg14_aucunCandidatSiLAuteurEstSeulPresent() {
        assertThat(TirageRelecteur.choisir(AUTEUR, List.of(AUTEUR), Map.of(), new Random())).isEmpty();
    }

    @Test
    void rg14_aucunCandidatSiPersonneNEstPresent() {
        assertThat(TirageRelecteur.choisir(AUTEUR, List.of(), Map.of(), new Random())).isEmpty();
    }

    @Test
    void rg12_unRelecteurDejaAttribueNEstPasTireUneSecondeFois() {
        for (int i = 0; i < 200; i++) {
            Optional<Long> relecteur = TirageRelecteur.choisir(AUTEUR, Set.of(2L), List.of(1L, 2L, 3L), Map.of(), new Random(i));
            assertThat(relecteur).contains(3L);
        }
    }
}