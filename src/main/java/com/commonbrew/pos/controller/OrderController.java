package com.commonbrew.pos.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.Category;
import com.commonbrew.pos.model.Drink;
import com.commonbrew.pos.model.Order;
import com.commonbrew.pos.model.dto.DrinkDto;
import com.commonbrew.pos.model.dto.OrderSummary;
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

        return "order-form";
    }

    // Generate order summary for confirmation
    @PostMapping("/preview")
    @ResponseBody
    public OrderSummary previewOrder(@RequestParam List<Long> drinkIds,
                                    @RequestParam List<Integer> quantities,
                                    @RequestParam(required = false) List<Long> addonIds,
                                    @RequestParam(required = false) List<Integer> addonQuantities) {
        return orderService.buildOrderSummary(drinkIds, quantities, addonIds, addonQuantities);
    }

    // Final order submission
    @PostMapping("/submit")
    public String submitOrder(@RequestParam List<Long> drinkIds,
                            @RequestParam(required = false) List<Integer> quantities,
                            @RequestParam(required = false) List<Long> addonIds,
                            RedirectAttributes redirectAttributes) {

        Order savedOrder = orderService.createOrder(drinkIds, quantities, addonIds);

        // Pass orderId as a request parameter on redirect
        redirectAttributes.addAttribute("orderId", savedOrder.getId());
        redirectAttributes.addAttribute("totalAmount", savedOrder.getTotalAmount());

        return "redirect:/order/success";
    }

    @GetMapping("/success")
    public String showSuccessPage(@RequestParam Long orderId,
                                @RequestParam Double totalAmount,
                                Model model) {
        
        Order order = orderService.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Order not found"));

        model.addAttribute("orderId", order.getId());
        model.addAttribute("totalAmount", order.getTotalAmount());
        return "success";
    }

    @PostMapping("/confirm")
    public String confirmOrder(@RequestParam List<Long> drinkIds,
                           @RequestParam List<Integer> quantities,
                           @RequestParam(required = false) List<Long> addonIds,
                           @RequestParam(required = false) List<Integer> addonQuantities,
                           Model model) {

        // Build a summary object to pass to Thymeleaf
        OrderSummary orderSummary = orderService.buildOrderSummary(drinkIds, quantities, addonIds, addonQuantities);
        model.addAttribute("orderSummary", orderSummary);
        return "order-receipt"; // Thymeleaf template for receipt
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
