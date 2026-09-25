package com.kfokam48.relectures.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Regle de tirage du relecteur, sans base de donnees ni Spring.
 * RG2 : jamais l'auteur. RG13 (Q7, H3) : au hasard parmi les presents les moins charges.
 */
public final class TirageRelecteur {

    private TirageRelecteur() {
    }

    public static Optional<Long> choisir(Long auteurId, Collection<Long> presents,
                                         Map<Long, Long> charges, Random aleatoire) {
        List<Long> candidats = presents.stream()
                .filter(id -> !id.equals(auteurId))
                .distinct()
                .toList();
        if (candidats.isEmpty()) {
            return Optional.empty(); // RG14 : l'exercice reste en attente d'attribution
        }

        long chargeMinimale = candidats.stream()
                .mapToLong(id -> charges.getOrDefault(id, 0L))
                .min()
                .orElse(0L);

        List<Long> moinsCharges = candidats.stream()
                .filter(id -> charges.getOrDefault(id, 0L) == chargeMinimale)
                .sorted()
                .toList();

        return Optional.of(moinsCharges.get(aleatoire.nextInt(moinsCharges.size())));
    }
}