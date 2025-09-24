package com.commonbrew.pos.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVariantResponse {
    private Long variantId;
    private String variantName;
    private Double price;
    private String code;
    private boolean active;
}
