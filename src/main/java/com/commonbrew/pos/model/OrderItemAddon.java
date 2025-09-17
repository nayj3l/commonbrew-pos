package com.commonbrew.pos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item_addons")
@Data
@NoArgsConstructor
public class OrderItemAddon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne
    @JoinColumn(name = "addon_id", nullable = false)
    private Addon addon;

    // snapshot addon details
    private String addonNameSnapshot;
    private Double addonPriceSnapshot;
    private Integer quantity; // usually 1, but allow multiple
}
