package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supplier response")
public record SupplierResponseDto(
        @Schema(description = "Supplier id", example = "1")
        Long id,

        @Schema(description = "Supplier name", example = "Tech Supply LLC")
        String name,

        @Schema(description = "Supplier contact email", example = "sales@techsupply.com")
        String contactEmail
) {
}
