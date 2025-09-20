package com.commonbrew.pos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_variants")
@Data
@NoArgsConstructor
public class ItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    // e.g. "Regular", "Upsize", "Sliced", "4-inch", or "Default"
    @Column(nullable = false)
    private String variantName;

    @Column(nullable = false)
    private Double price;

    // optional SKU/code or ordering weight
    private String code;

    @Column(nullable = false)
    private boolean active = true;

}

