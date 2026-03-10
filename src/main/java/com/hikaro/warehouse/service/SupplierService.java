package com.hikaro.warehouse.service;

import com.hikaro.warehouse.entity.Supplier;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.repository.SupplierRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier getById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Supplier with id " + id + " not found"
                        )
                );
    }

    public Supplier create(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public Supplier update(Long id, Supplier updatedSupplier) {
        Supplier supplier = getById(id);
        supplier.setName(updatedSupplier.getName());
        supplier.setContactEmail(updatedSupplier.getContactEmail());
        return supplierRepository.save(supplier);
    }

    public void delete(Long id) {
        Supplier supplier = getById(id);
        supplierRepository.delete(supplier);
    }
}
