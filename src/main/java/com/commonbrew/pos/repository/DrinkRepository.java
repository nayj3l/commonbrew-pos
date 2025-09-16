package com.commonbrew.pos.repository;

import com.commonbrew.pos.model.Drink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DrinkRepository extends JpaRepository<Drink, Long> {
    List<Drink> findByCategoryCategoryId(Long categoryId);
}
