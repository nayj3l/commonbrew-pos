package com.commonbrew.pos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.ItemVariant;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.Order;
import com.commonbrew.pos.model.OrderItem;
import com.commonbrew.pos.model.OrderItemAddon;
import com.commonbrew.pos.model.dto.OrderConfirmSummary;
import com.commonbrew.pos.repository.AddonRepository;
import com.commonbrew.pos.repository.ItemVariantRepository;
import com.commonbrew.pos.repository.MenuItemRepository;
import com.commonbrew.pos.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final ItemVariantRepository variantRepository;
    private final AddonRepository addonRepository;

    @Transactional
    public Order createOrder(
            List<Long> menuItemIds,
            List<Integer> quantities,
            List<Long> variantIds,            // nullable or elements can be null
            List<List<Long>> itemAddonIds,    // per-item addon ids (nullable)
            List<Long> orderAddonIds,         // addons applied to whole order (nullable)
            List<Integer> orderAddonQuantities // quantities for order-level addons (nullable)
    ) {
        if (menuItemIds == null || quantities == null || menuItemIds.size() != quantities.size()) {
            throw new IllegalArgumentException("menuItemIds and quantities are required and must match in length");
        }

        Order order = new Order();
        order.setOrderTime(LocalDateTime.now());
        order.setItems(new ArrayList<>());

        double totalAmount = 0.0;

        for (int i = 0; i < menuItemIds.size(); i++) {
            Long menuItemId = menuItemIds.get(i);
            int qty = quantities.get(i);

            // determine variant: prefer variantIds[i] if present, otherwise pick first variant of menu item
            ItemVariant chosenVariant = null;
            if (variantIds != null && variantIds.size() > i && variantIds.get(i) != null) {
                final Long variantId = variantIds.get(i); 
                chosenVariant = variantRepository.findById(variantIds.get(i))
                        .orElseThrow(() -> new RuntimeException("Variant not found: " + variantIds.get(variantId.intValue())));
            } else {
                MenuItem menuItem = menuItemRepository.findById(menuItemId)
                        .orElseThrow(() -> new RuntimeException("MenuItem not found: " + menuItemId));
                List<ItemVariant> variants = menuItem.getVariants();
                if (variants == null || variants.isEmpty()) {
                    throw new RuntimeException("No variants found for menu item: " + menuItemId);
                }
                chosenVariant = variants.get(0); // pick first/default
            }

            // build OrderItem and snapshot price/name
            OrderItem item = new OrderItem();
            item.setOrder(order);

            // assuming OrderItem has a field `variant` (ItemVariant)
            item.setVariant(chosenVariant);
            item.setVariantNameSnapshot(chosenVariant.getVariantName());
            item.setUnitPriceSnapshot(chosenVariant.getPrice());
            item.setQuantity(qty);

            // compute per-item addons (if provided)
            double addonsTotalForItem = 0.0;
            if (itemAddonIds != null && itemAddonIds.size() > i && itemAddonIds.get(i) != null) {
                List<Long> addonIdsForThisItem = itemAddonIds.get(i);
                List<OrderItemAddon> itemAddons = new ArrayList<>();
                for (Long aId : addonIdsForThisItem) {
                    Addon addon = addonRepository.findById(aId)
                            .orElseThrow(() -> new RuntimeException("Addon not found: " + aId));
                    OrderItemAddon oia = new OrderItemAddon();
                    oia.setOrderItem(item);
                    oia.setAddon(addon);
                    oia.setAddonNameSnapshot(addon.getAddonName());
                    oia.setAddonPriceSnapshot(addon.getPrice());
                    oia.setQuantity(1);
                    itemAddons.add(oia);

                    addonsTotalForItem += addon.getPrice() * 1;
                }
                item.setSelectedAddons(itemAddons); // assume OrderItem has this list
            }

            double itemTotal = (chosenVariant.getPrice() * qty) + (addonsTotalForItem * qty);
            item.setSubtotal(itemTotal);
            totalAmount += itemTotal;

            order.getItems().add(item);
        }

        // Process order-level addons (not attached to a specific item)
        if (orderAddonIds != null && !orderAddonIds.isEmpty()) {
            for (int j = 0; j < orderAddonIds.size(); j++) {
                Long addonId = orderAddonIds.get(j);
                int aQty = (orderAddonQuantities != null && orderAddonQuantities.size() > j)
                        ? orderAddonQuantities.get(j) : 1;

                Addon addon = addonRepository.findById(addonId)
                        .orElseThrow(() -> new RuntimeException("Addon not found: " + addonId));

                // Option A: represent order-level addons as separate OrderItem entries (simple)
                OrderItem addonOrderItem = new OrderItem();
                addonOrderItem.setOrder(order);
                addonOrderItem.setVariant(null); // not tied to variant
                addonOrderItem.setVariantNameSnapshot(addon.getAddonName());
                addonOrderItem.setUnitPriceSnapshot(addon.getPrice());
                addonOrderItem.setQuantity(aQty);
                addonOrderItem.setSubtotal(addon.getPrice() * aQty);
                order.getItems().add(addonOrderItem);

                totalAmount += addon.getPrice() * aQty;
            }
        }

        order.setTotalAmount(totalAmount);
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
}
