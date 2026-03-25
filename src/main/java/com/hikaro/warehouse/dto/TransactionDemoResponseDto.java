package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transaction demo summary response")
public record TransactionDemoResponseDto(
        @Schema(description = "Execution mode", example = "with-transaction")
        String mode,

        @Schema(description = "Result message", example = "Transaction demo completed")
        String message,

        @Schema(description = "Suppliers count", example = "1")
        long suppliers,

        @Schema(description = "Warehouses count", example = "1")
        long warehouses,

        @Schema(description = "Products count", example = "1")
        long products,

        @Schema(description = "Shipments count", example = "0")
        long shipments
) {
}
