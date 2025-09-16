package com.commonbrew.pos.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DrinkDto {
    private Long drinkId;
    private String drinkName;
    private Double basePrice;
    private Double upsizePrice;
}
