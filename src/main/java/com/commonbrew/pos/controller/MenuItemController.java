package com.commonbrew.pos.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.service.MenuItemService;

@RestController
@RequestMapping("/api/items")
public class MenuItemController {

    private final MenuItemService itemService;

    public MenuItemController(MenuItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<MenuItem> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping("/category/{menuId}")
    public List<MenuItem> getItemsByCategory(@PathVariable Long menuId) {
        return itemService.getMenuItemsByMenuId(menuId);
    }

    @GetMapping("/{id}")
    public MenuItem getItemById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
    }
}
