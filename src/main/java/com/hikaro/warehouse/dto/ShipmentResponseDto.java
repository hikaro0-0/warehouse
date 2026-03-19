package com.hikaro.warehouse.dto;

import java.util.List;

public record ShipmentResponseDto(
        Long id,
        String referenceNumber,
        Long warehouseId,
        String warehouseName,
        List<Long> productIds
) {
}
