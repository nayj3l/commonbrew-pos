package com.commonbrew.pos.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.commonbrew.pos.model.Category;
import com.commonbrew.pos.model.Drink;
import com.commonbrew.pos.service.CategoryService;
import com.commonbrew.pos.service.DrinkService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final DrinkService drinkService;

    @GetMapping()
    public String showCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "categories";
    }

    @GetMapping("/{id}")
    public String showDrinks(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id);
        List<Drink> drinks = drinkService.getDrinksByCategory(id);

        model.addAttribute("category", category);
        model.addAttribute("drinks", drinks);
        return "drinks";
    }

    // Show form to add a new drink
    @GetMapping("/{id}/drink/add")
    public String addDrinkForm(@PathVariable Long id, Model model) {
        model.addAttribute("categoryId", id);
        model.addAttribute("drink", new Drink());
        return "drink-form";
    }

    // Save new drink
    @PostMapping("/{id}/drink/save")
    public String saveDrink(@PathVariable Long id, @ModelAttribute Drink drink) {
        drinkService.saveDrink(id, drink);
        return "redirect:/category/" + id;
    }

    // Show form to edit a drink
    @GetMapping("/{categoryId}/drink/edit/{drinkId}")
    public String editDrinkForm(@PathVariable Long categoryId, @PathVariable Long drinkId, Model model) {
        Drink drink = drinkService.getDrinkById(drinkId);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("drink", drink);
        return "drink-form";
    }

    // Delete a drink
    @GetMapping("/{categoryId}/drink/delete/{drinkId}")
    public String deleteDrink(@PathVariable Long categoryId, @PathVariable Long drinkId) {
        drinkService.deleteDrink(drinkId);
        return "redirect:/category/" + categoryId;
    }

    @GetMapping("/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "add-category";
    }

	// Handle form submission
	@PostMapping("/add")
	public String addCategory(@ModelAttribute Category category) {
		categoryService.saveCategory(category);
		return "redirect:/category"; // back to category list
	}

}
