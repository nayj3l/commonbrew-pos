package com.commonbrew.pos.dto.wrapper;

import com.commonbrew.pos.dto.MenuResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuWrapper {
    private MenuResponse menu;
}
