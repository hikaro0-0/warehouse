package com.hikaro.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hikaro.warehouse.dto.ProductRequestDto;
import com.hikaro.warehouse.entity.Category;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.entity.Supplier;
import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.index.ProductQueryIndex;
import com.hikaro.warehouse.repository.CategoryRepository;
import com.hikaro.warehouse.repository.ProductRepository;
import com.hikaro.warehouse.repository.SupplierRepository;
import com.hikaro.warehouse.repository.WarehouseRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ProductQueryIndex productQueryIndex;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProductsInBulkAndInvalidateIndex() {
        Warehouse warehouse = new Warehouse(1L, "Main", "Street 1");
        Supplier supplier = new Supplier(2L, "ACME", "acme@example.com");
        Category category = new Category(3L, "Peripherals", "Peripherals");
        ProductRequestDto firstRequest = new ProductRequestDto("SKU-1", "Mouse", 4, 1L, 2L, List.of(3L));
        ProductRequestDto secondRequest = new ProductRequestDto("SKU-2", "Keyboard", 8, 1L, 2L, List.of(3L));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(supplier));
        when(categoryRepository.findAllById(List.of(3L))).thenReturn(List.of(category));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Product> products = invocation.getArgument(0);
            products.get(0).setId(11L);
            products.get(1).setId(12L);
            return products;
        });
        when(productRepository.findAllWithDetails(Pageable.unpaged())).thenAnswer(invocation -> {
            Product first = new Product(11L, "SKU-1", "Mouse", 4);
            first.setWarehouse(warehouse);
            first.setSupplier(supplier);
            first.setCategories(java.util.Set.of(category));
            Product second = new Product(12L, "SKU-2", "Keyboard", 8);
            second.setWarehouse(warehouse);
            second.setSupplier(supplier);
            second.setCategories(java.util.Set.of(category));
            return new PageImpl<>(List.of(first, second));
        });

        List<Product> products = productService.createBulk(List.of(firstRequest, secondRequest));

        assertEquals(2, products.size());
        assertEquals(List.of("Mouse", "Keyboard"), products.stream().map(Product::getName).toList());
        assertSame(warehouse, products.getFirst().getWarehouse());
        verify(productQueryIndex).invalidate();
    }

    @Test
    void shouldThrowWhenWarehouseMissingDuringBulkCreate() {
        ProductRequestDto request = new ProductRequestDto("SKU-9", "Dock", 2, 99L, 2L, List.of(3L));
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.createBulk(List.of(request))
        );

        assertEquals("Warehouse with id 99 not found", exception.getMessage());
    }

    @Test
    void shouldThrowWhenCategoryMissingDuringBulkCreate() {
        Warehouse warehouse = new Warehouse(1L, "Main", "Street 1");
        Supplier supplier = new Supplier(2L, "ACME", "acme@example.com");
        ProductRequestDto request = new ProductRequestDto("SKU-9", "Dock", 2, 1L, 2L, List.of(3L, 4L));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(supplier));
        when(categoryRepository.findAllById(List.of(3L, 4L))).thenReturn(List.of(new Category(3L, "Peripherals", "Peripherals")));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.createBulk(List.of(request))
        );

        assertEquals("One or more categories not found", exception.getMessage());
    }
}
