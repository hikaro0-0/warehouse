package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Category create/update request")
public record CategoryRequestDto(
        @Schema(description = "Category name", example = "Laptops")
        @NotBlank(message = "Category name must not be blank")
        @Size(max = 100, message = "Category name must be at most 100 characters")
        String name,

        @Schema(description = "Category description", example = "Portable computers and ultrabooks")
        @NotBlank(message = "Category description must not be blank")
        @Size(max = 255, message = "Category description must be at most 255 characters")
        String description
) {
}
