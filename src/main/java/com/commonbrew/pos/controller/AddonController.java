package com.commonbrew.pos.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.service.AddonService;
import com.commonbrew.pos.service.MenuService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/addon")
@RequiredArgsConstructor
public class AddonController {

    private final AddonService addonService;
    private final MenuService menuService;

    @GetMapping
    public String showAddons(Model model) {
        model.addAttribute("addons", addonService.getAllAddons());
        return "addons";
    }

    @GetMapping("/add")
    public String showAddAddonForm(Model model) {
        model.addAttribute("addon", new Addon());
        model.addAttribute("allMenus", menuService.getAllMenu());
        return "addons-save";
    }

    @PostMapping("/add")
    public String saveAddon(@ModelAttribute Addon addon, 
            @RequestParam(required = false) List<Long> menuIds) {
        addonService.saveAddon(addon, menuIds);
        return "redirect:/addon";
    }

    @GetMapping("/edit/{id}")
    public String showEditAddonForm(@PathVariable Long id, Model model) {
        Addon addon = addonService.getAddonById(id);
        model.addAttribute("addon", addon);
        model.addAttribute("allMenus", menuService.getAllMenu());
        return "addons-save";
    }

    @PostMapping("/edit/{id}")
    public String updateAddon(@PathVariable Long id,
                            @ModelAttribute Addon addon,
                            @RequestParam(required = false) List<Long> menuIds) {
        addon.setAddonId(id);
        addonService.saveAddon(addon, menuIds);
        return "redirect:/addon";
    }

    @GetMapping("/delete/{id}")
    public String deleteAddon(@PathVariable Long id) {
        addonService.deleteAddon(id);
        return "redirect:/addon";
    }
}
