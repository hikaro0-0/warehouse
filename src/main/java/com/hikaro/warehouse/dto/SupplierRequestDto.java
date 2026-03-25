package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Supplier create/update request")
public record SupplierRequestDto(
        @Schema(description = "Supplier name", example = "Tech Supply LLC")
        @NotBlank(message = "Supplier name must not be blank")
        @Size(max = 100, message = "Supplier name must be at most 100 characters")
        String name,

        @Schema(description = "Supplier contact email", example = "sales@techsupply.com")
        @NotBlank(message = "Supplier email must not be blank")
        @Email(message = "Supplier email must be valid")
        @Size(max = 255, message = "Supplier email must be at most 255 characters")
        String contactEmail
) {
}
