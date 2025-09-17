package com.commonbrew.pos.service;

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
import com.commonbrew.pos.model.dto.OrderItemDto;
import com.commonbrew.pos.model.dto.OrderSummary;
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

    // Build summary (similar idea): accept variantIds optional, fallback to first variant
    public OrderSummary buildOrderSummary(
            List<Long> menuItemIds,
            List<Integer> quantities,
            List<Long> variantIds,
            List<List<Long>> itemAddonIds,
            List<Long> orderAddonIds,
            List<Integer> orderAddonQuantities) {

        OrderSummary summary = new OrderSummary();
        List<OrderItemDto> items = new ArrayList<>();
        List<OrderItemDto> addons = new ArrayList<>();
        double total = 0.0;

        if (menuItemIds != null && quantities != null) {
            for (int i = 0; i < menuItemIds.size(); i++) {
                Long menuItemId = menuItemIds.get(i);
                int qty = quantities.get(i);

                ItemVariant variant = null;
                if (variantIds != null && variantIds.size() > i && variantIds.get(i) != null) {
                    variant = variantRepository.findById(variantIds.get(i)).orElse(null);
                } else {
                    MenuItem mi = menuItemRepository.findById(menuItemId).orElse(null);
                    if (mi != null && mi.getVariants() != null && !mi.getVariants().isEmpty()) {
                        variant = mi.getVariants().get(0);
                    }
                }

                if (variant != null) {
                    double price = variant.getPrice() * qty;
                    total += price;
                    items.add(new OrderItemDto(variant.getVariantName(), qty, price));
                }
                // item-level addons summary
                if (itemAddonIds != null && itemAddonIds.size() > i && itemAddonIds.get(i) != null) {
                    for (Long aid : itemAddonIds.get(i)) {
                        Addon a = addonRepository.findById(aid).orElse(null);
                        if (a != null) {
                            double ap = a.getPrice() * 1 * qty; // multiply by qty of item if addon follows each item
                            total += ap;
                            addons.add(new OrderItemDto(a.getAddonName(), qty, ap));
                        }
                    }
                }
            }
        }

        // order-level addons
        if (orderAddonIds != null) {
            for (int j = 0; j < orderAddonIds.size(); j++) {
                Long addonId = orderAddonIds.get(j);
                int aq = (orderAddonQuantities != null && orderAddonQuantities.size() > j) ? orderAddonQuantities.get(j) : 1;
                Addon a = addonRepository.findById(addonId).orElse(null);
                if (a != null) {
                    double ap = a.getPrice() * aq;
                    total += ap;
                    addons.add(new OrderItemDto(a.getAddonName(), aq, ap));
                }
            }
        }

        summary.setItems(items);
        summary.setAddons(addons);
        summary.setTotal(total);

        // keep references for reuse like before (convenience)
        summary.setItemIds(menuItemIds);
        summary.setQuantities(quantities);
        // flattening itemAddonIds is out-of-scope for this DTO; keep old fields for compatibility:
        summary.setAddonIds(orderAddonIds);
        // For addon quantities we store order-level only:
        summary.setAddonQuantities(orderAddonQuantities);

        return summary;
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
}
