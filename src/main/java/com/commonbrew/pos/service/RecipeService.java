package com.commonbrew.pos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.commonbrew.pos.model.Recipe;
import com.commonbrew.pos.repository.RecipeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }

    public Recipe save(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public List<Recipe> getByItemAndVariant(Long itemId, Long variantId) {
        return recipeRepository.findByItemIdAndVariantVariantId(itemId, variantId);
    }

    public void delete(Long id) {
        recipeRepository.deleteById(id);
    }
}

