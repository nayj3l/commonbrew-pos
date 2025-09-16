package com.commonbrew.pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.service.AddonService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/addon")
@RequiredArgsConstructor
public class AddonController {

    private final AddonService addonService;

    @GetMapping
    public String showAddons(Model model) {
        model.addAttribute("addons", addonService.getAllAddons());
        return "addons";
    }

    @GetMapping("/add")
    public String showAddAddonForm(Model model) {
        model.addAttribute("addon", new Addon());
        return "addon-form";
    }

    @PostMapping("/add")
    public String addAddon(@ModelAttribute Addon addon) {
        addonService.saveAddon(addon);
        return "redirect:/addon";
    }

    @GetMapping("/edit/{id}")
    public String showEditAddonForm(@PathVariable Long id, Model model) {
        Addon addon = addonService.getAddonById(id);
        model.addAttribute("addon", addon);
        return "addon-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteAddon(@PathVariable Long id) {
        addonService.deleteAddon(id);
        return "redirect:/addon";
    }
}
