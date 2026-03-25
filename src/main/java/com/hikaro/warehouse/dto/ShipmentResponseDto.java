package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Shipment response")
public record ShipmentResponseDto(
        @Schema(description = "Shipment id", example = "1")
        Long id,

        @Schema(description = "Shipment reference number", example = "REF-2026-001")
        String referenceNumber,

        @Schema(description = "Warehouse id", example = "1")
        Long warehouseId,

        @Schema(description = "Warehouse name", example = "Central Warehouse")
        String warehouseName,

        @ArraySchema(schema = @Schema(description = "Product id", example = "4"), arraySchema = @Schema(description = "Products in shipment"))
        List<Long> productIds
) {
}
