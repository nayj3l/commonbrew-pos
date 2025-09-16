package com.commonbrew.pos.repository;

import com.commonbrew.pos.model.Addon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddonRepository extends JpaRepository<Addon, Long> {
}
