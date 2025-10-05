package com.commonbrew.pos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.commonbrew.pos.model.Ingredient;
import com.commonbrew.pos.repository.IngredientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public List<Ingredient> getAllActive() {
        return ingredientRepository.findAllByActiveTrueOrderByNameAsc();
    }

    public Ingredient save(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }

    public Ingredient getById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found: " + id));
    }

    public void deactivate(Long id) {
        Ingredient ing = getById(id);
        ing.setActive(false);
        ingredientRepository.save(ing);
    }

}
