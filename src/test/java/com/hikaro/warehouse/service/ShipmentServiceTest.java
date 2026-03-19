package com.hikaro.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hikaro.warehouse.dto.ShipmentRequestDto;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.entity.Shipment;
import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.repository.ProductRepository;
import com.hikaro.warehouse.repository.ShipmentRepository;
import com.hikaro.warehouse.repository.WarehouseRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    @Test
    void shouldHandleShipmentCrudOperations() {
        Shipment shipment = new Shipment(10L, "REF-1");
        Warehouse warehouse = new Warehouse(3L, "Main", "Street 1");
        Product product = new Product(7L, "SKU-7", "Keyboard", 5);
        shipment.setWarehouse(warehouse);
        shipment.setProducts(Set.of(product));
        ShipmentRequestDto request = new ShipmentRequestDto("REF-2", 3L, List.of(7L));

        when(shipmentRepository.findAll()).thenReturn(List.of(shipment));
        when(shipmentRepository.findById(10L)).thenReturn(Optional.of(shipment));
        when(warehouseRepository.findById(3L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findAllById(List.of(7L))).thenReturn(List.of(product));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(List.of(shipment), shipmentService.findAll());
        assertSame(shipment, shipmentService.getById(10L));

        Shipment created = shipmentService.create(request);
        assertEquals("REF-2", created.getReferenceNumber());
        assertSame(warehouse, created.getWarehouse());
        assertEquals(Set.of(product), created.getProducts());

        Shipment updated = shipmentService.update(10L, request);
        assertSame(shipment, updated);
        assertEquals("REF-2", updated.getReferenceNumber());

        shipmentService.delete(10L);
        verify(shipmentRepository).delete(shipment);
    }

    @Test
    void shouldThrowWhenShipmentMissing() {
        when(shipmentRepository.findById(44L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> shipmentService.getById(44L)
        );

        assertEquals("Shipment with id 44 not found", exception.getMessage());
    }

    @Test
    void shouldThrowWhenWarehouseMissingDuringCreate() {
        ShipmentRequestDto request = new ShipmentRequestDto("REF-9", 2L, List.of(1L));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> shipmentService.create(request)
        );

        assertEquals("Warehouse with id 2 not found", exception.getMessage());
    }

    @Test
    void shouldThrowWhenAnyProductMissingDuringCreate() {
        Warehouse warehouse = new Warehouse(2L, "Main", "Street 1");
        Product product = new Product(1L, "SKU-1", "Mouse", 2);
        ShipmentRequestDto request = new ShipmentRequestDto("REF-9", 2L, List.of(1L, 5L));

        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findAllById(List.of(1L, 5L))).thenReturn(List.of(product));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> shipmentService.create(request)
        );

        assertEquals("One or more products not found", exception.getMessage());
    }
}
