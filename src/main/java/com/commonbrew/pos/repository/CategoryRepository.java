package com.commonbrew.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commonbrew.pos.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
