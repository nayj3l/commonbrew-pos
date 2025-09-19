package com.commonbrew.pos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commonbrew.pos.model.Menu;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    boolean existsByCode(String code);
    List<Menu> findByActiveTrue();
}
