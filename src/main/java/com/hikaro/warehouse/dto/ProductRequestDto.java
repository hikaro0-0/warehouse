package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
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

        @Schema(description = "Product description", example = "27-inch 4K monitor with IPS panel")
        @Size(max = 1000, message = "Product description must be at most 1000 characters")
        String description,

        @Schema(description = "Available quantity", example = "25")
        @NotNull(message = "Product quantity must not be null")
        @Min(value = 0, message = "Product quantity must be greater than or equal to 0")
        Integer quantity,

        @Schema(description = "Product price", example = "1299.99")
        @DecimalMin(value = "0.0", inclusive = true, message = "Product price must be greater than or equal to 0")
        @Digits(integer = 10, fraction = 2, message = "Product price must have up to 2 decimal places")
        BigDecimal price,

        @Schema(description = "Warehouse id", example = "1")
        @NotNull(message = "Warehouse id must not be null")
        Long warehouseId,

        @Schema(description = "Supplier id", example = "2")
        @NotNull(message = "Supplier id must not be null")
        Long supplierId,

        @ArraySchema(schema = @Schema(description = "Category id", example = "3"), arraySchema = @Schema(description = "Assigned category ids"))
        List<@NotNull(message = "Category id must not be null") Long> categoryIds
) {
    public ProductRequestDto(
            String sku,
            String name,
            Integer quantity,
            Long warehouseId,
            Long supplierId,
            List<Long> categoryIds
    ) {
        this(sku, name, null, quantity, null, warehouseId, supplierId, categoryIds);
    }
}
