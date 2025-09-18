package com.commonbrew.pos.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemVariantDto {
    private Long variantId;
    private Long menuItemId;
    private String variantName;
    private Double price;
}
