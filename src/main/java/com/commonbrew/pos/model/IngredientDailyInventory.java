package com.commonbrew.pos.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ingredient_daily_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientDailyInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false)
    private LocalDate inventoryDate;

    @Column(nullable = false)
    private Double openingQty;

    private Double theoreticalUsed; // computed from sales
    private Double actualUsed;      // computed from weighing
    private Double closingActual;   // end-of-day remaining
    private Double variance;        // theoreticalUsed - actualUsed

    @Column(nullable = false)
    private boolean manualOpening = false; // manual override indicator

    private String remarks;
}

