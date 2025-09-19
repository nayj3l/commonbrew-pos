package com.commonbrew.pos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.commonbrew.pos.model.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    @Query("SELECT m FROM MenuItem m WHERE m.menu.id = :menuId AND m.active = true")
    List<MenuItem> findByMenuId(Long menuId);

    @Query("SELECT m FROM MenuItem m WHERE m.active = true")
    List<MenuItem> findAllActive();

    List<MenuItem> findByMenuName(String menuName);
}
