package com.hikaro.warehouse.dto;

import java.util.List;

public record ShipmentResponseDto(
        Long id,
        String referenceNumber,
        Long supplierId,
        String supplierName,
        List<Long> productIds
) {
}
