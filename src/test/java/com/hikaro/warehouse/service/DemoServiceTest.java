package com.hikaro.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import com.hikaro.warehouse.dto.ProductRequestDto;
import com.hikaro.warehouse.entity.Category;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.entity.Supplier;
import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.repository.CategoryRepository;
import com.hikaro.warehouse.repository.ProductRepository;
import com.hikaro.warehouse.repository.ShipmentRepository;
import com.hikaro.warehouse.repository.SupplierRepository;
import com.hikaro.warehouse.repository.WarehouseRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private DemoService demoService;

    @Test
    void shouldThrowAfterBulkSaveWithoutTransaction() {
        List<ProductRequestDto> requests = List.of(
                new ProductRequestDto("SKU-1", "Mouse", 4, 1L, 2L, List.of(3L))
        );
        when(productService.createBulk(requests)).thenReturn(List.of(new Product(11L, "SKU-1", "Mouse", 4)));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> demoService.saveProductsBulkWithoutTransaction(requests)
        );

        assertEquals("Intentional failure after bulk product save without transaction", exception.getMessage());
        verify(productService).createBulk(requests);
    }

    @Test
    void shouldThrowAfterBulkSaveWithTransaction() {
        List<ProductRequestDto> requests = List.of(
                new ProductRequestDto("SKU-2", "Keyboard", 8, 1L, 2L, List.of(3L))
        );
        when(productService.createBulk(requests)).thenReturn(List.of(new Product(12L, "SKU-2", "Keyboard", 8)));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> demoService.saveProductsBulkWithTransaction(requests)
        );

        assertEquals("Intentional failure after bulk product save with transaction", exception.getMessage());
        verify(productService).createBulk(requests);
    }

    @Test
    void shouldKeepExistingGraphFailureContract() {
        BulkOperationRequestDto request = new BulkOperationRequestDto(
                "Supplier",
                "mail@example.com",
                "Warehouse",
                "Street 1",
                "Product",
                "SKU-9",
                5,
                List.of()
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> demoService.saveGraphWithoutTransaction(request)
        );

        assertEquals("Intentional failure after saving supplier, warehouse and product", exception.getMessage());
    }

    @Test
    void shouldHandleNullCategoryIdsDuringGraphSave() {
        BulkOperationRequestDto request = new BulkOperationRequestDto(
                "Supplier",
                "mail@example.com",
                "Warehouse",
                "Street 1",
                "Product",
                "SKU-10",
                5,
                null
        );

        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> demoService.saveGraphWithoutTransaction(request)
        );

        assertEquals("Intentional failure after saving supplier, warehouse and product", exception.getMessage());
    }

    @Test
    void shouldSaveGraphWithCategoriesBeforeIntentionalTransactionalFailure() {
        BulkOperationRequestDto request = new BulkOperationRequestDto(
                "Supplier",
                "mail@example.com",
                "Warehouse",
                "Street 1",
                "Product",
                "SKU-11",
                5,
                List.of(3L)
        );
        Category category = new Category(3L, "Peripherals", "Peripherals");

        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(categoryRepository.findAllById(List.of(3L))).thenReturn(List.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> demoService.saveGraphWithTransaction(request)
        );

        assertEquals("Intentional failure after saving supplier, warehouse and product", exception.getMessage());
        verify(supplierRepository).save(any(Supplier.class));
        verify(warehouseRepository).save(any(Warehouse.class));
        verify(categoryRepository).findAllById(List.of(3L));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowWhenSomeCategoriesAreMissingDuringGraphSave() {
        BulkOperationRequestDto request = new BulkOperationRequestDto(
                "Supplier",
                "mail@example.com",
                "Warehouse",
                "Street 1",
                "Product",
                "SKU-12",
                5,
                List.of(3L, 4L)
        );

        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(categoryRepository.findAllById(List.of(3L, 4L))).thenReturn(List.of(new Category(3L, "Peripherals", "Peripherals")));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> demoService.saveGraphWithoutTransaction(request)
        );

        assertEquals("One or more categories not found", exception.getMessage());
        verify(categoryRepository).findAllById(List.of(3L, 4L));
    }
}
