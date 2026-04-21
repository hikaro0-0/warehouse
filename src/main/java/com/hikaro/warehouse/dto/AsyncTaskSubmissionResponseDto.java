package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after asynchronous task submission")
public record AsyncTaskSubmissionResponseDto(
        @Schema(description = "Generated task id", example = "78f0d749-fd1e-4a3a-bb7f-894f9e6a9b11")
        String taskId,

        @Schema(description = "Initial task status")
        AsyncTaskStatus status
) {
}
