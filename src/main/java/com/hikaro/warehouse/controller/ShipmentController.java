package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.ShipmentRequestDto;
import com.hikaro.warehouse.dto.ShipmentResponseDto;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.entity.Shipment;
import com.hikaro.warehouse.service.ShipmentService;
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
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public List<ShipmentResponseDto> findAll() {
        return shipmentService.findAll().stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/{id}")
    public ShipmentResponseDto getById(@PathVariable Long id) {
        return toResponseDto(shipmentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponseDto create(@RequestBody ShipmentRequestDto request) {
        return toResponseDto(shipmentService.create(request));
    }

    @PutMapping("/{id}")
    public ShipmentResponseDto update(
            @PathVariable Long id,
            @RequestBody ShipmentRequestDto request
    ) {
        return toResponseDto(shipmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        shipmentService.delete(id);
    }

    private ShipmentResponseDto toResponseDto(Shipment shipment) {
        return new ShipmentResponseDto(
                shipment.getId(),
                shipment.getReferenceNumber(),
                shipment.getSupplier().getId(),
                shipment.getSupplier().getName(),
                shipment.getProducts()
                        .stream()
                        .map(Product::getId)
                        .sorted()
                        .toList()
        );
    }
}
