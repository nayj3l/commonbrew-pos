package com.commonbrew.pos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.commonbrew.pos.model.Menu;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    boolean existsByCode(String code);

    List<Menu> findByActiveTrue();

    Optional<Menu> findByIdAndActiveTrue(Long id);

   @Query("SELECT m FROM Menu m WHERE m.active = true")
    List<Menu> findAllActiveMenus();

    @Query("SELECT DISTINCT m FROM Menu m " +
       "LEFT JOIN FETCH m.items i " +
       "LEFT JOIN FETCH m.variants v " +
       "WHERE m.active = true and m.id = :id")
    Optional<Menu> findAllActiveMenusById(@Param("id") Long id);

    @Query("SELECT m FROM Menu m " +
           "JOIN m.items i " +
           "WHERE i.id = :menuItemId")
    Optional<Menu> findMenuByMenuItemId(@Param("menuItemId") Long menuItemId);

}
