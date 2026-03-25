package com.hikaro.warehouse.exception;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Unified API error response")
public record ApiErrorResponse(
        @Schema(description = "Error timestamp", example = "2026-03-25T12:34:56Z")
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP status reason", example = "Bad Request")
        String error,

        @Schema(description = "Human-readable error message", example = "Validation failed")
        String message,

        @Schema(description = "Request path", example = "/api/categories")
        String path,

        @ArraySchema(schema = @Schema(implementation = ApiValidationError.class), arraySchema = @Schema(description = "Validation issues list"))
        List<ApiValidationError> errors
) {
}
