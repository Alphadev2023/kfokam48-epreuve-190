package com.kfokam48.relectures.service;

import com.kfokam48.relectures.dto.EtudiantResumeDto;
import com.kfokam48.relectures.exception.MetierException;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.PromotionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EtudiantService {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;

    public EtudiantService(PromotionRepository promotionRepository, EtudiantRepository etudiantRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
    }

    /** EF2, Q1 : l'etudiant se choisit dans cette liste, sans mot de passe. */
    @Transactional(readOnly = true)
    public List<EtudiantResumeDto> listerParPromotion(Long promotionId) {
        if (!promotionRepository.existsById(promotionId)) {
            throw new MetierException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE");
        }
        return etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId).stream()
                .map(EtudiantResumeDto::depuis)
                .toList();
    }
}