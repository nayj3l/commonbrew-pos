package com.commonbrew.pos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.commonbrew.pos.model.Ingredient;
import com.commonbrew.pos.model.Recipe;
import com.commonbrew.pos.service.IngredientService;
import com.commonbrew.pos.service.MenuItemService;
import com.commonbrew.pos.service.MenuVariantService;
import com.commonbrew.pos.service.RecipeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final IngredientService ingredientService;
    private final MenuItemService menuItemService;
    private final MenuVariantService menuVariantService;
    private final RecipeService recipeService;
    
    @GetMapping
    public String inventoryHome() {
        return "inventory/inventory";
    }

    @GetMapping("/ingredients")
    public String showIngredients(Model model) {
        model.addAttribute("ingredients", ingredientService.getAllActive());
        model.addAttribute("newIngredient", new Ingredient());
        return "inventory/inventory-ingredients";
    }

    // ADD Ingredients Page Form
    @GetMapping("/ingredients/add")
    public String showAddIngredientForm(Model model) {
        model.addAttribute("ingredient", new Ingredient());
        return "inventory/add-ingredient";
    }

    // ADD Ingredients SUBMISSION
    @PostMapping("/ingredients/save")
    public String addIngredient(@ModelAttribute Ingredient ingredient) {
        ingredientService.save(ingredient);
        return "redirect:/inventory/ingredients";
    }

    // UPDATE Ingredients
    @PostMapping("/ingredients/update")
    @ResponseBody
    public ResponseEntity<String> updateIngredient(@RequestBody Ingredient ingredient) {
        log.info("=== [UPDATE INGREDIENT] Request received ===");
        log.info("Payload: {}", ingredient);

        try {
            ingredientService.save(ingredient);
            log.info("Ingredient updated successfully: {}", ingredient.getId());
            return ResponseEntity.ok("Updated successfully");
        } catch (Exception e) {
            log.error("Error updating ingredient: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to update ingredient.");
        }
    }

    // NEW: fetch single ingredient by id as JSON (used by edit modal)
    @GetMapping("/ingredients/{id}")
    @ResponseBody
    public ResponseEntity<Ingredient> getIngredientById(@PathVariable Long id) {
        Ingredient ingredient = ingredientService.getById(id);
        return ResponseEntity.ok(ingredient);
    }

    // Edit Ingredients
    @GetMapping("/ingredients/edit/{id}")
    public String showEditIngredientForm(@PathVariable Long id, Model model) {
        Ingredient ingredient = ingredientService.getById(id);
        if (ingredient == null) {
            throw new RuntimeException("Ingredient not found with ID: " + id);
        }

        model.addAttribute("ingredient", ingredient);
        return "inventory/edit-ingredient";
    }

    @DeleteMapping("/ingredients/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteIngredient(@PathVariable Long id) {
        ingredientService.deactivate(id);
        return ResponseEntity.ok("Deleted successfully");
    }

    @GetMapping("/recipes")
    public String showRecipes(Model model) {
        model.addAttribute("recipes", recipeService.findAll());
        return "inventory/inventory-recipes";
    }

    @GetMapping("/list")
    public String listRecipes(Model model) {
        model.addAttribute("recipes", recipeService.findAll());
        return "inventory-recipes-list";
    }

    @GetMapping("/new")
    public String newRecipe(Model model) {
        model.addAttribute("recipe", new Recipe());
        model.addAttribute("items", menuItemService.getAllActiveEntities());
        model.addAttribute("variants", menuVariantService.findAllActiveVariants());
        model.addAttribute("ingredients", ingredientService.getAllActive());
        return "inventory-recipes-new";
    }

    @PostMapping("/save")
    public String saveRecipe(@ModelAttribute Recipe recipe) {
        recipeService.save(recipe);
        return "redirect:/inventory/recipes/list";
    }
}

