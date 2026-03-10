package com.hikaro.warehouse.dto;

import java.util.List;

public record ProductRequestDto(
        String sku,
        String name,
        Integer quantity,
        Long warehouseId,
        Long supplierId,
        List<Long> categoryIds
) {
}
