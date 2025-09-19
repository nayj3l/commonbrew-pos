package com.commonbrew.pos.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.service.MenuItemService;
import com.commonbrew.pos.service.MenuService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final MenuItemService itemService;

    @GetMapping()
    public String showMenu(Model model) {
        List<Menu> menu = menuService.getAllMenu();
        model.addAttribute("menu", menu);
        return "menu";
    }

    @GetMapping("/add")
    public String showAddMenuForm(Model model) {
        model.addAttribute("menu", new Menu());
        return "menu-add";
    }

	@PostMapping("/add")
	public String save(@ModelAttribute Menu menu) {
		menuService.save(menu);
		return "redirect:/menu";
	}

    @GetMapping("/{id}")
    public String showMenuItems(@PathVariable Long id, Model model) {
        Menu menu = menuService.getMenuById(id);
        List<MenuItem> menuItems = itemService.getMenuItemsByMenuId(id);

        model.addAttribute("menu", menu);
        model.addAttribute("menuItems", menuItems);
        return "items";
    }

    @GetMapping("/{id}/edit")
    public String editMenu(@PathVariable Long id, Model model) {
        Menu menu = menuService.findById(id);
        model.addAttribute("menu", menu);
        return "menu-edit";
    }

    @PostMapping("/{id}/update")
    public String updateMenu(@PathVariable Long id, @ModelAttribute Menu menuDetails) {
        Menu menu = menuService.findById(id);
        menu.setName(menuDetails.getName());
        menuService.save(menu);
        return "redirect:/menu";
    }

    @PostMapping("/{id}/delete")
    public String deleteMenu(@PathVariable Long id) {
        menuService.softDelete(id);
        return "redirect:/menu";
    }

    // Show form to add a new menu item
    @GetMapping("{menuId}/item/add")
    public String addItemForm(@PathVariable Long menuId, Model model) {
        Menu menu = menuService.getMenuById(menuId);
        model.addAttribute("menu", menu);
        model.addAttribute("item", new MenuItem());
        return "save-item-form";
    }

    // Show form to edit a item
    @GetMapping("/{menuId}/item/edit/{itemId}")
    public String editItemForm(@PathVariable Long menuId, @PathVariable Long itemId, Model model) {
        MenuItem item = itemService.getItemById(itemId);
        Menu menu = menuService.getMenuById(menuId);
        model.addAttribute("menu", menu);
        model.addAttribute("item", item);
        return "save-item-form";
    }

    // Save new menu item
    @PostMapping("/{menuId}/item/save")
    public String saveItem(@PathVariable Long menuId, @ModelAttribute MenuItem item) {
        itemService.saveItem(menuId, item);
        return "redirect:/menu/" + menuId;
    }

    // Delete a item
    @GetMapping("/{menuId}/item/delete/{itemId}")
    public String deleteItem(@PathVariable Long menuId, @PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return "redirect:/menu";
    }
    
}
