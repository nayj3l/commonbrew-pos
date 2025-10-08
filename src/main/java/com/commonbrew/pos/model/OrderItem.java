package com.commonbrew.pos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "order_item")
@Data
@NoArgsConstructor
@ToString(exclude = "order") 
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    private Menu menu;

    @ManyToOne
    private MenuItem item;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private MenuVariant variant;

    @Column(nullable = false)
    private String menuItemNameSnapshot;

    // snapshot values to preserve history
    @Column(nullable = false)
    private String variantNameSnapshot;

    @Column(nullable = false)
    private Double unitPriceSnapshot;

    private Integer quantity;

    // subtotal for this order item (unit * qty + addons)
    private Double subtotal;
}
