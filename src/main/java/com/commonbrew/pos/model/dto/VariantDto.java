package com.commonbrew.pos.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariantDto {
    private Long variantId;
    private String variantName;
    private Double price;
}
