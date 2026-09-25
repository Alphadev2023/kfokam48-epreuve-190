package com.kfokam48.relectures.controller;

import com.kfokam48.relectures.dto.EtudiantResumeDto;
import com.kfokam48.relectures.dto.PromotionDto;
import com.kfokam48.relectures.service.EtudiantService;
import com.kfokam48.relectures.service.PromotionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final EtudiantService etudiantService;

    public PromotionController(PromotionService promotionService, EtudiantService etudiantService) {
        this.promotionService = promotionService;
        this.etudiantService = etudiantService;
    }

    @GetMapping
    public List<PromotionDto> lister() {
        return promotionService.lister();
    }

    /** [AJOUT] EF2 : etudiants d'une promotion, tries par nom. */
    @GetMapping("/{id}/etudiants")
    public List<EtudiantResumeDto> etudiants(@PathVariable Long id) {
        return etudiantService.listerParPromotion(id);
    }
}