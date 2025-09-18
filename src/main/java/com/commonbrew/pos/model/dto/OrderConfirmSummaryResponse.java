package com.commonbrew.pos.model.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmSummaryResponse {
    private List<OrderConfirmSummary> items;
    private List<AddonConfirmSummary> addons;
    private BigDecimal total;
}

