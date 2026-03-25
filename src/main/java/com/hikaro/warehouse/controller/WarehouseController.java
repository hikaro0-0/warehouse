package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.WarehouseRequestDto;
import com.hikaro.warehouse.dto.WarehouseResponseDto;
import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.exception.ApiErrorResponse;
import com.hikaro.warehouse.service.WarehouseService;
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
@RequestMapping("/api/warehouses")
@Tag(name = "Warehouses", description = "Warehouse management endpoints")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    @Operation(summary = "Get all warehouses")
    @ApiResponse(responseCode = "200", description = "Warehouses fetched successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = WarehouseResponseDto.class))))
    public List<WarehouseResponseDto> findAll() {
        return warehouseService.findAll().stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouse found",
                    content = @Content(schema = @Schema(implementation = WarehouseResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Warehouse not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public WarehouseResponseDto getById(@PathVariable Long id) {
        return toResponseDto(warehouseService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create warehouse")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Warehouse created",
                    content = @Content(schema = @Schema(implementation = WarehouseResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public WarehouseResponseDto create(@Valid @RequestBody WarehouseRequestDto request) {
        return toResponseDto(warehouseService.create(toEntity(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update warehouse")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouse updated",
                    content = @Content(schema = @Schema(implementation = WarehouseResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Warehouse not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public WarehouseResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequestDto request
    ) {
        return toResponseDto(warehouseService.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete warehouse")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Warehouse deleted"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void delete(@PathVariable Long id) {
        warehouseService.delete(id);
    }

    private Warehouse toEntity(WarehouseRequestDto request) {
        return new Warehouse(null, request.name(), request.address());
    }

    private WarehouseResponseDto toResponseDto(Warehouse warehouse) {
        return new WarehouseResponseDto(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getAddress()
        );
    }
}
