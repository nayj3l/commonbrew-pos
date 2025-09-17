package com.commonbrew.pos.controller;

import java.util.ArrayList;
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
import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.Order;
import com.commonbrew.pos.model.dto.MenuItemDto;
import com.commonbrew.pos.model.dto.OrderSummary;
import com.commonbrew.pos.service.AddonService;
import com.commonbrew.pos.service.MenuService;
import com.commonbrew.pos.service.MenuItemService;
import com.commonbrew.pos.service.OrderService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final MenuItemService itemService;
    private final AddonService addonService;
    private final OrderService orderService;
    private final MenuService menuService;

    @GetMapping
    public String showOrderPage(Model model) {
        List<Menu> menu = menuService.getAllMenu();
        List<MenuItem> menuItems = itemService.getAllItems();
        List<Addon> addons = addonService.getAllAddons();

        model.addAttribute("categories", menu);
        model.addAttribute("items", menuItems);
        model.addAttribute("addons", addons);

        return "order-form";
    }

    @PostMapping("/preview")
    @ResponseBody
    public OrderSummary previewOrder(
            @RequestParam("itemIds") List<Long> itemIds,
            @RequestParam("quantities") List<Integer> quantities,
            @RequestParam(value = "variantIds", required = false) List<Long> variantIds,
            @RequestParam(value = "itemAddonCsv", required = false) List<String> itemAddonCsv,
            @RequestParam(value = "addonIds", required = false) List<Long> orderAddonIds,
            @RequestParam(value = "addonQuantities", required = false) List<Integer> addonQuantities) {

        if (itemIds == null || quantities == null || itemIds.size() != quantities.size()) {
            throw new IllegalArgumentException("itemIds and quantities are required and must have the same length");
        }

        List<List<Long>> itemAddonIds = parseItemAddonCsv(itemAddonCsv, itemIds.size());

        return orderService.buildOrderSummary(
                itemIds,          // menuItemIds
                quantities,
                variantIds,        // optional per-item variant choices
                itemAddonIds,      // per-item addons parsed
                orderAddonIds,     // order-level addons
                addonQuantities    // order-level addon quantities
        );
    }

    // Final order submission (redirect to success page)
    @PostMapping("/submit")
    public String submitOrder(
            @RequestParam("itemIds") List<Long> itemIds,
            @RequestParam("quantities") List<Integer> quantities,
            @RequestParam(value = "variantIds", required = false) List<Long> variantIds,
            @RequestParam(value = "itemAddonCsv", required = false) List<String> itemAddonCsv,
            @RequestParam(value = "addonIds", required = false) List<Long> orderAddonIds,
            @RequestParam(value = "addonQuantities", required = false) List<Integer> addonQuantities,
            RedirectAttributes redirectAttributes) {

        if (itemIds == null || quantities == null || itemIds.size() != quantities.size()) {
            throw new IllegalArgumentException("itemIds and quantities are required and must have the same length");
        }

        List<List<Long>> itemAddonIds = parseItemAddonCsv(itemAddonCsv, itemIds.size());

        Order savedOrder = orderService.createOrder(
                itemIds,           // menuItemIds
                quantities,
                variantIds,         // optional
                itemAddonIds,       // per-item addon lists
                orderAddonIds,      // order-level addons
                addonQuantities     // order-level addon quantities
        );

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
    public String confirmOrder(
            @RequestParam("itemIds") List<Long> itemIds,
            @RequestParam("quantities") List<Integer> quantities,
            // optional: preferred to pass explicit variant IDs for each line (can be null)
            @RequestParam(value = "variantIds", required = false) List<Long> variantIds,
            // optional: per-item addons as CSV strings (one CSV string per line item).
            // e.g. itemAddonCsv = ["5,6", "", "3"] => first item has addons 5 and 6, second none, third addon 3
            @RequestParam(value = "itemAddonCsv", required = false) List<String> itemAddonCsv,
            // order-level addons (old behavior)
            @RequestParam(value = "addonIds", required = false) List<Long> orderAddonIds,
            @RequestParam(value = "addonQuantities", required = false) List<Integer> addonQuantities,
            Model model) {

        // Basic validation: make sure required lists align
        if (itemIds == null || quantities == null || itemIds.size() != quantities.size()) {
            throw new IllegalArgumentException("itemIds and quantities are required and must have the same length");
        }

        // Parse per-item addon CSVs into List<List<Long>>
        List<List<Long>> itemAddonIds = parseItemAddonCsv(itemAddonCsv, itemIds.size());

        // Call the updated service method (menuItemIds == itemIds)
        OrderSummary orderSummary = orderService.buildOrderSummary(
                itemIds,                 // menuItemIds
                quantities,
                variantIds,               // may be null
                itemAddonIds,             // per-item addons parsed
                orderAddonIds,            // order-level addons
                addonQuantities           // order-level addon quantities
        );

        model.addAttribute("orderSummary", orderSummary);
        return "order-receipt";
    }

    /**
     * Helper: convert list of CSV strings into List<List<Long>> with same size as itemsCount.
     * If itemAddonCsv is null, returns a list of nulls to indicate "no per-item addons".
     */
    private List<List<Long>> parseItemAddonCsv(List<String> itemAddonCsv, int itemsCount) {
        if (itemAddonCsv == null) {
            // keep it null for the service (service accepts null)
            return null;
        }

        List<List<Long>> parsed = new ArrayList<>(itemsCount);
        for (int i = 0; i < itemsCount; i++) {
            if (i < itemAddonCsv.size() && itemAddonCsv.get(i) != null && !itemAddonCsv.get(i).trim().isEmpty()) {
                String csv = itemAddonCsv.get(i).trim();
                String[] parts = csv.split(",");
                List<Long> ids = new ArrayList<>();
                for (String p : parts) {
                    String s = p.trim();
                    if (!s.isEmpty()) {
                        try {
                            ids.add(Long.parseLong(s));
                        } catch (NumberFormatException ex) {
                            // ignore invalid tokens (or throw if you prefer strict)
                        }
                    }
                }
                parsed.add(ids);
            } else {
                parsed.add(null); // no addons for this item
            }
        }
        return parsed;
    }


    // Show past orders
    @GetMapping("/history")
    public String showOrderHistory(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "order-history";
    }

    @GetMapping("/items/{categoryId}")
    @ResponseBody
    public List<MenuItemDto> getItemsByCategory(@PathVariable Long menuId) {
        List<MenuItem> items = itemService.getMenuItemsByMenuId(menuId);
        return items.stream()
                .map(item -> new MenuItemDto(
                        item.getId(),
                        item.getName()))
                .collect(Collectors.toList());
    }

}
