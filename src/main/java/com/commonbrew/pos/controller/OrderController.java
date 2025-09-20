package com.commonbrew.pos.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

import com.commonbrew.pos.constants.PaymentOption;
import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.Order;
import com.commonbrew.pos.model.dto.AddonConfirmSummary;
import com.commonbrew.pos.model.dto.ItemVariantDto;
import com.commonbrew.pos.model.dto.MenuItemDto;
import com.commonbrew.pos.model.dto.OrderConfirmSummary;
import com.commonbrew.pos.model.dto.OrderConfirmSummaryResponse;
import com.commonbrew.pos.service.AddonService;
import com.commonbrew.pos.service.MenuItemService;
import com.commonbrew.pos.service.MenuService;
import com.commonbrew.pos.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
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

        model.addAttribute("menu", menu);
        model.addAttribute("items", menuItems);
        model.addAttribute("addons", addons);

        return "order";
    }

    @GetMapping("/items/{itemId}/variants")
    @ResponseBody
    public List<ItemVariantDto> getVariantsByMenuItem(@PathVariable Long itemId) {
        MenuItem item = itemService.getItemById(itemId);

        return item.getVariants().stream()
            .map(v -> new ItemVariantDto(v.getVariantId(), v.getMenuItem().getId(), v.getVariantName(), v.getPrice()))
            .collect(Collectors.toList());
    }

    @GetMapping("/items/{menuId}")
    @ResponseBody
    public List<MenuItemDto> getItemsByCategory(@PathVariable Long menuId) {
        List<MenuItem> items = itemService.getMenuItemsByMenuId(menuId);
        return items.stream()
                .map(item -> new MenuItemDto(
                        item.getId(),
                        item.getName()))
                .collect(Collectors.toList());
    }

    // Final order submission (redirect to success page)
    @PostMapping("/submit")
    public String submitOrder(
            @RequestParam("variantId") List<Long> variantIds,
            @RequestParam("quantity") List<Integer> quantities,
            @RequestParam(value = "addonItemIds", required = false) List<Long> addonItemIds,
            @RequestParam(value = "addonIds", required = false) List<Long> addonIds,
            @RequestParam(value = "addonQuantities", required = false) List<Integer> addonQuantities,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "unpaidReason", required = false) String unpaidReason,
            RedirectAttributes redirectAttributes) {

    log.info("Received variantIds: {}", variantIds);
    log.info("Received quantities: {}", quantities);
    log.info("Received addonItemIds: {}", addonItemIds);
    log.info("Received addonIds: {}", addonIds);
    log.info("Received addonQuantities: {}", addonQuantities);
    log.info("Payment method: {}", paymentMethod);
    log.info("Unpaid reason: {}", unpaidReason);

        if (addonItemIds == null) addonItemIds = new ArrayList<>();
        if (addonIds == null) addonIds = new ArrayList<>();
        if (addonQuantities == null) addonQuantities = new ArrayList<>();

        if (variantIds == null || quantities == null || variantIds.size() != quantities.size()) {
            throw new IllegalArgumentException("variantIds and quantities are required and must have the same length");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String barista = authentication.getName();

        PaymentOption paymentOption = PaymentOption.valueOf(paymentMethod.toUpperCase());

        List<List<Long>> addonIdsList = new ArrayList<>();
        List<List<Integer>> addonQuantitiesList = new ArrayList<>();

        addonIdsList.add(addonIds);
        addonQuantitiesList.add(addonQuantities);

        Order savedOrder = orderService.createOrder(
                variantIds,
                quantities,
                addonIdsList,
                addonQuantitiesList,
                paymentOption,
                unpaidReason,
                barista
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
        return "order-success";
    }

    @PostMapping("/confirm")
    public String confirmOrder(
            @RequestParam("itemsVariantsIds") List<Integer> itemsVariantsIds,
            @RequestParam("quantities") List<Integer> quantities,
            @RequestParam(value = "addonItemIds", required = false) List<Integer> addonItemIds,
            @RequestParam(value = "addonIds", required = false) List<Integer> addonIds,
            @RequestParam(value = "addonQuantities", required = false) List<Integer> addonQuantities,
            Model model) {
                
        log.info("Received itemIds: {}", itemsVariantsIds);
        log.info("Received quantities: {}", quantities);

        if (addonItemIds == null) {
            addonItemIds = new ArrayList<>();
        }
        if (addonIds == null) {
            addonIds = new ArrayList<>();
        }
        if (addonQuantities == null) {
            addonQuantities = new ArrayList<>();
        }

        if (itemsVariantsIds == null || quantities == null || itemsVariantsIds.size() != quantities.size()) {
            throw new IllegalArgumentException("itemIds and quantities are required and must have the same length");
        }

        List<OrderConfirmSummary> items = orderService.buildOrderSummary(itemsVariantsIds, quantities);
        List<AddonConfirmSummary> addons = new ArrayList<>();
        if (addonIds != null) {
            for (int i = 0; i < addonIds.size(); i++) {
                int itemId = addonItemIds.get(i);
                int addonId = addonIds.get(i);
                int quantity = addonQuantities.get(i);

                Addon addon = orderService.getAddonById(Long.valueOf(addonId));
                addons.add(new AddonConfirmSummary(
                    addon.getAddonName(),                  // Use addon name here
                    (long) itemId,                         // parent item id is okay
                    (long) addonId,
                    "Addon",                                   // variant name placeholder
                    quantity,
                    BigDecimal.valueOf(addon.getPrice()),
                    BigDecimal.valueOf(addon.getPrice() * quantity)
                ));
            }
        }

        BigDecimal total = items.stream()
            .map(OrderConfirmSummary::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .add(addons.stream().map(AddonConfirmSummary::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add));

        model.addAttribute("orderConfirmSummary", new OrderConfirmSummaryResponse(items, addons, total));
        return "order-confirm";
    }

    @GetMapping("/history")
    public String showOrderHistory(Model model) {
        List<Order> orders = orderService.getAllOrders()
                                        .stream()
                                        .sorted((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime()))
                                        .toList(); // descending by orderTime
        model.addAttribute("orders", orders);
        return "order-history";
    }

}
