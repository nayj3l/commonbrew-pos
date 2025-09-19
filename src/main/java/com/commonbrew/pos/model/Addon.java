package com.commonbrew.pos.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addons")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Addon {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addonId;

    private String addonName;
    private Double price;

    @ManyToMany
    @JoinTable(name = "addon_menus",
        joinColumns = @JoinColumn(name = "addon_id"),
        inverseJoinColumns = @JoinColumn(name = "menu_id"))
    private List<Menu> menu = new ArrayList<>();
}
