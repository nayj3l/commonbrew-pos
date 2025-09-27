package com.commonbrew.pos.model.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {
    private Long id;
    private String code;
    private String name;
    private boolean active;
    private String imageUrl;
    private List<MenuItemResponse> items;
    private List<AddonResponse> addons;
}
