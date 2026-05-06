package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dispatch item request")
public record DispatchItemRequestDto(
        @Schema(description = "Product id", example = "4")
        @NotNull(message = "Product id must not be null")
        Long productId,

        @Schema(description = "Dispatch quantity", example = "3")
        @NotNull(message = "Dispatch quantity must not be null")
        @Min(value = 1, message = "Dispatch quantity must be greater than 0")
        Integer quantity
) {
}
