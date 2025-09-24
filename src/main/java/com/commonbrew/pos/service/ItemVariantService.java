package com.commonbrew.pos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commonbrew.pos.model.ItemVariant;
import com.commonbrew.pos.model.dto.ItemVariantResponse;
import com.commonbrew.pos.repository.ItemVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemVariantService {
    private final ItemVariantRepository variantRepository;

    @Cacheable("variants")
    public List<ItemVariantResponse> getAllVariants() {
        return variantRepository.findAll().stream()
                .map(this::mapToItemVariantResponse)
                .toList();
    }

    @Cacheable(value = "variant", key = "#id")
    public Optional<ItemVariant> getVariantById(Long id) {
        return variantRepository.findById(id);
    }

    @Cacheable(value = "variantsByItem", key = "#menuItemId")
    public List<ItemVariant> getVariantsByMenuItemId(Long menuItemId) {
        return variantRepository.findByMenuItemIdWithMenuItem(menuItemId);
    }

    @Transactional
    @CacheEvict(value = {"variants", "variant", "variantsByItem"}, allEntries = true)
    public ItemVariant saveVariant(ItemVariant variant) {
        return variantRepository.save(variant);
    }

    @Transactional
    @CacheEvict(value = {"variants", "variant", "variantsByItem"}, allEntries = true)
    public void deleteVariant(Long id) {
        variantRepository.deleteById(id);
    }

    private ItemVariantResponse mapToItemVariantResponse(ItemVariant variant) {
        return ItemVariantResponse.builder()
                .variantId(variant.getVariantId())
                .variantName(variant.getVariantName())
                .price(variant.getPrice())
                .code(variant.getCode())
                .active(variant.isActive())
                .build();
    }
}