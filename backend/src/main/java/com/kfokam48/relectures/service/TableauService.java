package com.kfokam48.relectures.service;

import com.kfokam48.relectures.dto.LigneTableauDto;
import com.kfokam48.relectures.exception.MetierException;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.ExerciceRepository;
import com.kfokam48.relectures.repository.PresenceRepository;
import com.kfokam48.relectures.repository.PromotionRepository;
import com.kfokam48.relectures.repository.RelectureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** EF7 (Q16) : 5 requetes agregees au total, quel que soit le nombre d'etudiants (ENF2). */
@Service
public class TableauService {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final ExerciceRepository exerciceRepository;
    private final RelectureRepository relectureRepository;

    public TableauService(PromotionRepository promotionRepository, EtudiantRepository etudiantRepository,
                          PresenceRepository presenceRepository, ExerciceRepository exerciceRepository,
                          RelectureRepository relectureRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
        this.exerciceRepository = exerciceRepository;
        this.relectureRepository = relectureRepository;
    }

    @Transactional(readOnly = true)
    public List<LigneTableauDto> tableau(Long promotionId) {
        if (!promotionRepository.existsById(promotionId)) {
            throw new MetierException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE");
        }

        Map<Long, Long> presences = comptes(presenceRepository.compterParEtudiantDeLaPromotion(promotionId));
        Map<Long, Long> exercices = comptes(exerciceRepository.compterParAuteurDeLaPromotion(promotionId));
        Map<Long, Long> enAttente = comptes(relectureRepository.compterEnAttenteParRelecteurDeLaPromotion(promotionId));
        Map<Long, Double> moyennes = new HashMap<>();
        for (Object[] ligne : relectureRepository.moyenneRecueParAuteurDeLaPromotion(promotionId)) {
            moyennes.put((Long) ligne[0], (Double) ligne[1]);
        }

        return etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId).stream()
                .map(e -> new LigneTableauDto(
                        e.getId(),
                        e.getNom(),
                        presences.getOrDefault(e.getId(), 0L).intValue(),
                        exercices.getOrDefault(e.getId(), 0L).intValue(),
                        arrondir(moyennes.get(e.getId())),
                        enAttente.getOrDefault(e.getId(), 0L).intValue()))
                .toList();
    }

    /** RG17 : arrondi a 2 decimales, null si aucune note recue. */
    static Double arrondir(Double moyenne) {
        return moyenne == null ? null : BigDecimal.valueOf(moyenne).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static Map<Long, Long> comptes(List<Object[]> lignes) {
        Map<Long, Long> resultat = new HashMap<>();
        for (Object[] ligne : lignes) {
            resultat.put((Long) ligne[0], (Long) ligne[1]);
        }
        return resultat;
    }
}