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
        return menuRepository.findAll();
    }

    public Menu getMenuById(Long id) {
        return menuRepository.findById(id).orElse(null);
    }

    public Menu saveMenu(Menu menu) {
        return menuRepository.save(menu);
    }

    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }
}
