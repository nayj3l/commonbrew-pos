package com.commonbrew.pos.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long variantId;

    @ManyToMany(mappedBy = "variants")
    @JsonIgnore
    private Set<Menu> menus = new HashSet<>();

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
