package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.SupplierRequestDto;
import com.hikaro.warehouse.dto.SupplierResponseDto;
import com.hikaro.warehouse.entity.Supplier;
import com.hikaro.warehouse.service.SupplierService;
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
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<SupplierResponseDto> findAll() {
        return supplierService.findAll().stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/{id}")
    public SupplierResponseDto getById(@PathVariable Long id) {
        return toResponseDto(supplierService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponseDto create(@RequestBody SupplierRequestDto request) {
        return toResponseDto(supplierService.create(toEntity(request)));
    }

    @PutMapping("/{id}")
    public SupplierResponseDto update(
            @PathVariable Long id,
            @RequestBody SupplierRequestDto request
    ) {
        return toResponseDto(supplierService.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
