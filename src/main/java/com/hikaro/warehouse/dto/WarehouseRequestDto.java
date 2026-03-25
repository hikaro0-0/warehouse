package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Warehouse create/update request")
public record WarehouseRequestDto(
        @Schema(description = "Warehouse name", example = "Central Warehouse")
        @NotBlank(message = "Warehouse name must not be blank")
        @Size(max = 100, message = "Warehouse name must be at most 100 characters")
        String name,

        @Schema(description = "Warehouse address", example = "Minsk, Industrialnaya 12")
        @NotBlank(message = "Warehouse address must not be blank")
        @Size(max = 255, message = "Warehouse address must be at most 255 characters")
        String address
) {
}
