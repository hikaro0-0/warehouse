package com.hikaro.warehouse.dto;

import java.util.List;

public record BulkOperationRequestDto(
        String supplierName,
        String contactEmail,
        String warehouseName,
        String warehouseAddress,
        String productName,
        String sku,
        Integer quantity,
        List<Long> categoryIds
) {
}
