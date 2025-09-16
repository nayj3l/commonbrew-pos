package com.commonbrew.pos.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class OrderItem {

    @Id @GeneratedValue
    private Long orderItemId;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Drink drink;

    private int quantity;

    @ManyToMany
    private List<Addon> addons;
}
