package com.commonbrew.pos.mapper;

import org.springframework.stereotype.Component;

import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.dto.MenuItemResponse;

@Component
public class MenuItemMapper {
    
    public MenuItemResponse toResponse(MenuItem menuItem) {
        if (menuItem == null) {
            return null;
        }

        Long menuId = menuItem.getMenu() != null ? menuItem.getMenu().getId() : null;

        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .active(menuItem.isActive())
                .menuId(menuId)
                .build();
    }
}