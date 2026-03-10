package com.hikaro.warehouse.dto;

import java.util.List;

public record ShipmentRequestDto(
        String referenceNumber,
        Long supplierId,
        List<Long> productIds
) {
}
