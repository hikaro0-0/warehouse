package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category response")
public record CategoryResponseDto(
        @Schema(description = "Category id", example = "1")
        Long id,

        @Schema(description = "Category name", example = "Laptops")
        String name,

        @Schema(description = "Category description", example = "Portable computers and ultrabooks")
        String description
) {
}
