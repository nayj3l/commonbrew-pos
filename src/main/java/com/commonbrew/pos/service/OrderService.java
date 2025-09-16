package com.commonbrew.pos.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.Drink;
import com.commonbrew.pos.model.Order;
import com.commonbrew.pos.model.OrderItem;
import com.commonbrew.pos.model.dto.OrderItemDto;
import com.commonbrew.pos.model.dto.OrderSummary;
import com.commonbrew.pos.repository.AddonRepository;
import com.commonbrew.pos.repository.DrinkRepository;
import com.commonbrew.pos.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DrinkRepository drinkRepository;
    private final AddonRepository addonRepository;

    @Transactional
    public Order createOrder(List<Long> drinkIds, List<Integer> quantities, List<Long> addonIds) {
        Order order = new Order();
        order.setOrderTime(LocalDateTime.now());
        order.setItems(new ArrayList<>());
        
        double totalAmount = 0.0;

        // Process drinks
        for (int i = 0; i < drinkIds.size(); i++) {
            Drink drink = drinkRepository.findById(drinkIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Drink not found"));
            
            OrderItem item = new OrderItem();
            item.setDrink(drink);
            item.setQuantity(quantities.get(i));
            item.setOrder(order);
            
            double itemTotal = drink.getBasePrice() * quantities.get(i);
            totalAmount += itemTotal;
            
            order.getItems().add(item);
        }

        // Process addons
        if (addonIds != null) {
            for (Long addonId : addonIds) {
                Addon addon = addonRepository.findById(addonId)
                        .orElseThrow(() -> new RuntimeException("Addon not found"));
                totalAmount += addon.getPrice();
            }
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public OrderSummary buildOrderSummary(List<Long> drinkIds, List<Integer> quantities, List<Long> addonIds) {
        OrderSummary summary = new OrderSummary();
        List<OrderItemDto> items = new ArrayList<>();
        List<OrderItemDto> addons = new ArrayList<>();
        double total = 0;

        // Build drinks list
        if (drinkIds != null && quantities != null) {
            for (int i = 0; i < drinkIds.size(); i++) {
                Long drinkId = drinkIds.get(i);
                int qty = quantities.get(i);

                Drink drink = drinkRepository.findById(drinkId).get();
                if (drink != null) {
                    double price = drink.getBasePrice() * qty;
                    total += price;
                    items.add(new OrderItemDto(drink.getDrinkName(), qty, price));
                }
            }
        }

        // Build addons list
        if (addonIds != null) {
            for (Long addonId : addonIds) {
                Addon addon = addonRepository.findById(addonId).get();
                if (addon != null) {
                    double price = addon.getPrice(); // assume quantity = 1 for addons
                    total += price;
                    addons.add(new OrderItemDto(addon.getAddonName(), 1, price));
                }
            }
        }

        summary.setItems(items);
        summary.setAddons(addons);
        summary.setTotal(total);

        // Optional: save original IDs to submit form after confirmation
        summary.setDrinkIds(drinkIds);
        summary.setQuantities(quantities);
        summary.setAddonIds(addonIds);

        return summary;
    }
}
