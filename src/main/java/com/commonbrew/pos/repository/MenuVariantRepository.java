package com.commonbrew.pos.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.commonbrew.pos.model.MenuVariant;

@Repository
public interface MenuVariantRepository extends JpaRepository<MenuVariant, Long> {

    List<MenuVariant> findByVariantIdIn(List<Long> variantIds);

    List<MenuVariant> findByActiveTrue();

    Optional<MenuVariant> findByVariantNameAndActiveTrue(String variantName);

    List<MenuVariant> findByMenus_IdIn(Collection<Long> menuIds);

}