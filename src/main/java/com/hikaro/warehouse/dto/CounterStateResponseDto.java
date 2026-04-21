package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current values of thread-safe demo counters")
public record CounterStateResponseDto(
        @Schema(description = "Current synchronized counter value", example = "3")
        int synchronizedCounter,

        @Schema(description = "Current atomic counter value", example = "5")
        int atomicCounter
) {
}
