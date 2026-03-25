package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.SupplierRequestDto;
import com.hikaro.warehouse.dto.SupplierResponseDto;
import com.hikaro.warehouse.entity.Supplier;
import com.hikaro.warehouse.exception.ApiErrorResponse;
import com.hikaro.warehouse.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "Suppliers", description = "Supplier management endpoints")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @Operation(summary = "Get all suppliers")
    @ApiResponse(responseCode = "200", description = "Suppliers fetched successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SupplierResponseDto.class))))
    public List<SupplierResponseDto> findAll() {
        return supplierService.findAll().stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier found",
                    content = @Content(schema = @Schema(implementation = SupplierResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public SupplierResponseDto getById(@PathVariable Long id) {
        return toResponseDto(supplierService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supplier created",
                    content = @Content(schema = @Schema(implementation = SupplierResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public SupplierResponseDto create(@Valid @RequestBody SupplierRequestDto request) {
        return toResponseDto(supplierService.create(toEntity(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier updated",
                    content = @Content(schema = @Schema(implementation = SupplierResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public SupplierResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequestDto request
    ) {
        return toResponseDto(supplierService.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supplier deleted"),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void delete(@PathVariable Long id) {
        supplierService.delete(id);
    }

    private Supplier toEntity(SupplierRequestDto request) {
        return new Supplier(null, request.name(), request.contactEmail());
    }

    private SupplierResponseDto toResponseDto(Supplier supplier) {
        return new SupplierResponseDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactEmail()
        );
    }
}
