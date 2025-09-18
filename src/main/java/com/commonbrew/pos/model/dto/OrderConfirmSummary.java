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
public class OrderConfirmSummary {
    private String itemName;
    private Integer variantId;
    private String variantName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}
