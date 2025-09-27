package com.commonbrew.pos.mapper;

import org.springframework.stereotype.Component;

import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.dto.AddonResponse;

@Component
public class AddonMapper {

    public AddonResponse toResponse(Addon addon) {
        if (addon == null) {
            return null;
        }

        return AddonResponse.builder()
            .addonId(addon.getAddonId())
            .addonName(addon.getAddonName())
            .price(addon.getPrice())
            .build();
    }
}
