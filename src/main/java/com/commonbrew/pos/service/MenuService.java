package com.commonbrew.pos.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.commonbrew.pos.mapper.MenuMapper;
import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.dto.MenuResponse;
import com.commonbrew.pos.repository.AddonRepository;
import com.commonbrew.pos.repository.MenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final AddonRepository addonRepository;
    private final MenuMapper menuMapper;

    @Cacheable("menus")
    public List<MenuResponse> getAllMenu() {
        List<Menu> menus = menuRepository.findAllActiveMenusWithItems();

        if (menus.isEmpty()) {
            return Collections.emptyList();
        }

        initializeMenuAddons(menus);
        
        return menus.stream()
                .map(menuMapper::toResponse)
                .toList();
    }

    public List<MenuResponse> getMenuApiResponse() {
        List<Menu> menus = menuRepository.findAll();
        return menus.stream()
                .map(menuMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "menu", key = "#id")
    public Menu getMenuById(Long id) {
        return menuRepository.findById(id)
                .filter(Menu::isActive)
                .orElseThrow(() -> new RuntimeException("Menu not found with id " + id));
    }

    @CacheEvict(value = {"menus", "menu"}, allEntries = true)
    public Menu save(Menu menu) {
        if (menu.getCode() == null || menu.getCode().isBlank()) {
            String baseCode = menu.getName().trim().toLowerCase().replace(" ", "_");
            String uniqueCode = generateUniqueCode(baseCode);
            menu.setCode(uniqueCode);
        }
        menu.setActive(true); 
        return menuRepository.save(menu);
    }

    private String generateUniqueCode(String baseCode) {
        String code = baseCode;
        int counter = 2;
        while (menuRepository.existsByCode(code)) {
            code = baseCode + "_" + counter;
            counter++;
        }
        return code;
    }

    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }
   
    public Menu findById(Long id) {
        return menuRepository.findById(id)
                .filter(Menu::isActive)
                .orElseThrow(() -> new RuntimeException("Menu not found with id " + id));
    }

    @CacheEvict(value = {"menus", "menu"}, allEntries = true)
    public void softDelete(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot delete. Menu not found with id " + id));
        menu.setActive(false);
        menuRepository.save(menu);
    }
    
    private void initializeMenuAddons(List<Menu> menus) {
        List<Addon> addons = addonRepository.findAddonsByMenus(menus);
        Map<Long, List<Addon>> addonsByMenuId = new HashMap<>();

        for (Addon addon : addons) {
            if (addon.getMenu() == null) continue;
            for (Menu menu : addon.getMenu()) {
                if (menu == null || menu.getId() == null) continue;
                addonsByMenuId
                    .computeIfAbsent(menu.getId(), k -> new ArrayList<>())
                    .add(addon);
            }
        }

        for (Menu menu : menus) {
            List<Addon> menuAddons = addonsByMenuId.get(menu.getId());
            if (menuAddons != null) {
                menu.setAddons(menuAddons);
            } else {
                menu.setAddons(Collections.emptyList());
            }
        }
    }


}
