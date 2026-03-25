package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Bulk transaction demo request")
public record BulkOperationRequestDto(
        @Schema(description = "Supplier name", example = "Tech Supply LLC")
        @NotBlank(message = "Supplier name must not be blank")
        @Size(max = 100, message = "Supplier name must be at most 100 characters")
        String supplierName,

        @Schema(description = "Supplier email", example = "sales@techsupply.com")
        @NotBlank(message = "Supplier email must not be blank")
        @Email(message = "Supplier email must be valid")
        @Size(max = 255, message = "Supplier email must be at most 255 characters")
        String contactEmail,

        @Schema(description = "Warehouse name", example = "Central Warehouse")
        @NotBlank(message = "Warehouse name must not be blank")
        @Size(max = 100, message = "Warehouse name must be at most 100 characters")
        String warehouseName,

        @Schema(description = "Warehouse address", example = "Minsk, Industrialnaya 12")
        @NotBlank(message = "Warehouse address must not be blank")
        @Size(max = 255, message = "Warehouse address must be at most 255 characters")
        String warehouseAddress,

        @Schema(description = "Product name", example = "Monitor")
        @NotBlank(message = "Product name must not be blank")
        @Size(max = 100, message = "Product name must be at most 100 characters")
        String productName,

        @Schema(description = "Product SKU", example = "SKU-1001")
        @NotBlank(message = "Product sku must not be blank")
        @Size(max = 100, message = "Product sku must be at most 100 characters")
        String sku,

        @Schema(description = "Product quantity", example = "15")
        @NotNull(message = "Product quantity must not be null")
        @Min(value = 0, message = "Product quantity must be greater than or equal to 0")
        Integer quantity,

        @ArraySchema(schema = @Schema(description = "Category id", example = "3"), arraySchema = @Schema(description = "Category ids to assign"))
        List<@NotNull(message = "Category id must not be null") Long> categoryIds
) {
}
