package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Race condition demo result comparing unsafe and safe counters")
public record RaceConditionDemoResponseDto(
        @Schema(description = "Number of threads used in the demo", example = "64")
        int threadCount,

        @Schema(description = "Number of increments executed by each thread", example = "2000")
        int incrementsPerThread,

        @Schema(description = "Expected counter value after all increments", example = "128000")
        int expectedValue,

        @Schema(
                description = "Result of the unsafe counter without synchronization",
                example = "113742"
        )
        int unsafeCounterValue,

        @Schema(description = "Result of the synchronized counter", example = "128000")
        int synchronizedCounterValue,

        @Schema(description = "Result of the atomic counter", example = "128000")
        int atomicCounterValue,

        @Schema(description = "Whether a race condition was observed for the unsafe counter",
                example = "true")
        boolean raceConditionDetected
) {
}
