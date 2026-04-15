package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.ShipmentRequestDto;
import com.hikaro.warehouse.dto.ShipmentResponseDto;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.entity.Shipment;
import com.hikaro.warehouse.exception.ApiErrorResponse;
import com.hikaro.warehouse.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/api/shipments")
@Tag(name = "Shipments", description = "Shipment management endpoints")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    @Operation(summary = "Get all shipments")
    @ApiResponse(responseCode = "200", description = "Shipments fetched successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ShipmentResponseDto.class))))
    public List<ShipmentResponseDto> findAll() {
        return shipmentService.findAll().stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shipment by id")
    @ApiResponse(responseCode = "200", description = "Shipment found",
                    content = @Content(schema = @Schema(implementation = ShipmentResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Shipment not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ShipmentResponseDto getById(@PathVariable Long id) {
        return toResponseDto(shipmentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create shipment")
    @ApiResponse(responseCode = "201", description = "Shipment created",
                    content = @Content(schema = @Schema(implementation = ShipmentResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ShipmentResponseDto create(@Valid @RequestBody ShipmentRequestDto request) {
        return toResponseDto(shipmentService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update shipment")
    @ApiResponse(responseCode = "200", description = "Shipment updated",
                    content = @Content(schema = @Schema(implementation = ShipmentResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Shipment not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ShipmentResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentRequestDto request
    ) {
        return toResponseDto(shipmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete shipment")
    @ApiResponse(responseCode = "204", description = "Shipment deleted")
    @ApiResponse(responseCode = "404", description = "Shipment not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public void delete(@PathVariable Long id) {
        shipmentService.delete(id);
    }

    private ShipmentResponseDto toResponseDto(Shipment shipment) {
        return new ShipmentResponseDto(
                shipment.getId(),
                shipment.getReferenceNumber(),
                shipment.getWarehouse().getId(),
                shipment.getWarehouse().getName(),
                shipment.getProducts()
                        .stream()
                        .map(Product::getId)
                        .sorted()
                        .toList()
        );
    }
}
