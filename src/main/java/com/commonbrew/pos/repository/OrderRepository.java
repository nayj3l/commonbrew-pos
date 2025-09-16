package com.commonbrew.pos.repository;

import com.commonbrew.pos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}