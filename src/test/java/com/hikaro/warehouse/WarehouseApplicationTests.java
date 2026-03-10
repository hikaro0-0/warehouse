package com.hikaro.warehouse;

import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import com.hikaro.warehouse.dto.ProductRequestDto;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class WarehouseApplicationTests {

    @Autowired
    private ProductService productService;

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
    @Transactional
    void shouldReduceQueryCountWithEntityGraph() {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

        statistics.clear();
        List<Product> plainProducts = productService.demoNplusOne(null);
        plainProducts.forEach(product -> product.getCategories().size());
        long nPlusOneStatements = statistics.getPrepareStatementCount();

        statistics.clear();
        List<Product> optimizedProducts = productService.findByNameWithEntityGraph(null);
        optimizedProducts.forEach(product -> product.getCategories().size());
        long optimizedStatements = statistics.getPrepareStatementCount();

        Assertions.assertTrue(nPlusOneStatements > optimizedStatements);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldPersistDataPartiallyWithoutTransactional() {
        long suppliersBefore = supplierRepository.count();
        long warehousesBefore = warehouseRepository.count();
        long productsBefore = productRepository.count();
        long shipmentsBefore = shipmentRepository.count();

        demoService.saveGraphWithoutTransaction(buildBulkRequest("SKU-NON-TX", "non-tx@example.com"));

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

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> demoService.saveGraphWithTransaction(buildBulkRequest("SKU-TX", "tx@example.com"))
        );

        Assertions.assertEquals(suppliersBefore, supplierRepository.count());
        Assertions.assertEquals(warehousesBefore, warehouseRepository.count());
        Assertions.assertEquals(productsBefore, productRepository.count());
        Assertions.assertEquals(shipmentsBefore, shipmentRepository.count());
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
}
