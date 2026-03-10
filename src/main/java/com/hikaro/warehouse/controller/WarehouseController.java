package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.WarehouseRequestDto;
import com.hikaro.warehouse.dto.WarehouseResponseDto;
import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.service.WarehouseService;
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
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public List<WarehouseResponseDto> findAll() {
        return warehouseService.findAll().stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/{id}")
    public WarehouseResponseDto getById(@PathVariable Long id) {
        return toResponseDto(warehouseService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponseDto create(@RequestBody WarehouseRequestDto request) {
        return toResponseDto(warehouseService.create(toEntity(request)));
    }

    @PutMapping("/{id}")
    public WarehouseResponseDto update(
            @PathVariable Long id,
            @RequestBody WarehouseRequestDto request
    ) {
        return toResponseDto(warehouseService.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
