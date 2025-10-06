package com.commonbrew.pos.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.commonbrew.pos.dto.MenuResponse;
import com.commonbrew.pos.dto.MenuVariantResponse;
import com.commonbrew.pos.dto.request.MenuUpdateRequest;
import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.MenuVariant;
import com.commonbrew.pos.service.MenuItemService;
import com.commonbrew.pos.service.MenuService;
import com.commonbrew.pos.service.MenuVariantService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final MenuService menuService;
    private final MenuVariantService menuVariantService;
    private final MenuItemService itemService;

    @GetMapping()
    public String showMenu(Model model) {
        List<MenuResponse> menu = menuService.getAllMenu().getMenus();
        model.addAttribute("menu", menu);
        return "menu";
    }

    @GetMapping("/add")
    public String showAddMenuForm(Model model) {
        List<MenuVariant> allActiveVariants = menuVariantService.findAllActiveVariants();
        List<String> existingMenuNames = menuService.getAllMenu().getMenus().stream()
                .map(menu -> menu.getName().toLowerCase())
                .toList();

        model.addAttribute("menu", new Menu());
        model.addAttribute("allVariants", allActiveVariants);
        model.addAttribute("existingMenuNames", existingMenuNames);
        return "menu-add";
    }

    @PostMapping("/add")
    public String save(@RequestParam String variantNames,
            @RequestParam String variantPrices,
            @ModelAttribute Menu menu) {
        Set<MenuVariant> finalVariants = new HashSet<>();

        if (variantNames != null && !variantNames.trim().isEmpty() &&
                variantPrices != null && !variantPrices.trim().isEmpty()) {

            String[] variantNameArray = variantNames.split(",");
            String[] variantPriceArray = variantPrices.split(",");

            if (variantNameArray.length != variantPriceArray.length) {
                throw new IllegalArgumentException("Variant names and prices count mismatch");
            }

            for (int i = 0; i < variantNameArray.length; i++) {
                String variantName = variantNameArray[i].trim();
                Double variantPrice;

                try {
                    variantPrice = Double.parseDouble(variantPriceArray[i].trim());
                } catch (NumberFormatException e) {
                    variantPrice = 0.0;
                }

                // make a final copy so the lambda can capture it
                final Double priceCopy = variantPrice;

                if (!variantName.isEmpty()) {
                    MenuVariant newVariant = MenuVariant.builder()
                            .variantName(variantName)
                            .price(priceCopy)
                            .active(true)
                            .build();

                    menuVariantService.save(newVariant);
                    finalVariants.add(newVariant);
                }
            }
        }

        menu.setVariants(finalVariants);
        menuService.save(menu);
        return "redirect:/menu";
    }

    @GetMapping("/{id}")
    public String showMenuItems(@PathVariable Long id, Model model) {
        MenuResponse menu = menuService.getMenuById(id).getMenu();
        model.addAttribute("menu", menu);
        model.addAttribute("menuItems", menu.getItems());
        model.addAttribute("variants", menu.getVariants());
        return "items";
    }

    @GetMapping("/{id}/edit")
    public String editMenu(@PathVariable Long id, Model model) {
        Menu menu = menuService.getActiveMenuById(id);
        List<MenuVariantResponse> allVariants = menuVariantService.getAllVariants();
        List<Long> selectedVariants = menu.getVariants().stream()
                .map(MenuVariant::getVariantId)
                .toList();

        MenuUpdateRequest menuUpdateRequest = new MenuUpdateRequest();
        menuUpdateRequest.setMenuId(menu.getId());
        menuUpdateRequest.setName(menu.getName());
        menuUpdateRequest.setVariantsId(selectedVariants);

        model.addAttribute("menu", menu);
        model.addAttribute("menuUpdateRequest", menuUpdateRequest);
        model.addAttribute("allVariants", allVariants);
        model.addAttribute("selectedVariants", selectedVariants);

        return "menu-edit";
    }

    @PostMapping("/{id}/update")
    public String updateMenu(@PathVariable Long id, @ModelAttribute MenuUpdateRequest request) {
        Menu menu = menuService.findById(id);

        // fetch variants by IDs and attach
        List<MenuVariant> variants = menuVariantService.findAllById(request.getVariantsId());
        menu.setVariants(new HashSet<>(variants));

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
        MenuResponse menu = menuService.getMenuById(menuId).getMenu();
        model.addAttribute("menu", menu);
        model.addAttribute("item", new MenuItem());
        return "menu-item-save";
    }

    // Show form to edit a item
    @GetMapping("/{menuId}/item/edit/{itemId}")
    public String editItemForm(@PathVariable Long menuId, @PathVariable Long itemId, Model model) {
        MenuResponse menu = menuService.getMenuById(menuId).getMenu();
        model.addAttribute("menu", menu);
        model.addAttribute("variants", menu.getVariants());
        model.addAttribute("item", menu.getItems()
                .stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null));
        return "menu-item-save";
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
        return "redirect:/menu/" + menuId;
    }

}
