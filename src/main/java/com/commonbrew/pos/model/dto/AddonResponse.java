package com.commonbrew.pos.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddonResponse {
    private Long addonId;
    private String addonName;
    private Double price;
}
