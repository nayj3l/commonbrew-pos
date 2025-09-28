package com.commonbrew.pos.repository;

import com.commonbrew.pos.model.Order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByOrderTimeBetweenOrderByOrderTimeDesc(LocalDateTime start, LocalDateTime end);
}