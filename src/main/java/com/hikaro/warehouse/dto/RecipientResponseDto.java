package com.hikaro.warehouse.dto;

import com.hikaro.warehouse.entity.RecipientType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Recipient response")
public record RecipientResponseDto(
        @Schema(description = "Recipient id", example = "1")
        Long id,

        @Schema(description = "Recipient name", example = "ООО ТехноМаркет")
        String name,

        @Schema(description = "Recipient type", example = "COMPANY")
        RecipientType type,

        @Schema(description = "Recipient contact email", example = "orders@technomarket.by")
        String contactEmail,

        @Schema(description = "Recipient address", example = "12 Commerce Street, Minsk")
        String address
) {
}
