package com.hikaro.warehouse.service;

import com.hikaro.warehouse.dto.ShipmentRequestDto;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.entity.Shipment;
import com.hikaro.warehouse.entity.Supplier;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.repository.ProductRepository;
import com.hikaro.warehouse.repository.ShipmentRepository;
import com.hikaro.warehouse.repository.SupplierRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    public List<Shipment> findAll() {
        return shipmentRepository.findAll();
    }

    public Shipment getById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Shipment with id " + id + " not found"
                        )
                );
    }

    public Shipment create(ShipmentRequestDto request) {
        Shipment shipment = new Shipment();
        applyRequest(shipment, request);
        return shipmentRepository.save(shipment);
    }

    public Shipment update(Long id, ShipmentRequestDto request) {
        Shipment shipment = getById(id);
        applyRequest(shipment, request);
        return shipmentRepository.save(shipment);
    }

    public void delete(Long id) {
        Shipment shipment = getById(id);
        shipmentRepository.delete(shipment);
    }

    private void applyRequest(Shipment shipment, ShipmentRequestDto request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Supplier with id "
                                        + request.supplierId()
                                        + " not found"
                        )
                );
        Set<Product> products = new LinkedHashSet<>(
                productRepository.findAllById(request.productIds())
        );
        if (products.size() != request.productIds().size()) {
            throw new ResourceNotFoundException("One or more products not found");
        }

        shipment.setReferenceNumber(request.referenceNumber());
        shipment.setSupplier(supplier);
        shipment.setProducts(products);
    }
}
