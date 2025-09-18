package com.commonbrew.pos.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuItemDto {
    private Long id;
    private String name;

    // optional convenience fields for backward compatibility
    private Double basePrice;     // optional, e.g. first/default variant price
    private Double upsizePrice;   // optional: price of an "Upsize" variant if exists
    private List<VariantDto> variants;

    public MenuItemDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
