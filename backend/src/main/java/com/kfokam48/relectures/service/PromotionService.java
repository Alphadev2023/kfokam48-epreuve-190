package com.kfokam48.relectures.service;

import com.kfokam48.relectures.dto.PromotionDto;
import com.kfokam48.relectures.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Transactional(readOnly = true)
    public List<PromotionDto> lister() {
        return promotionRepository.findAllByOrderByNomAsc().stream()
                .map(PromotionDto::depuis)
                .toList();
    }
}