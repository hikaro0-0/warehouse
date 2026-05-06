package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Dispatch create/update request")
public record DispatchRequestDto(
        @Schema(description = "Dispatch reference number", example = "OUT-2026-001")
        @NotBlank(message = "Dispatch reference number must not be blank")
        @Size(max = 100, message = "Dispatch reference number must be at most 100 characters")
        String referenceNumber,

        @Schema(description = "Warehouse id", example = "1")
        @NotNull(message = "Warehouse id must not be null")
        Long warehouseId,

        @Schema(description = "Recipient id", example = "2")
        @NotNull(message = "Recipient id must not be null")
        Long recipientId,

        @ArraySchema(
                schema = @Schema(implementation = DispatchItemRequestDto.class),
                arraySchema = @Schema(description = "Dispatch item list")
        )
        @NotEmpty(message = "Dispatch items must not be empty")
        List<@Valid DispatchItemRequestDto> items
) {
}
