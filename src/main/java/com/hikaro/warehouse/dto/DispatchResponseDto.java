package com.hikaro.warehouse.dto;

import com.hikaro.warehouse.entity.DispatchStatus;
import com.hikaro.warehouse.entity.RecipientType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Dispatch response")
public record DispatchResponseDto(
        @Schema(description = "Dispatch id", example = "1")
        Long id,

        @Schema(description = "Dispatch reference number", example = "OUT-2026-001")
        String referenceNumber,

        @Schema(description = "Warehouse id", example = "1")
        Long warehouseId,

        @Schema(description = "Warehouse name", example = "Central Warehouse")
        String warehouseName,

        @Schema(description = "Recipient id", example = "2")
        Long recipientId,

        @Schema(description = "Recipient name", example = "ООО ТехноМаркет")
        String recipientName,

        @Schema(description = "Recipient type", example = "STORE")
        RecipientType recipientType,

        @Schema(description = "Dispatch status", example = "DRAFT")
        DispatchStatus status,

        @Schema(description = "Dispatch creation timestamp", example = "2026-05-06T10:15:30Z")
        Instant createdAt,

        @Schema(description = "Dispatch update timestamp", example = "2026-05-06T10:16:10Z")
        Instant updatedAt,

        @ArraySchema(
                schema = @Schema(implementation = DispatchItemResponseDto.class),
                arraySchema = @Schema(description = "Dispatch item list")
        )
        List<DispatchItemResponseDto> items
) {
}
