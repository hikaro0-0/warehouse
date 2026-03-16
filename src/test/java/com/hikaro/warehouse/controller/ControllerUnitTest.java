package com.hikaro.warehouse.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import com.hikaro.warehouse.dto.CategoryRequestDto;
import com.hikaro.warehouse.dto.CategoryResponseDto;
import com.hikaro.warehouse.dto.ProductRequestDto;
import com.hikaro.warehouse.dto.ProductResponseDto;
import com.hikaro.warehouse.dto.ShipmentRequestDto;
import com.hikaro.warehouse.dto.ShipmentResponseDto;
import com.hikaro.warehouse.dto.SupplierRequestDto;
import com.hikaro.warehouse.dto.SupplierResponseDto;
import com.hikaro.warehouse.dto.WarehouseRequestDto;
import com.hikaro.warehouse.dto.WarehouseResponseDto;
import com.hikaro.warehouse.entity.Category;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.entity.Shipment;
import com.hikaro.warehouse.entity.Supplier;
import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.mapper.ProductMapper;
import com.hikaro.warehouse.service.CategoryService;
import com.hikaro.warehouse.service.DemoService;
import com.hikaro.warehouse.service.ProductService;
import com.hikaro.warehouse.service.ShipmentService;
import com.hikaro.warehouse.service.SupplierService;
import com.hikaro.warehouse.service.WarehouseService;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ControllerUnitTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private SupplierService supplierService;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private DemoService demoService;

    @InjectMocks
    private CategoryController categoryController;

    @InjectMocks
    private SupplierController supplierController;

    @InjectMocks
    private WarehouseController warehouseController;

    @InjectMocks
    private ShipmentController shipmentController;

    @InjectMocks
    private ProductController productController;

    @InjectMocks
    private DemoController demoController;

    @Test
    void categoryControllerShouldMapRequestsAndResponses() {
        Category category = new Category(1L, "Hardware", "Devices");
        CategoryRequestDto request = new CategoryRequestDto("Hardware", "Devices");

        when(categoryService.findAll()).thenReturn(List.of(category));
        when(categoryService.getById(1L)).thenReturn(category);
        when(categoryService.create(org.mockito.ArgumentMatchers.any(Category.class))).thenReturn(category);
        when(categoryService.update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Category.class)))
                .thenReturn(category);

        List<CategoryResponseDto> all = categoryController.findAll();
        assertEquals(1, all.size());
        assertEquals("Hardware", all.getFirst().name());
        assertEquals(category.getId(), categoryController.getById(1L).id());
        assertEquals("Devices", categoryController.create(request).description());
        assertEquals("Hardware", categoryController.update(1L, request).name());

        categoryController.delete(1L);
        verify(categoryService).delete(1L);
    }

    @Test
    void supplierControllerShouldMapRequestsAndResponses() {
        Supplier supplier = new Supplier(1L, "ACME", "acme@example.com");
        SupplierRequestDto request = new SupplierRequestDto("ACME", "acme@example.com");

        when(supplierService.findAll()).thenReturn(List.of(supplier));
        when(supplierService.getById(1L)).thenReturn(supplier);
        when(supplierService.create(org.mockito.ArgumentMatchers.any(Supplier.class))).thenReturn(supplier);
        when(supplierService.update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Supplier.class)))
                .thenReturn(supplier);

        List<SupplierResponseDto> all = supplierController.findAll();
        assertEquals("ACME", all.getFirst().name());
        assertEquals("acme@example.com", supplierController.getById(1L).contactEmail());
        assertEquals(1L, supplierController.create(request).id());
        assertEquals("ACME", supplierController.update(1L, request).name());

        supplierController.delete(1L);
        verify(supplierService).delete(1L);
    }

    @Test
    void warehouseControllerShouldMapRequestsAndResponses() {
        Warehouse warehouse = new Warehouse(1L, "Main", "Street 1");
        WarehouseRequestDto request = new WarehouseRequestDto("Main", "Street 1");

        when(warehouseService.findAll()).thenReturn(List.of(warehouse));
        when(warehouseService.getById(1L)).thenReturn(warehouse);
        when(warehouseService.create(org.mockito.ArgumentMatchers.any(Warehouse.class))).thenReturn(warehouse);
        when(warehouseService.update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(Warehouse.class)))
                .thenReturn(warehouse);

        List<WarehouseResponseDto> all = warehouseController.findAll();
        assertEquals("Main", all.getFirst().name());
        assertEquals("Street 1", warehouseController.getById(1L).address());
        assertEquals(1L, warehouseController.create(request).id());
        assertEquals("Main", warehouseController.update(1L, request).name());

        warehouseController.delete(1L);
        verify(warehouseService).delete(1L);
    }

    @Test
    void shipmentControllerShouldMapRequestsAndResponses() {
        Supplier supplier = new Supplier(2L, "ACME", "acme@example.com");
        Product firstProduct = new Product(5L, "SKU-5", "Mouse", 2);
        Product secondProduct = new Product(3L, "SKU-3", "Keyboard", 4);
        Shipment shipment = new Shipment(7L, "REF-7");
        shipment.setSupplier(supplier);
        shipment.setProducts(new LinkedHashSet<>(List.of(firstProduct, secondProduct)));
        ShipmentRequestDto request = new ShipmentRequestDto("REF-7", 2L, List.of(5L, 3L));

        when(shipmentService.findAll()).thenReturn(List.of(shipment));
        when(shipmentService.getById(7L)).thenReturn(shipment);
        when(shipmentService.create(request)).thenReturn(shipment);
        when(shipmentService.update(7L, request)).thenReturn(shipment);

        List<ShipmentResponseDto> all = shipmentController.findAll();
        assertIterableEquals(List.of(3L, 5L), all.getFirst().productIds());
        assertEquals("ACME", shipmentController.getById(7L).supplierName());
        assertEquals("REF-7", shipmentController.create(request).referenceNumber());
        assertEquals(7L, shipmentController.update(7L, request).id());

        shipmentController.delete(7L);
        verify(shipmentService).delete(7L);
    }

    @Test
    void productControllerShouldDelegateAllEndpoints() {
        Product product = new Product(1L, "SKU-1", "Monitor", 5);
        ProductRequestDto request = new ProductRequestDto("SKU-1", "Monitor", 5, 1L, 2L, List.of(3L));
        ProductResponseDto response = new ProductResponseDto(
                1L, "SKU-1", "Monitor", 5, 1L, "Main", 2L, "ACME", List.of("Displays")
        );

        when(productService.getById(1L)).thenReturn(product);
        when(productService.findByName("Mon")).thenReturn(List.of(product));
        when(productService.demoNplusOne(null)).thenReturn(List.of(product));
        when(productService.findByNameWithEntityGraph("Mon")).thenReturn(List.of(product));
        when(productService.create(request)).thenReturn(product);
        when(productService.update(1L, request)).thenReturn(product);
        when(productMapper.toResponseDto(product)).thenReturn(response);

        assertEquals(response, productController.getById(1L));
        assertEquals(List.of(response), productController.findByName("Mon"));
        assertEquals(List.of(response), productController.demoNplusOne(null));
        assertEquals(List.of(response), productController.findWithEntityGraph("Mon"));
        assertEquals(response, productController.create(request));
        assertEquals(response, productController.update(1L, request));

        productController.delete(1L);
        verify(productService).delete(1L);
    }

    @Test
    void demoControllerShouldPropagateFailuresForScenarioScreenshots() {
        BulkOperationRequestDto request = new BulkOperationRequestDto(
                "Supplier", "mail@example.com", "Warehouse", "Street", "Product", "SKU", 3, List.of(1L, 2L)
        );

        assertDoesNotThrow(() -> { demoController.withoutTransaction(request); });
        verify(demoService).saveGraphWithoutTransaction(request);

        doThrow(new IllegalStateException("boom")).when(demoService).saveGraphWithTransaction(request);
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> demoController.withTransaction(request)
        );
        assertEquals("boom", exception.getMessage());
    }
}
