package com.hikaro.warehouse.dto;

import com.hikaro.warehouse.entity.RecipientType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Recipient create/update request")
public record RecipientRequestDto(
        @Schema(description = "Recipient name", example = "ООО ТехноМаркет")
        @NotBlank(message = "Recipient name must not be blank")
        @Size(max = 100, message = "Recipient name must be at most 100 characters")
        String name,

        @Schema(description = "Recipient type", example = "COMPANY")
        @NotNull(message = "Recipient type must not be null")
        RecipientType type,

        @Schema(description = "Recipient contact email", example = "orders@technomarket.by")
        @Email(message = "Recipient email must be valid")
        @Size(max = 255, message = "Recipient email must be at most 255 characters")
        String contactEmail,

        @Schema(description = "Recipient address", example = "12 Commerce Street, Minsk")
        @Size(max = 255, message = "Recipient address must be at most 255 characters")
        String address
) {
}
