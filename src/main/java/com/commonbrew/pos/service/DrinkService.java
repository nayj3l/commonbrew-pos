package com.commonbrew.pos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.commonbrew.pos.model.Drink;
import com.commonbrew.pos.repository.DrinkRepository;

@Service
public class DrinkService {

    private final DrinkRepository drinkRepository;

    public DrinkService(DrinkRepository drinkRepository) {
        this.drinkRepository = drinkRepository;
    }

    public List<Drink> getAllDrinks() {
        return drinkRepository.findAll();
    }

    public List<Drink> getDrinksByCategory(Long categoryId) {
        return drinkRepository.findByCategoryCategoryId(categoryId);
    }

    public Drink getDrinkById(Long id) {
        return drinkRepository.findById(id).orElse(null);
    }

    public Drink saveDrink(Drink drink) {
        return drinkRepository.save(drink);
    }

    public void deleteDrink(Long id) {
        drinkRepository.deleteById(id);
    }
}
