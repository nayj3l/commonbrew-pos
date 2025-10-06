package com.commonbrew.pos.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.commonbrew.pos.dto.AddonResponse;
import com.commonbrew.pos.dto.MenuItemResponse;
import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.repository.MenuItemRepository;
import com.commonbrew.pos.repository.MenuRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository itemRepository;
    private final MenuRepository menuRepository;

    @Cacheable("menuItems")
    public List<MenuItemResponse> getAllItems() {
        return itemRepository.findAllActive()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Cacheable("menuItemsEntities")
    public List<MenuItem> getAllActiveEntities() {
        return itemRepository.findAllActive();
    }


    @Cacheable(value = "menuItemsByMenu", key = "#menuId")
    public List<MenuItem> getMenuItemsByMenuId(Long menuId) {
        return itemRepository.findByMenuId(menuId);
    }

    @Cacheable(value = "menuItem", key = "#id")
    public MenuItem getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MenuItem not found with id " + id));
    }

    @CacheEvict(value = {"menus", "menu", "menuItems", "menuItemsByMenu", "menuItem"}, allEntries = true)
    public MenuItem saveItem(Long menuId, MenuItem menuItem) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid menu ID: " + menuId));

        menuItem.setMenu(menu);

        if (menuItem.getId() != null && !itemRepository.existsById(menuItem.getId())) {
            menuItem.setId(null);
        }

        return itemRepository.save(menuItem);
    }

    @CacheEvict(value = {"menus", "menu", "menuItems", "menuItemsByMenu", "menuItem"}, allEntries = true)
    public void deleteItem(Long id) {
        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item ID: " + id));

        item.setActive(false);
        itemRepository.save(item);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .active(menuItem.isActive())
                .menuId(menuItem.getMenu() != null ? menuItem.getMenu().getId() : null)
                .addons(
                        menuItem.getMenu().getAddons().stream()
                                .map(addon -> AddonResponse.builder()
                                        .addonId(addon.getAddonId())
                                        .addonName(addon.getAddonName())
                                        .price(addon.getPrice())
                                        .build()
                                )
                                .toList()
                )
                .build();
    }
}
