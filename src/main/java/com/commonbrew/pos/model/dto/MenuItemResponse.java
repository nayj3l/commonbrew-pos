package com.commonbrew.pos.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private boolean active;
    private Long menuId;
    private List<ItemVariantResponse> variants;
    private List<AddonResponse> addons;
}
