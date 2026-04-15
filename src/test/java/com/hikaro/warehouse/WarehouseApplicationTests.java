package com.hikaro.warehouse;

import com.hikaro.warehouse.controller.ProductController;
import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import com.hikaro.warehouse.dto.ProductRequestDto;
import com.hikaro.warehouse.dto.ProductResponseDto;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.repository.ProductRepository;
import com.hikaro.warehouse.repository.ShipmentRepository;
import com.hikaro.warehouse.repository.SupplierRepository;
import com.hikaro.warehouse.repository.WarehouseRepository;
import com.hikaro.warehouse.service.DemoService;
import com.hikaro.warehouse.service.ProductService;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class WarehouseApplicationTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductController productController;

    @Autowired
    private DemoService demoService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void contextLoads() {
        Assertions.assertTrue(productRepository.count() >= 4);
    }

    @Test
    void shouldReturnNPlusOneDemoWithoutLazyInitializationFailure() {
        Page<ProductResponseDto> response = productController.demoNplusOne(null, PageRequest.of(0, 10));

        Assertions.assertFalse(response.isEmpty());
        Assertions.assertNotNull(response.getContent().getFirst().warehouseName());
        Assertions.assertNotNull(response.getContent().getFirst().supplierName());
    }

    @Test
    @Transactional
    void shouldReduceQueryCountWithEntityGraph() {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

        statistics.clear();
        List<Product> plainProducts = productService.demoNplusOne(null, PageRequest.of(0, 10)).getContent();
        plainProducts.forEach(product -> product.getCategories().size());
        long nPlusOneStatements = statistics.getPrepareStatementCount();

        statistics.clear();
        List<Product> optimizedProducts = productService.findByNameWithEntityGraph(null, PageRequest.of(0, 10)).getContent();
        optimizedProducts.forEach(product -> product.getCategories().size());
        long optimizedStatements = statistics.getPrepareStatementCount();

        Assertions.assertTrue(nPlusOneStatements > optimizedStatements);
    }

    @Test
    void shouldReturnPagedProducts() {
        Page<Product> products = productService.findByName(null, PageRequest.of(0, 2));

        Assertions.assertEquals(2, products.getSize());
        Assertions.assertEquals(4, products.getTotalElements());
        Assertions.assertEquals(2, products.getNumberOfElements());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldInvalidateIndexAfterProductUpdate() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ProductResponseDto> cachedResponse = productController.findByName("Monitor", pageable);

        Assertions.assertEquals(1, cachedResponse.getTotalElements());
        Assertions.assertEquals("Monitor", cachedResponse.getContent().getFirst().name());

        productService.update(4L, new ProductRequestDto(
                "SKU-103",
                "Display",
                15,
                2L,
                1L,
                List.of(1L, 3L)
        ));

        Page<ProductResponseDto> monitorResponse = productController.findByName("Monitor", pageable);
        Page<ProductResponseDto> displayResponse = productController.findByName("Display", pageable);

        Assertions.assertEquals(0, monitorResponse.getTotalElements());
        Assertions.assertEquals(1, displayResponse.getTotalElements());
        Assertions.assertEquals("Display", displayResponse.getContent().getFirst().name());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldPersistDataPartiallyWithoutTransactional() {
        long suppliersBefore = supplierRepository.count();
        long warehousesBefore = warehouseRepository.count();
        long productsBefore = productRepository.count();
        long shipmentsBefore = shipmentRepository.count();

        BulkOperationRequestDto request = buildBulkRequest("SKU-NON-TX", "non-tx@example.com");

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> demoService.saveGraphWithoutTransaction(request)
        );

        Assertions.assertEquals(suppliersBefore + 1, supplierRepository.count());
        Assertions.assertEquals(warehousesBefore + 1, warehouseRepository.count());
        Assertions.assertEquals(productsBefore + 1, productRepository.count());
        Assertions.assertEquals(shipmentsBefore, shipmentRepository.count());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldRollbackAllChangesWithTransactional() {
        long suppliersBefore = supplierRepository.count();
        long warehousesBefore = warehouseRepository.count();
        long productsBefore = productRepository.count();
        long shipmentsBefore = shipmentRepository.count();

        BulkOperationRequestDto request = buildBulkRequest("SKU-TX", "tx@example.com");

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> demoService.saveGraphWithTransaction(request)
        );

        Assertions.assertEquals(suppliersBefore, supplierRepository.count());
        Assertions.assertEquals(warehousesBefore, warehouseRepository.count());
        Assertions.assertEquals(productsBefore, productRepository.count());
        Assertions.assertEquals(shipmentsBefore, shipmentRepository.count());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldPersistBulkProductsWithoutOuterTransaction() {
        long productsBefore = productRepository.count();

        List<ProductRequestDto> requests = buildProductBulkRequests("NO-TX");

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> demoService.saveProductsBulkWithoutTransaction(requests)
        );

        Assertions.assertEquals(productsBefore + 2, productRepository.count());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldRollbackBulkProductsWithOuterTransaction() {
        long productsBefore = productRepository.count();

        List<ProductRequestDto> requests = buildProductBulkRequests("TX");

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> demoService.saveProductsBulkWithTransaction(requests)
        );

        Assertions.assertEquals(productsBefore, productRepository.count());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldSupportProductCrudOperations() {
        Product createdProduct = productService.create(new ProductRequestDto(
                "SKU-777",
                "Docking Station",
                18,
                1L,
                1L,
                List.of(1L, 3L)
        ));

        Assertions.assertNotNull(createdProduct.getId());
        Assertions.assertEquals("Docking Station", productService.getById(createdProduct.getId()).getName());

        productService.update(createdProduct.getId(), new ProductRequestDto(
                "SKU-777",
                "Docking Station Pro",
                20,
                2L,
                2L,
                List.of(1L, 2L)
        ));

        Assertions.assertEquals("Docking Station Pro", productService.getById(createdProduct.getId()).getName());

        productService.delete(createdProduct.getId());
        Assertions.assertFalse(productRepository.findById(createdProduct.getId()).isPresent());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateProductsInBulk() {
        List<ProductRequestDto> requests = buildProductBulkRequests("PLAIN");

        List<Product> createdProducts = productService.createBulk(requests);

        Assertions.assertEquals(2, createdProducts.size());
        Assertions.assertEquals(
                List.of("Keyboard PLAIN", "Mouse PLAIN"),
                createdProducts.stream().map(Product::getName).sorted().toList()
        );
    }

    @Test
    void shouldFilterProductsByNestedCategoryUsingJpql() {
        Page<Product> products = productService.findByNameAndCategoryWithJpql(
                "top",
                "Premium",
                PageRequest.of(0, 10)
        );

        Assertions.assertEquals(1, products.getTotalElements());
        Assertions.assertEquals("Laptop", products.getContent().getFirst().getName());
    }

    @Test
    void shouldFilterProductsByNestedCategoryUsingNativeQuery() {
        Page<Product> products = productService.findByNameAndCategoryWithNativeQuery(
                "mon",
                "Premium",
                PageRequest.of(0, 10)
        );

        Assertions.assertEquals(1, products.getTotalElements());
        Assertions.assertEquals("Monitor", products.getContent().getFirst().getName());
    }

    private BulkOperationRequestDto buildBulkRequest(String sku, String email) {
        return new BulkOperationRequestDto(
                "Bulk Supplier " + sku,
                email,
                "Bulk Warehouse " + sku,
                "Failure street 1",
                "Stress Product " + sku,
                sku,
                5,
                List.of(1L, 2L)
        );
    }

    private List<ProductRequestDto> buildProductBulkRequests(String suffix) {
        return List.of(
                new ProductRequestDto(
                        "SKU-" + suffix + "-1",
                        "Mouse " + suffix,
                        5,
                        1L,
                        1L,
                        List.of(1L, 3L)
                ),
                new ProductRequestDto(
                        "SKU-" + suffix + "-2",
                        "Keyboard " + suffix,
                        7,
                        2L,
                        2L,
                        List.of(2L)
                )
        );
    }
}
