package com.commonbrew.pos.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commonbrew.pos.dto.MenuResponse;
import com.commonbrew.pos.dto.wrapper.MenuListWrapper;
import com.commonbrew.pos.dto.wrapper.MenuWrapper;
import com.commonbrew.pos.mapper.MenuMapper;
import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.MenuVariant;
import com.commonbrew.pos.repository.AddonRepository;
import com.commonbrew.pos.repository.MenuItemRepository;
import com.commonbrew.pos.repository.MenuRepository;
import com.commonbrew.pos.repository.MenuVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final AddonRepository addonRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuVariantRepository menuVariantRepository;
    private final MenuMapper menuMapper;

    @Cacheable(value = "menus", key = "'all'")
    public MenuListWrapper getAllMenu() {
        List<Menu> menus = menuRepository.findAllActiveMenus();
        if (menus.isEmpty()) {
            return new MenuListWrapper();
        }

        fetchAndAttachItems(menus);

        fetchAndAttachVariants(menus);

        initializeMenuAddons(menus);

        List<MenuResponse> response = menus.stream()
                .map(menuMapper::toResponse)
                .toList();
    
        return new MenuListWrapper(response);
    }

    public List<MenuResponse> getMenuApiResponse() {
        List<Menu> menus = menuRepository.findAll();
        return menus.stream()
                .map(menuMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "menu", key = "#id")
    public MenuWrapper getMenuById(Long id) {
        Menu menu = menuRepository.findById(id)
                .filter(Menu::isActive)
                .orElseThrow(() -> new RuntimeException("Menu not found with id " + id));

        // Filter items by name (case-insensitive)
        Set<MenuItem> filteredItems = menu.getItems().stream()
            .filter(MenuItem::isActive)
            .sorted(Comparator.comparing(MenuItem::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        menu.setItems(filteredItems);

        MenuResponse menuResponse = menuMapper.toResponse(menu);
        return new MenuWrapper(menuResponse);
    }

    @Transactional(readOnly = true)
    public Menu getActiveMenuById(Long id) {
        Menu menu = menuRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new RuntimeException("Menu not found"));
        menu.getItems().size();
        menu.getVariants().size();
        return menu;
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

    private void fetchAndAttachItems(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) return;

        List<MenuItem> activeItems = menuItemRepository.findAllActive();

        // defensive: filter out items with missing menu or id just in case
        Map<Long, List<MenuItem>> itemsByMenuId = activeItems.stream()
                .filter(item -> item.getMenu() != null && item.getMenu().getId() != null)
                .collect(Collectors.groupingBy(item -> item.getMenu().getId()));

        for (Menu menu : menus) {
            List<MenuItem> items = itemsByMenuId.get(menu.getId());
            if (items != null && !items.isEmpty()) {
                menu.setItems(new HashSet<>(items));
            } else {
                menu.setItems(Collections.emptySet());
            }
        }
    }

    public void fetchAndAttachVariants(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) return;

        Set<Long> menuIds = menus.stream()
                .map(Menu::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new)); // keep order if desired

        // fetch all variants associated with those menu ids
        List<MenuVariant> variants = menuVariantRepository.findByMenus_IdIn(menuIds);

        // build map menuId -> Set<MenuVariant>
        Map<Long, Set<MenuVariant>> variantsByMenuId = new HashMap<>();
        for (MenuVariant v : variants) {
            // v.getMenus() is likely lazy, but we're in a transactional method so it's safe to iterate
            for (Menu m : v.getMenus()) {
                Long mid = m.getId();
                if (menuIds.contains(mid)) { // only map for menus we care about
                    variantsByMenuId.computeIfAbsent(mid, k -> new HashSet<>()).add(v);
                }
            }
        }

        // attach to menus
        for (Menu menu : menus) {
            Set<MenuVariant> vs = variantsByMenuId.getOrDefault(menu.getId(), Collections.emptySet());
            menu.setVariants(vs);
        }
    }

}
