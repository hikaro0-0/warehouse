package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Warehouse response")
public record WarehouseResponseDto(
        @Schema(description = "Warehouse id", example = "1")
        Long id,

        @Schema(description = "Warehouse name", example = "Central Warehouse")
        String name,

        @Schema(description = "Warehouse address", example = "Minsk, Industrialnaya 12")
        String address
) {
}
