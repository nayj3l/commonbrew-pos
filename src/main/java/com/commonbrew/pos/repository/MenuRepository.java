package com.commonbrew.pos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.commonbrew.pos.model.Menu;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    boolean existsByCode(String code);

    List<Menu> findByActiveTrue();

    // Fetch only menus with items (no variants or addons)
    @Query("SELECT DISTINCT m FROM Menu m " +
           "LEFT JOIN FETCH m.items i " +
           "WHERE m.active = true")
    List<Menu> findAllActiveMenusWithItems();
}
