package com.commonbrew.pos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.repository.MenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    public List<Menu> getAllMenu() {
        return menuRepository.findByActiveTrue();
    }

    public Menu getMenuById(Long id) {
        return menuRepository.findById(id)
                .filter(Menu::isActive) // prevent access to deleted
                .orElseThrow(() -> new RuntimeException("Menu not found with id " + id));
    }

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

    public void softDelete(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot delete. Menu not found with id " + id));
        menu.setActive(false);
        menuRepository.save(menu);
    }

}
