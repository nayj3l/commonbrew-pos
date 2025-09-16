package com.commonbrew.pos.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item")
@Data
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "drink_id", nullable = false)
    private Drink drink;

    private int quantity;

    @ManyToMany
    @JoinTable(
        name = "order_item_addon",
        joinColumns = @JoinColumn(name = "order_item_id"),
        inverseJoinColumns = @JoinColumn(name = "addon_id")
    )
    private List<Addon> addons;
}
