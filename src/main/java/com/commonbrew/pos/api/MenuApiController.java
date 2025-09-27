package com.commonbrew.pos.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commonbrew.pos.model.dto.MenuResponse;
import com.commonbrew.pos.service.MenuService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuApiController {

    private final MenuService menuService;
    
    @GetMapping
    public List<MenuResponse> getAllMenus() {
        return menuService.getMenuApiResponse();
    }

}