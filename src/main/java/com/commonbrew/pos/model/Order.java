package com.commonbrew.pos.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Order {

    @Id @GeneratedValue
    private Long orderId;

    private LocalDateTime orderTime;
    
    private Double totalAmount;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL)
    private List<OrderItem> items;
}
