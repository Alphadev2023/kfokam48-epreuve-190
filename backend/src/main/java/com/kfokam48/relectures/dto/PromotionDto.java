package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.Promotion;

public record PromotionDto(Long id, String nom) {

    public static PromotionDto depuis(Promotion promotion) {
        return new PromotionDto(promotion.getId(), promotion.getNom());
    }
}