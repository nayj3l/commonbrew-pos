package com.commonbrew.pos.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.commonbrew.pos.dto.AddonConfirmSummary;
import com.commonbrew.pos.dto.AddonResponse;
import com.commonbrew.pos.dto.MenuItemDto;
import com.commonbrew.pos.dto.MenuItemResponse;
import com.commonbrew.pos.dto.MenuResponse;
import com.commonbrew.pos.dto.MenuVariantResponse;
import com.commonbrew.pos.dto.OrderConfirmSummary;
import com.commonbrew.pos.dto.OrderConfirmSummaryResponse;
import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.Order;
import com.commonbrew.pos.service.AddonService;
import com.commonbrew.pos.service.MenuItemService;
import com.commonbrew.pos.service.MenuService;
import com.commonbrew.pos.service.MenuVariantService;
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
    private final MenuVariantService variantService;

    @GetMapping
    public String showOrderPage(Model model) {
        List<MenuResponse> menu = menuService.getAllMenu();
        List<MenuItemResponse> menuItems = itemService.getAllItems();
        List<AddonResponse> addons = addonService.getAllAddons();
        List<MenuVariantResponse> variants = variantService.getAllVariants();

        model.addAttribute("menu", menu);
        model.addAttribute("items", menuItems);
        model.addAttribute("variants", variants);
        model.addAttribute("addons", addons);

        return "order";
    }

    // @GetMapping("/items/{itemId}/variants")
    // @ResponseBody
    // public List<MenuVariantDto> getVariantsByMenuItem(@PathVariable Long itemId)
    // {
    // MenuItem item = itemService.getItemById(itemId);

    // return item.getVariants().stream()
    // .map(v -> new MenuVariantDto(v.getVariantId(), v.getMenuItem().getId(),
    // v.getVariantName(), v.getPrice()))
    // .collect(Collectors.toList());
    // }

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
            @RequestParam("itemId") List<Long> itemIds,
            @RequestParam("variantId") List<Long> variantIds,
            @RequestParam("quantity") List<Integer> quantities,
            @RequestParam(required = false) List<Long> addonItemIds,
            @RequestParam(required = false) List<Long> addonIds,
            @RequestParam(required = false) List<Integer> addonQuantities,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String unpaidReason,
            RedirectAttributes redirectAttributes) {

        log.info("Received itemIds: {}", itemIds);
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

        if (itemIds == null || variantIds == null || quantities == null) {
            throw new IllegalArgumentException("itemIds, variantIds, and quantities are all required");
        }

        if (itemIds.isEmpty() || variantIds.isEmpty() || quantities.isEmpty()) {
            throw new IllegalArgumentException("itemIds, variantIds, and quantities cannot be empty");
        }

        if (itemIds.size() != variantIds.size() || itemIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Each item must have a corresponding variant and quantity");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String barista = authentication.getName();

        PaymentOption paymentOption = PaymentOption.valueOf(paymentMethod.toUpperCase());

        List<List<Long>> addonIdsList = new ArrayList<>();
        List<List<Integer>> addonQuantitiesList = new ArrayList<>();

        addonIdsList.add(addonIds);
        addonQuantitiesList.add(addonQuantities);

        Order savedOrder = orderService.createOrder(
                itemIds,
                variantIds,
                quantities,
                addonIdsList,
                addonQuantitiesList,
                paymentOption,
                unpaidReason,
                barista);

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
            @RequestParam List<Integer> itemIds,
            @RequestParam List<Integer> variantsIds,
            @RequestParam List<Integer> quantities,
            @RequestParam(required = false) List<Integer> addonIds,
            @RequestParam(required = false) List<Integer> addonQuantities,
            Model model) {

        log.info("Received itemIds : {}", itemIds);
        log.info("Received variantsIds : {}", variantsIds);
        log.info("Received quantities: {}", quantities);
        log.info("Received addonIds: {}", addonIds);
        log.info("Received addonQuantities: {}", addonQuantities);

        if (itemIds == null) {
            itemIds = new ArrayList<>();
        }

        if (addonIds == null) {
            addonIds = new ArrayList<>();
        }
        if (addonQuantities == null) {
            addonQuantities = new ArrayList<>();
        }

        if (variantsIds == null || quantities == null || variantsIds.size() != quantities.size()) {
            throw new IllegalArgumentException("itemIds and quantities are required and must have the same length");
        }

        List<OrderConfirmSummary> items = orderService.buildOrderSummary(itemIds, variantsIds, quantities);
        List<AddonConfirmSummary> addons = new ArrayList<>();
        if (addonIds != null) {
            for (int i = 0; i < addonIds.size(); i++) {
                int itemId = itemIds.get(0); // all addons belong to first item
                int addonId = addonIds.get(i);
                int quantity = addonQuantities.get(i);

                Addon addon = orderService.getAddonById(Long.valueOf(addonId));
                addons.add(new AddonConfirmSummary(
                        addon.getAddonName(),
                        (long) itemId,
                        (long) addonId,
                        "Addon",
                        quantity,
                        BigDecimal.valueOf(addon.getPrice()),
                        BigDecimal.valueOf(addon.getPrice() * quantity)));
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
    public String showOrderHistory(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            Model model) {

        List<Order> orders;

        if (fromDate != null && toDate != null) {
            LocalDateTime from = fromDate.atStartOfDay();
            LocalDateTime to = toDate.atTime(LocalTime.MAX);
            orders = orderService.getOrdersBetween(from, to);
        } else {
            orders = orderService.getOrdersForToday();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);

        return "order-history";
    }
}
