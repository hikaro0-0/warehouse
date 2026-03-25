package com.hikaro.warehouse.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.hikaro.warehouse.dto.CategoryRequestDto;
import com.hikaro.warehouse.dto.CategoryResponseDto;
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
import com.hikaro.warehouse.exception.ApiErrorResponse;
import com.hikaro.warehouse.exception.ApiExceptionHandler;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.mapper.ProductMapper;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class SupportClassesTest {

    @Test
    void shouldMapProductToResponseDto() {
        Warehouse warehouse = new Warehouse(1L, "Main", "Street 1");
        Supplier supplier = new Supplier(2L, "ACME", "acme@example.com");
        Category firstCategory = new Category(3L, "Cables", "Cables");
        Category secondCategory = new Category(4L, "Accessories", "Accessories");
        Product product = new Product(5L, "SKU-5", "Dock", 9);
        product.setWarehouse(warehouse);
        product.setSupplier(supplier);
        product.setCategories(new LinkedHashSet<>(List.of(firstCategory, secondCategory)));

        ProductResponseDto response = new ProductMapper().toResponseDto(product);

        assertEquals(5L, response.id());
        assertEquals("Main", response.warehouseName());
        assertEquals(List.of("Accessories", "Cables"), response.categories());
    }

    @Test
    void shouldBuildErrorResponses() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        ResponseEntity<ApiErrorResponse> notFound = handler.handleNotFound(
                new ResourceNotFoundException("missing"),
                request
        );
        ResponseEntity<ApiErrorResponse> badRequest = handler.handleBadRequest(
                new DataIntegrityViolationException("bad data"),
                request
        );

        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
        assertEquals("missing", notFound.getBody().message());
        assertNotNull(notFound.getBody().timestamp());
        assertEquals("/api/test", notFound.getBody().path());

        assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());
        assertEquals("bad data", badRequest.getBody().message());
        assertEquals(400, badRequest.getBody().status());
    }

    @Test
    void shouldExposeExceptionMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException("not here");
        assertEquals("not here", exception.getMessage());
    }

    @Test
    void shouldHandleEntityAccessors() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Hardware");
        category.setDescription("Devices");

        Shipment shipment = new Shipment();
        shipment.setId(2L);
        shipment.setReferenceNumber("REF-1");

        Supplier supplier = new Supplier();
        supplier.setId(3L);
        supplier.setName("ACME");
        supplier.setContactEmail("acme@example.com");
        supplier.setProducts(new LinkedHashSet<>());

        Warehouse warehouse = new Warehouse();
        warehouse.setId(4L);
        warehouse.setName("Main");
        warehouse.setAddress("Street 1");
        warehouse.setProducts(new LinkedHashSet<>());
        warehouse.setShipments(new LinkedHashSet<>());

        Product product = new Product();
        product.setId(5L);
        product.setSku("SKU-5");
        product.setName("Monitor");
        product.setQuantity(7);
        product.setWarehouse(warehouse);
        product.setSupplier(supplier);
        product.setCategories(new LinkedHashSet<>(List.of(category)));
        product.setShipments(new LinkedHashSet<>(List.of(shipment)));

        category.setProducts(new LinkedHashSet<>(List.of(product)));
        supplier.setProducts(new LinkedHashSet<>(List.of(product)));
        warehouse.setProducts(new LinkedHashSet<>(List.of(product)));
        warehouse.setShipments(new LinkedHashSet<>(List.of(shipment)));
        shipment.setWarehouse(warehouse);
        shipment.setProducts(new LinkedHashSet<>(List.of(product)));

        assertEquals(1L, category.getId());
        assertEquals("Hardware", category.getName());
        assertEquals("Devices", category.getDescription());
        assertEquals(1, category.getProducts().size());
        assertEquals(2L, shipment.getId());
        assertEquals("REF-1", shipment.getReferenceNumber());
        assertEquals(warehouse, shipment.getWarehouse());
        assertEquals(1, shipment.getProducts().size());
        assertEquals("ACME", supplier.getName());
        assertEquals("acme@example.com", supplier.getContactEmail());
        assertEquals(1, supplier.getProducts().size());
        assertEquals("Main", warehouse.getName());
        assertEquals("Street 1", warehouse.getAddress());
        assertEquals(1, warehouse.getProducts().size());
        assertEquals(1, warehouse.getShipments().size());
        assertEquals("SKU-5", product.getSku());
        assertEquals("Monitor", product.getName());
        assertEquals(7, product.getQuantity());
        assertEquals(warehouse, product.getWarehouse());
        assertEquals(supplier, product.getSupplier());
        assertEquals(1, product.getCategories().size());
        assertEquals(1, product.getShipments().size());
    }

    @Test
    void shouldInstantiateDtoRecords() {
        CategoryRequestDto categoryRequest = new CategoryRequestDto("Category", "Desc");
        CategoryResponseDto categoryResponse = new CategoryResponseDto(1L, "Category", "Desc");
        SupplierRequestDto supplierRequest = new SupplierRequestDto("Supplier", "mail@example.com");
        SupplierResponseDto supplierResponse = new SupplierResponseDto(2L, "Supplier", "mail@example.com");
        WarehouseRequestDto warehouseRequest = new WarehouseRequestDto("Warehouse", "Street");
        WarehouseResponseDto warehouseResponse = new WarehouseResponseDto(3L, "Warehouse", "Street");
        ShipmentRequestDto shipmentRequest = new ShipmentRequestDto("REF", 2L, List.of(1L, 2L));
        ShipmentResponseDto shipmentResponse = new ShipmentResponseDto(4L, "REF", 2L, "Warehouse", List.of(1L, 2L));

        assertEquals("Category", categoryRequest.name());
        assertEquals(1L, categoryResponse.id());
        assertEquals("mail@example.com", supplierRequest.contactEmail());
        assertEquals("Supplier", supplierResponse.name());
        assertEquals("Warehouse", warehouseRequest.name());
        assertEquals("Street", warehouseResponse.address());
        assertEquals("REF", shipmentRequest.referenceNumber());
        assertEquals(List.of(1L, 2L), shipmentResponse.productIds());
    }
}
