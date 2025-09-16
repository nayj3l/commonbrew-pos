package com.commonbrew.pos.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.Category;
import com.commonbrew.pos.model.Drink;
import com.commonbrew.pos.model.dto.DrinkDto;
import com.commonbrew.pos.service.AddonService;
import com.commonbrew.pos.service.CategoryService;
import com.commonbrew.pos.service.DrinkService;
import com.commonbrew.pos.service.OrderService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final DrinkService drinkService;
    private final AddonService addonService;
    private final OrderService orderService;
    private final CategoryService categoryService;

    @GetMapping
    public String showOrderPage(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Drink> drinks = drinkService.getAllDrinks();
        List<Addon> addons = addonService.getAllAddons();

        model.addAttribute("categories", categories);
        model.addAttribute("drinks", drinks);
        model.addAttribute("addons", addons);

        return "order-form"; // Thymeleaf template
    }

    // Handle order submission
    @PostMapping("/submit")
    public String submitOrder(@RequestParam List<Long> drinkIds,
                              @RequestParam(required = false) List<Integer> quantities,
                              @RequestParam(required = false) List<Long> addonIds) {

        orderService.createOrder(drinkIds, quantities, addonIds);
        return "redirect:/order/summary"; // optionally show order summary
    }

    // Show past orders
    @GetMapping("/history")
    public String showOrderHistory(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "order-history";
    }

    @GetMapping("/drinks/{categoryId}")
    @ResponseBody
    public List<DrinkDto> getDrinksByCategory(@PathVariable Long categoryId) {
        List<Drink> drinks = drinkService.getDrinksByCategory(categoryId);
        return drinks.stream()
                .map(drink -> new DrinkDto(
                        drink.getDrinkId(),
                        drink.getDrinkName(),
                        drink.getBasePrice(),
                        drink.getUpsizePrice()))
                .collect(Collectors.toList());
    }

}
