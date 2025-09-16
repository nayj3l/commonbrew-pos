package com.commonbrew.pos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.repository.AddonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddonService {

    private final AddonRepository addonRepository;

    public List<Addon> getAllAddons() {
        return addonRepository.findAll();
    }

    public Addon saveAddon(Addon addon) {
        return addonRepository.save(addon);
    }

    public void deleteAddon(Long id) {
        addonRepository.deleteById(id);
    }
}
