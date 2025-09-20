package com.commonbrew.pos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commonbrew.pos.constants.PaymentOption;
import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.ItemVariant;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.Order;
import com.commonbrew.pos.model.OrderItem;
import com.commonbrew.pos.model.dto.OrderConfirmSummary;
import com.commonbrew.pos.repository.AddonRepository;
import com.commonbrew.pos.repository.ItemVariantRepository;
import com.commonbrew.pos.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemVariantRepository variantRepository;
    private final AddonRepository addonRepository;

    @Transactional
    public Order createOrder(
            List<Long> variantIds,
            List<Integer> quantities,
            List<List<Long>> addonIdsList,          // optional: list of addon IDs per variant
            List<List<Integer>> addonQuantitiesList,// optional: quantities of addons per variant
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

        for (int i = 0; i < variantIds.size(); i++) {
            Long variantId = variantIds.get(i);
            Integer quantity = quantities.get(i);

            // Get variant
            ItemVariant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));

            // Create main order item (the chosen variant)
            OrderItem orderItem = new OrderItem();
            orderItem.setVariant(variant);
            orderItem.setQuantity(quantity);
            orderItem.setVariantNameSnapshot(variant.getVariantName());
            orderItem.setUnitPriceSnapshot(variant.getPrice());
            orderItem.setMenuItemNameSnapshot(variant.getMenuItem().getName());
            orderItem.setSubtotal(variant.getPrice() * quantity);
            orderItem.setOrder(order);
            orderItems.add(orderItem);

            totalAmount += orderItem.getSubtotal();

            // Handle optional addons for this variant
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
        addonItem.setMenuItemNameSnapshot(addon.getAddonName() + " (Addon)");
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

    public List<OrderConfirmSummary> buildOrderSummary(
            List<Integer> itemsVariantsIds,
            List<Integer> quantities
    ) {
        List<OrderConfirmSummary> summaries = new ArrayList<>();

        for (int i = 0; i < itemsVariantsIds.size(); i++) {
            Long variantId = Long.valueOf(itemsVariantsIds.get(i));
            int quantity = quantities.get(i);

            ItemVariant itemVariant = variantRepository.getReferenceById(variantId);
            MenuItem menuItem = itemVariant.getMenuItem();

            BigDecimal unitPrice = new BigDecimal(itemVariant.getPrice());
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            OrderConfirmSummary summary = OrderConfirmSummary.builder()
                    .variantId(variantId.intValue())
                    .itemName(menuItem.getName())    
                    .variantName(itemVariant.getVariantName())
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
