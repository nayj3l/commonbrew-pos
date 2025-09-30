package com.commonbrew.pos.dto;

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
    private String menuName;
    private Integer variantId;
    private String variantName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}
