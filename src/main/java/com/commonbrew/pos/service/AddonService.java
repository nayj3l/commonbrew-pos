package com.commonbrew.pos.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.repository.AddonRepository;
import com.commonbrew.pos.repository.MenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddonService {

    private final AddonRepository addonRepository;
    private final MenuRepository menuRepository;

    @Cacheable("addons")
    public List<Addon> getAllAddons() {
        return addonRepository.findAll();
    }

    @Cacheable(value = "addon", key = "#id")
    public Addon getAddonById(Long id) {
        return addonRepository.findById(id).orElse(null);
    }

    @CacheEvict(value = {"addons", "addon"}, allEntries = true)
    public void saveAddon(Addon addon, List<Long> menuIds) {
        if (menuIds != null) {
            List<Menu> menus = menuRepository.findAllById(menuIds);
            addon.setMenu(menus);
        }
        addonRepository.save(addon);
    }

    @CacheEvict(value = {"addons", "addon"}, allEntries = true)
    public void deleteAddon(Long id) {
        addonRepository.deleteById(id);
    }
}
