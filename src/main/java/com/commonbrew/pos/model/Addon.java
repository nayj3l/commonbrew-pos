package com.commonbrew.pos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addons")
@Data
@NoArgsConstructor
public class Addon {
  
    @Id @GeneratedValue
    private Long addonId;

    private String addonName;
    private Double price;

}
