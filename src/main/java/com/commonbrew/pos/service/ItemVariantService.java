package com.commonbrew.pos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commonbrew.pos.model.ItemVariant;
import com.commonbrew.pos.repository.ItemVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemVariantService {
    private final ItemVariantRepository variantRepository;

    public List<ItemVariant> getAllVariants() {
        return variantRepository.findAll();
    }

    public Optional<ItemVariant> getVariantById(Long id) {
        return variantRepository.findById(id);
    }

    public List<ItemVariant> getVariantsByMenuItemId(Long menuItemId) {
        return variantRepository.findByMenuItemId(menuItemId);
    }

    @Transactional
    public ItemVariant saveVariant(ItemVariant variant) {
        return variantRepository.save(variant);
    }

    @Transactional
    public void deleteVariant(Long id) {
        variantRepository.deleteById(id);
    }
}