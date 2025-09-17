package com.commonbrew.pos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commonbrew.pos.model.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    // Correct property path: menu.id
    List<MenuItem> findByMenuId(Long menuId);

    // If you want by menu name:
    List<MenuItem> findByMenuName(String menuName);
}
