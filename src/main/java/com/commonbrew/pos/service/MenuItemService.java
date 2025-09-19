package com.commonbrew.pos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.repository.MenuItemRepository;
import com.commonbrew.pos.repository.MenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository itemRepository;
    private final MenuRepository menuRepository;

    public List<MenuItem> getAllItems() {
        return itemRepository.findAll();
    }

    public List<MenuItem> getMenuItemsByMenuId(Long menuId) {
        return itemRepository.findByMenuId(menuId);
    }

    public MenuItem getItemById(Long id) {
        return itemRepository.findById(id).orElse(null);
    }

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

    public void deleteItem(Long id) {
        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item ID: " + id));

        item.setActive(false);
        if (item.getVariants() != null) {
            item.getVariants().forEach(v -> v.setActive(false));
        }

        itemRepository.save(item);
    }
}
