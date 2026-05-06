package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Product response")
public record ProductResponseDto(
        @Schema(description = "Product id", example = "1")
        Long id,

        @Schema(description = "Product SKU", example = "SKU-1001")
        String sku,

        @Schema(description = "Product name", example = "Monitor")
        String name,

        @Schema(description = "Product description", example = "27-inch 4K monitor with IPS panel")
        String description,

        @Schema(description = "Available quantity", example = "25")
        Integer quantity,

        @Schema(description = "Product price", example = "1299.99")
        BigDecimal price,

        @Schema(description = "Product creation timestamp", example = "2026-05-05T18:39:00Z")
        Instant createdAt,

        @Schema(description = "Product update timestamp", example = "2026-05-05T18:45:00Z")
        Instant updatedAt,

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
    public ProductResponseDto(
            Long id,
            String sku,
            String name,
            Integer quantity,
            Long warehouseId,
            String warehouseName,
            Long supplierId,
            String supplierName,
            List<String> categories
    ) {
        this(id, sku, name, null, quantity, null, null, null, warehouseId, warehouseName, supplierId, supplierName, categories);
    }
}
