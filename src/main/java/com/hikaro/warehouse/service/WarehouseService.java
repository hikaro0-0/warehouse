package com.hikaro.warehouse.service;

import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.repository.WarehouseRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }

    public Warehouse getById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Warehouse with id " + id + " not found"
                        )
                );
    }

    public Warehouse create(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    public Warehouse update(Long id, Warehouse updatedWarehouse) {
        Warehouse warehouse = getById(id);
        warehouse.setName(updatedWarehouse.getName());
        warehouse.setAddress(updatedWarehouse.getAddress());
        return warehouseRepository.save(warehouse);
    }

    public void delete(Long id) {
        Warehouse warehouse = getById(id);
        warehouseRepository.delete(warehouse);
    }
}
