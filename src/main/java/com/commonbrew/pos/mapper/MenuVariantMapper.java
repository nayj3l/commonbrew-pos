package com.commonbrew.pos.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.commonbrew.pos.dto.MenuVariantResponse;
import com.commonbrew.pos.model.MenuVariant;

@Component
public class MenuVariantMapper {

    // Map a single MenuVariant to MenuVariantResponse
    public MenuVariantResponse toResponse(MenuVariant variant) {
        if (variant == null) return null;

        return MenuVariantResponse.builder()
                .variantId(variant.getVariantId())
                .variantName(variant.getVariantName())
                .price(variant.getPrice())
                .code(variant.getCode())
                .active(variant.isActive())
                .build();
    }

    // Map a list of MenuVariants to a list of MenuVariantResponses
    public List<MenuVariantResponse> toResponseList(List<MenuVariant> variants) {
        if (variants == null) return List.of();
        return variants.stream()
                .map(this::toResponse)
                .toList();
    }

}
