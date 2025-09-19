package com.commonbrew.pos.model.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddonConfirmSummary {
    private String itemName;
    private Long parentItemId;
    private Long addonId;
    private String addonName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}

