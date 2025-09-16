package com.commonbrew.pos.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commonbrew.pos.model.Drink;
import com.commonbrew.pos.service.DrinkService;

@RestController
@RequestMapping("/api/drinks")
public class DrinkController {

    private final DrinkService drinkService;

    public DrinkController(DrinkService drinkService) {
        this.drinkService = drinkService;
    }

    @GetMapping
    public List<Drink> getAllDrinks() {
        return drinkService.getAllDrinks();
    }

    @GetMapping("/category/{categoryId}")
    public List<Drink> getDrinksByCategory(@PathVariable Long categoryId) {
        return drinkService.getDrinksByCategory(categoryId);
    }

    @GetMapping("/{id}")
    public Drink getDrinkById(@PathVariable Long id) {
        return drinkService.getDrinkById(id);
    }

    @PostMapping
    public Drink createDrink(@RequestBody Drink drink) {
        return drinkService.saveDrink(drink);
    }

    @DeleteMapping("/{id}")
    public void deleteDrink(@PathVariable Long id) {
        drinkService.deleteDrink(id);
    }
}
