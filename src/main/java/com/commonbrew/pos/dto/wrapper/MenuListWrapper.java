package com.commonbrew.pos.dto.wrapper;

import java.util.List;

import com.commonbrew.pos.dto.MenuResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuListWrapper {
    private List<MenuResponse> menus;
}
