package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Product create/update request")
public record ProductRequestDto(
        @Schema(description = "Product SKU", example = "SKU-1001")
        @NotBlank(message = "Product sku must not be blank")
        @Size(max = 100, message = "Product sku must be at most 100 characters")
        String sku,

        @Schema(description = "Product name", example = "Monitor")
        @NotBlank(message = "Product name must not be blank")
        @Size(max = 100, message = "Product name must be at most 100 characters")
        String name,

        @Schema(description = "Available quantity", example = "25")
        @NotNull(message = "Product quantity must not be null")
        @Min(value = 0, message = "Product quantity must be greater than or equal to 0")
        Integer quantity,

        @Schema(description = "Warehouse id", example = "1")
        @NotNull(message = "Warehouse id must not be null")
        Long warehouseId,

        @Schema(description = "Supplier id", example = "2")
        @NotNull(message = "Supplier id must not be null")
        Long supplierId,

        @ArraySchema(schema = @Schema(description = "Category id", example = "3"), arraySchema = @Schema(description = "Assigned category ids"))
        List<@NotNull(message = "Category id must not be null") Long> categoryIds
) {
}
