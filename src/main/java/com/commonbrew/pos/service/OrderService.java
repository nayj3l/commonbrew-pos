package com.commonbrew.pos.service;

import com.commonbrew.pos.model.*;
import com.commonbrew.pos.repository.AddonRepository;
import com.commonbrew.pos.repository.DrinkRepository;
import com.commonbrew.pos.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
}
