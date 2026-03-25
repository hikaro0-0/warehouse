package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Shipment create/update request")
public record ShipmentRequestDto(
        @Schema(description = "Shipment reference number", example = "REF-2026-001")
        @NotBlank(message = "Shipment reference number must not be blank")
        @Size(max = 100, message = "Shipment reference number must be at most 100 characters")
        String referenceNumber,

        @Schema(description = "Warehouse id", example = "1")
        @NotNull(message = "Warehouse id must not be null")
        Long warehouseId,

        @ArraySchema(schema = @Schema(description = "Product id", example = "4"), arraySchema = @Schema(description = "Products in shipment"))
        @NotEmpty(message = "Product ids must not be empty")
        List<@NotNull(message = "Product id must not be null") Long> productIds
) {
}
