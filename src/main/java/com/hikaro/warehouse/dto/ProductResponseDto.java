package com.hikaro.warehouse.dto;

public record ProductResponseDto(
        Long id,
        String name,
        Integer quantity,
        String location
) {
}
