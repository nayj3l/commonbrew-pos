package com.commonbrew.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commonbrew.pos.model.Menu;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
