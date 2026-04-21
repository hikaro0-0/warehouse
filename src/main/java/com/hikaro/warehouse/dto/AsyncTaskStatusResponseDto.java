package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Response with the current asynchronous task status")
public record AsyncTaskStatusResponseDto(
        @Schema(description = "Task id", example = "78f0d749-fd1e-4a3a-bb7f-894f9e6a9b11")
        String taskId,

        @Schema(description = "Current task status")
        AsyncTaskStatus status,

        @Schema(description = "Task creation timestamp", example = "2026-04-21T10:15:30Z")
        Instant createdAt,

        @Schema(description = "Task start timestamp", nullable = true,
                example = "2026-04-21T10:15:31Z")
        Instant startedAt,

        @Schema(description = "Task completion timestamp", nullable = true,
                example = "2026-04-21T10:15:35Z")
        Instant completedAt,

        @Schema(description = "Error details when task execution fails", nullable = true,
                example = "One or more categories not found")
        String errorMessage
) {
}
