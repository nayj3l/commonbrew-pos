package com.commonbrew.pos.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.dto.AddonResponse;
import com.commonbrew.pos.model.dto.MenuItemResponse;
import com.commonbrew.pos.model.dto.MenuResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MenuMapper {

    private final MenuItemMapper menuItemMapper;
    private final AddonMapper addonMapper;

    public MenuResponse toResponse(Menu menu) {
        if (menu == null) {
            return null;
        }

        List<MenuItemResponse> itemResponses = menu.getItems() != null
                ? menu.getItems().stream()
                    .map(menuItemMapper::toResponse)
                    .toList()
                : Collections.emptyList();

        List<AddonResponse> addonResponses = menu.getAddons() != null
                ? menu.getAddons().stream()
                    .map(addonMapper::toResponse)
                    .toList()
                : Collections.emptyList();
        
        return MenuResponse.builder()
                .id(menu.getId())
                .code(menu.getCode())
                .name(menu.getName())
                .active(menu.isActive())
                .items(itemResponses)
                .addons(addonResponses)
                .imageUrl(menu.getImageUrl())
                .build();
    }
    
}
