package com.commonbrew.pos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.commonbrew.pos.model.ItemVariant;

@Repository
public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {

    /**
     * Return all variants for a given MenuItem id.
     */
    List<ItemVariant> findByMenuItemId(Long menuItemId); // Changed from findByMenuItemMenuItemId

    /**
     * Return the first (default) variant for a MenuItem, ordered by variantId ascending.
     */
    Optional<ItemVariant> findFirstByMenuItemIdOrderByVariantIdAsc(Long menuItemId); // Changed

    /**
     * Find a variant by menu item and (case-insensitive) variant name.
     */
    Optional<ItemVariant> findByMenuItemIdAndVariantNameIgnoreCase(Long menuItemId, String variantName); // Changed

    /**
     * Batch lookup by variant ids.
     */
    List<ItemVariant> findByVariantIdIn(List<Long> variantIds);
}