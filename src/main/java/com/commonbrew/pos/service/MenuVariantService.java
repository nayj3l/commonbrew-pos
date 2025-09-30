package com.commonbrew.pos.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commonbrew.pos.dto.MenuVariantResponse;
import com.commonbrew.pos.mapper.MenuVariantMapper;
import com.commonbrew.pos.model.MenuVariant;
import com.commonbrew.pos.repository.MenuVariantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MenuVariantService {

    private final MenuVariantRepository menuVariantRepository;
    private final MenuVariantMapper menuVariantMapper;

    @Transactional
    public MenuVariant save(MenuVariant variant) {
        log.info("Saving...");
        return menuVariantRepository.save(variant);
    }

    @Cacheable("variants")
    public List<MenuVariantResponse> getAllVariants() {
        return menuVariantRepository.findAll().stream()
                .map(menuVariantMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "variant", key = "#id")
    public Optional<MenuVariant> getVariantById(Long id) {
        return menuVariantRepository.findById(id);
    }

    public Optional<MenuVariant> findByVariantName(String name) {
        return menuVariantRepository.findByVariantNameAndActiveTrue(name);
    }

    @Transactional
    @CacheEvict(value = {"variants", "variant", "variantsByMenu"}, allEntries = true)
    public MenuVariant saveVariant(MenuVariant variant) {
        return menuVariantRepository.save(variant);
    }

    @Transactional
    @CacheEvict(value = {"variants", "variant", "variantsByItem"}, allEntries = true)
    public void deleteVariant(Long id) {
        menuVariantRepository.deleteById(id);
    }

    public List<MenuVariant> findAllActiveVariants() {
        List<MenuVariant> menuVariants = menuVariantRepository.findByActiveTrue();

        if (menuVariants.isEmpty()) {
            return Collections.emptyList();
        }

        return menuVariants;
    }

    public List<MenuVariant> findAllById(List<Long> variantsId) {
        return menuVariantRepository.findAllById(variantsId);
    }

}