package com.commonbrew.pos.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.dto.AddonResponse;
import com.commonbrew.pos.model.dto.ItemVariantResponse;
import com.commonbrew.pos.model.dto.MenuItemResponse;
import com.commonbrew.pos.repository.MenuItemRepository;
import com.commonbrew.pos.repository.MenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository itemRepository;
    private final MenuRepository menuRepository;

    @Cacheable("menuItems")
    public List<MenuItemResponse> getAllItems() {
        return itemRepository.findAllWithVariants()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Cacheable(value = "menuItemsByMenu", key = "#menuId")
    public List<MenuItem> getMenuItemsByMenuId(Long menuId) {
        return itemRepository.findByMenuIdWithVariants(menuId);
    }
    
    @Cacheable(value = "menuItem", key = "#id")
    public MenuItem getItemById(Long id) {
        return itemRepository.findByIdWithVariants(id).orElse(null);
    }

    @CacheEvict(value = {"menuItems", "menuItemsByMenu", "menuItem"}, allEntries = true)
    public MenuItem saveItem(Long menuId, MenuItem item) {
        // Load the managed Menu entity
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid menu ID: " + menuId));

        // Always set the managed menu, don't rely on bound object
        item.setMenu(menu);

        // Ensure new items have null id to prevent overwriting
        if (item.getId() != null && !itemRepository.existsById(item.getId())) {
            item.setId(null);
        }

        // Remove variants that are null or empty
        if (item.getVariants() != null) {
            item.getVariants().removeIf(v -> v.getVariantName() == null || v.getVariantName().isEmpty());

            // **Important:** link each variant to its parent
            item.getVariants().forEach(v -> v.setMenuItem(item));
        }

        return itemRepository.save(item);
    }

    @CacheEvict(value = {"menuItems", "menuItemsByMenu", "menuItem"}, allEntries = true)
    public void deleteItem(Long id) {
        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item ID: " + id));

        item.setActive(false);
        if (item.getVariants() != null) {
            item.getVariants().forEach(v -> v.setActive(false));
        }

        itemRepository.save(item);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .active(menuItem.isActive())
                .menuId(menuItem.getMenu() != null ? menuItem.getMenu().getId() : null)
                .variants(
                        menuItem.getVariants().stream()
                                .map(variant -> ItemVariantResponse.builder()
                                        .variantId(variant.getVariantId())
                                        .variantName(variant.getVariantName())
                                        .price(variant.getPrice())
                                        .code(variant.getCode())
                                        .active(variant.isActive())
                                        .build()
                                )
                                .toList()
                )
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
