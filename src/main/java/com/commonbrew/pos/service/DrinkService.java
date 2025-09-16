package com.commonbrew.pos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.commonbrew.pos.model.Category;
import com.commonbrew.pos.model.Drink;
import com.commonbrew.pos.repository.CategoryRepository;
import com.commonbrew.pos.repository.DrinkRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DrinkService {

    private final DrinkRepository drinkRepository;
    private final CategoryRepository categoryRepository;

    public List<Drink> getAllDrinks() {
        return drinkRepository.findAll();
    }

    public List<Drink> getDrinksByCategory(Long categoryId) {
        return drinkRepository.findByCategoryCategoryId(categoryId);
    }

    public Drink getDrinkById(Long id) {
        return drinkRepository.findById(id).orElse(null);
    }

    public Drink saveDrink(Long categoryId, Drink drink) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + categoryId));
        drink.setCategory(category);
        return drinkRepository.save(drink);
    }

    public void deleteDrink(Long id) {
        drinkRepository.deleteById(id);
    }
}
