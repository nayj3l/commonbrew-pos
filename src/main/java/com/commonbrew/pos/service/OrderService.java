package com.commonbrew.pos.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commonbrew.pos.constants.PaymentOption;
import com.commonbrew.pos.dto.OrderConfirmSummary;
import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.MenuVariant;
import com.commonbrew.pos.model.Order;
import com.commonbrew.pos.model.OrderItem;
import com.commonbrew.pos.repository.AddonRepository;
import com.commonbrew.pos.repository.MenuItemRepository;
import com.commonbrew.pos.repository.MenuRepository;
import com.commonbrew.pos.repository.MenuVariantRepository;
import com.commonbrew.pos.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuVariantRepository variantRepository;
    private final AddonRepository addonRepository;

    @Transactional
    public Order createOrder(
            List<Long> itemIds,
            List<Long> variantIds,
            List<Integer> quantities,
            List<List<Long>> addonIdsList, // optional: list of addon IDs per variant
            List<List<Integer>> addonQuantitiesList, // optional: quantities of addons per variant
            PaymentOption paymentOption,
            String unpaidReason,
            String barista) {

        // Create new order
        Order order = new Order();
        order.setOrderTime(LocalDateTime.now());
        order.setPaymentOption(paymentOption);
        order.setUnpaidReason(paymentOption == PaymentOption.UNPAID ? unpaidReason : null);
        order.setBarista(barista);

        double totalAmount = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            Long variantId = variantIds.get(i);
            Integer quantity = quantities.get(i);

            MenuItem menuItem = menuItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("MenuItem not found: " + itemId));

            Menu menu = menuRepository.findMenuByMenuItemId(itemId)
                    .orElseThrow(() -> new RuntimeException("Menu not found for MenuItem: " + itemId));

            // Get variant
            MenuVariant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));

            // Create main order item (the chosen variant)
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(menuItem);
            orderItem.setVariant(variant);
            orderItem.setQuantity(quantity);
            orderItem.setVariantNameSnapshot(variant.getVariantName());
            orderItem.setUnitPriceSnapshot(variant.getPrice());
            orderItem.setMenuItemNameSnapshot("(" + menu.getName() + ") " + menuItem.getName());
            orderItem.setSubtotal(variant.getPrice() * quantity);
            orderItem.setOrder(order);
            orderItems.add(orderItem);

            totalAmount += orderItem.getSubtotal();

            // Handle optional addons for this variant
            if (addonIdsList != null && addonIdsList.size() > i && addonIdsList.get(i) != null) {
                List<Long> addonIds = addonIdsList.get(i);
                List<Integer> addonQuantities = addonQuantitiesList.get(i);

                for (int j = 0; j < addonIds.size(); j++) {
                    Long addonId = addonIds.get(j);
                    Integer addonQty = addonQuantities.get(j);

                    Addon addon = addonRepository.findById(addonId)
                            .orElseThrow(() -> new RuntimeException("Addon not found: " + addonId));

                    OrderItem addonItem = new OrderItem();
                    addonItem.setVariant(variant);
                    addonItem.setQuantity(addonQty);
                    addonItem.setVariantNameSnapshot(addon.getAddonName());
                    addonItem.setUnitPriceSnapshot(addon.getPrice());
                    addonItem.setMenuItemNameSnapshot("Addon");
                    addonItem.setSubtotal(addon.getPrice() * addonQty);
                    addonItem.setOrder(order);

                    orderItems.add(addonItem);
                    totalAmount += addonItem.getSubtotal();
                }
            }
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersBetween(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findByOrderTimeBetweenOrderByOrderTimeDesc(from, to);
    }

    public List<Order> getOrdersForToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(LocalTime.MAX);
        return getOrdersBetween(from, to);
    }

    public List<Order> getOrdersByDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(LocalTime.MAX);

        log.info("Converting date range to LocalDateTime:");
        log.info("fromDate: {} -> startDateTime: {}", fromDate, startDateTime);
        log.info("toDate: {} -> endDateTime: {}", toDate, endDateTime);
        log.info("Querying database for orders between {} and {}", startDateTime, endDateTime);

        List<Order> orders = orderRepository.findByOrderTimeBetweenOrderByOrderTimeDesc(startDateTime, endDateTime);

        log.info("Retrieved {} orders from database", orders.size());

        return orders;
    }

    public List<OrderConfirmSummary> buildOrderSummary(
            List<Integer> itemIds,
            List<Integer> variantsIds,
            List<Integer> quantities) {
        List<OrderConfirmSummary> summaries = new ArrayList<>();

        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = Long.valueOf(itemIds.get(i));
            Long variantId = Long.valueOf(variantsIds.get(i));
            int quantity = quantities.get(i);

            // Fetch MenuItem
            MenuItem item = menuItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("MenuItem not found: " + itemId));

            // Get Menu from item
            Menu menu = item.getMenu();

            // Fetch Variant
            MenuVariant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("MenuVariant not found: " + variantId));

            BigDecimal unitPrice = BigDecimal.valueOf(variant.getPrice());
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            String fullItemName = "[" + menu.getName() + "] " + item.getName();

            OrderConfirmSummary summary = OrderConfirmSummary.builder()
                    .menuName(menu.getName())
                    .itemId(item.getId())
                    .itemName(fullItemName)
                    .variantId(variantId.intValue())
                    .variantName(variant.getVariantName())
                    .quantity(quantity)
                    .price(unitPrice)
                    .totalPrice(lineTotal)
                    .build();

            summaries.add(summary);
        }

        return summaries;
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Addon getAddonById(Long addonId) {
        return addonRepository.findById(addonId)
                .orElseThrow(() -> new RuntimeException("Addon not found: " + addonId));
    }

}
