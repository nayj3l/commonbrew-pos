package com.commonbrew.pos.repository;

import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.Menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddonRepository extends JpaRepository<Addon, Long> {
    
    // Fetch addons for specific menus
    @Query("SELECT DISTINCT a FROM Addon a LEFT JOIN FETCH a.menu m WHERE m IN :menus")
    List<Addon> findAddonsByMenus(@Param("menus") List<Menu> menus);
}
