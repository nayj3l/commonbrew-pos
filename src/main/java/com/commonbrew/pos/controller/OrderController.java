package com.commonbrew.pos.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

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
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.Order;
import com.commonbrew.pos.service.MenuItemService;
import com.commonbrew.pos.service.MenuService;
import com.commonbrew.pos.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final MenuItemService itemService;
    private final OrderService orderService;
    private final MenuService menuService;

    @GetMapping
    public String showOrderPage(Model model) {
        List<MenuResponse> menu = menuService.getAllMenu().getMenus();

        List<MenuItemResponse> menuItems = menu.stream()
                .flatMap(m -> m.getItems().stream())
                .toList();

        List<AddonResponse> addons = menu.stream()
                .flatMap(m -> m.getAddons().stream())
                .toList();

        List<MenuVariantResponse> variants = menu.stream()
                .flatMap(m -> m.getVariants().stream())
                .toList();

        model.addAttribute("menu", menu);
        model.addAttribute("items", menuItems);
        model.addAttribute("variants", variants);
        model.addAttribute("addons", addons);

        return "order";
    }

    @GetMapping("/items/{menuId}")
    @ResponseBody
    public List<MenuItemDto> getItemsByCategory(@PathVariable Long menuId) {
        List<MenuItem> items = itemService.getMenuItemsByMenuId(menuId);
        return items.stream()
                .map(item -> new MenuItemDto(
                        item.getId(),
                        item.getName()))
                .toList();
    }

    // Final order submission (redirect to success page)
    @PostMapping("/submit")
    public String submitOrder(
            @RequestParam String orderJson,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String unpaidReason,
            RedirectAttributes redirectAttributes) {

        ObjectMapper objectMapper = new ObjectMapper();
        OrderConfirmSummaryResponse orderConfirmSummary;

        try {
            orderConfirmSummary = objectMapper.readValue(orderJson, OrderConfirmSummaryResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse order JSON: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid order data format", e);
        }

        List<OrderConfirmSummary> items = orderConfirmSummary.getItems();

        List<Long> itemIds = items.stream()
                .map(OrderConfirmSummary::getItemId)
                .toList();

        List<Long> variantIds = items.stream()
                .map(OrderConfirmSummary::getVariantId)
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .toList();

        List<Integer> quantities = items.stream()
                .map(OrderConfirmSummary::getQuantity)
                .toList();

        List<AddonConfirmSummary> addons = orderConfirmSummary.getAddons();

        List<Long> addonIds = addons.stream()
                .map(AddonConfirmSummary::getAddonId)
                .toList();

        List<Integer> addonQuantities = addons.stream()
                .map(AddonConfirmSummary::getQuantity)
                .toList();

        log.info("Received itemIds: {}", itemIds);
        log.info("Received variantIds: {}", variantIds);
        log.info("Received quantities: {}", quantities);
        log.info("Received addonIds: {}", addonIds);
        log.info("Received addonQuantities: {}", addonQuantities);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String barista = authentication.getName();

        PaymentOption paymentOption = PaymentOption.valueOf(paymentMethod.toUpperCase());

        Order savedOrder = orderService.createOrder(
                itemIds,
                variantIds,
                quantities,
                addonIds,
                addonQuantities,
                paymentOption,
                unpaidReason,
                barista);

        redirectAttributes.addAttribute("orderId", savedOrder.getId());
        redirectAttributes.addAttribute("totalAmount", savedOrder.getTotalAmount());
        log.info("✅ Order created successfully with ID: {}", savedOrder.getId());
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

    @GetMapping("/confirm")
    public String confirmOrder(Model model) {
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
