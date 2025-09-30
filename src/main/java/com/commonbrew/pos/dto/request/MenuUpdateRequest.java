package com.commonbrew.pos.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class MenuUpdateRequest {
    private Long menuId;
    private String name;
    private String imageUrl;
    private List<Long> variantsId;
}
