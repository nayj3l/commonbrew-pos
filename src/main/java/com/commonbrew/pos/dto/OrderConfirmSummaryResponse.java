package com.commonbrew.pos.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderConfirmSummaryResponse {
    private List<OrderConfirmSummary> items;
    private List<AddonConfirmSummary> addons;
    private BigDecimal total;
}
