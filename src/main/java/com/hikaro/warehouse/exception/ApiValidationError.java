package com.hikaro.warehouse.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation error details for a specific field")
public record ApiValidationError(
        @Schema(description = "Request field name", example = "name")
        String field,

        @Schema(description = "Validation error message", example = "Category name must not be blank")
        String message,

        @Schema(description = "Rejected field value", example = "")
        Object rejectedValue
) {
}
