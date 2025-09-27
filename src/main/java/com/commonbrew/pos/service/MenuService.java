package com.commonbrew.pos.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.commonbrew.pos.mapper.MenuMapper;
import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.dto.MenuResponse;
import com.commonbrew.pos.repository.MenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    @Cacheable("menus")
    public List<Menu> getAllMenu() {
        return menuRepository.findByActiveTrue();
    }

    @Cacheable("menuResponses")
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

}
