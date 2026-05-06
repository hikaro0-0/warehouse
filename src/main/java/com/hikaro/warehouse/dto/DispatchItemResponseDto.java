package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dispatch item response")
public record DispatchItemResponseDto(
        @Schema(description = "Product id", example = "4")
        Long productId,

        @Schema(description = "Product sku", example = "SKU-1001")
        String productSku,

        @Schema(description = "Product name", example = "Monitor")
        String productName,

        @Schema(description = "Dispatched quantity", example = "3")
        Integer quantity
) {
}
