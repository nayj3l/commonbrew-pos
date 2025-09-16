package com.commonbrew.pos.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummary {
    private List<OrderItemDto> items;
    private List<OrderItemDto> addons;
    private double total;

    private List<Long> drinkIds;
    private List<Integer> quantities;
    private List<Long> addonIds;
}
