package com.commonbrew.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MenuVariantDto {
    private Long variantId;
    private Long menuItemId;
    private String variantName;
    private Double price;
}