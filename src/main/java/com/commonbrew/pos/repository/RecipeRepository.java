package com.commonbrew.pos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.commonbrew.pos.model.Recipe;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByItemIdAndVariantVariantId(Long itemId, Long variantId);
}

