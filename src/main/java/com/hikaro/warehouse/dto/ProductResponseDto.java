package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Product response")
public record ProductResponseDto(
        @Schema(description = "Product id", example = "1")
        Long id,

        @Schema(description = "Product SKU", example = "SKU-1001")
        String sku,

        @Schema(description = "Product name", example = "Monitor")
        String name,

        @Schema(description = "Available quantity", example = "25")
        Integer quantity,

        @Schema(description = "Warehouse id", example = "1")
        Long warehouseId,

        @Schema(description = "Warehouse name", example = "Central Warehouse")
        String warehouseName,

        @Schema(description = "Supplier id", example = "2")
        Long supplierId,

        @Schema(description = "Supplier name", example = "Tech Supply LLC")
        String supplierName,

        @ArraySchema(schema = @Schema(example = "Displays"), arraySchema = @Schema(description = "Assigned categories"))
        List<String> categories
) {
}
