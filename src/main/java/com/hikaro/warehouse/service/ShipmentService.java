package com.hikaro.warehouse.service;

import com.hikaro.warehouse.dto.ShipmentRequestDto;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.entity.Shipment;
import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.repository.ProductRepository;
import com.hikaro.warehouse.repository.ShipmentRepository;
import com.hikaro.warehouse.repository.WarehouseRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Shipment> findAll() {
        List<Shipment> shipments = shipmentRepository.findAll();
        initializeAssociations(shipments);
        return shipments;
    }

    @Transactional(readOnly = true)
    public Shipment getById(Long id) {
        Shipment shipment = findByIdOrThrow(id);
        initializeAssociations(List.of(shipment));
        return shipment;
    }

    @Transactional
    public Shipment create(ShipmentRequestDto request) {
        Shipment shipment = new Shipment();
        applyRequest(shipment, request);
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public Shipment update(Long id, ShipmentRequestDto request) {
        Shipment shipment = findByIdOrThrow(id);
        applyRequest(shipment, request);
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public void delete(Long id) {
        Shipment shipment = findByIdOrThrow(id);
        shipmentRepository.delete(shipment);
    }

    private Shipment findByIdOrThrow(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Shipment with id " + id + " not found"
                        )
                );
    }

    private void applyRequest(Shipment shipment, ShipmentRequestDto request) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Warehouse with id "
                                        + request.warehouseId()
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
        shipment.setWarehouse(warehouse);
        shipment.setProducts(products);
    }

    private void initializeAssociations(List<Shipment> shipments) {
        shipments.forEach(shipment -> {
            shipment.getWarehouse().getName();
            shipment.getProducts().forEach(Product::getName);
        });
    }
}
