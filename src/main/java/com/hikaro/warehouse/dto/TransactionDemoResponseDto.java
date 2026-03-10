package com.hikaro.warehouse.dto;

public record TransactionDemoResponseDto(
        String mode,
        String message,
        long suppliers,
        long warehouses,
        long products,
        long shipments
) {
}
