package com.kfokam48.relectures.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Regle de tirage d'un relecteur, sans base de donnees ni Spring.
 * RG2 : jamais l'auteur. RG12 (v2) : jamais un relecteur deja attribue au meme exercice.
 * RG13 : au hasard parmi les presents les moins charges.
 */
public final class TirageRelecteur {

    private TirageRelecteur() {
    }

    public static Optional<Long> choisir(Long auteurId, Collection<Long> presents,
                                         Map<Long, Long> charges, Random aleatoire) {
        return choisir(auteurId, Set.of(), presents, charges, aleatoire);
    }

    public static Optional<Long> choisir(Long auteurId, Set<Long> dejaAttribues, Collection<Long> presents,
                                         Map<Long, Long> charges, Random aleatoire) {
        List<Long> candidats = presents.stream()
                .filter(id -> !id.equals(auteurId))
                .filter(id -> !dejaAttribues.contains(id))
                .distinct()
                .toList();
        if (candidats.isEmpty()) {
            return Optional.empty(); // RG14 : le relecteur manquant sera tire plus tard
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