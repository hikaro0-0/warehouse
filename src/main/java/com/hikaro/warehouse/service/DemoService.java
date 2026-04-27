package com.hikaro.warehouse.service;

import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import com.hikaro.warehouse.dto.ProductRequestDto;
import com.hikaro.warehouse.entity.Category;
import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.entity.Supplier;
import com.hikaro.warehouse.entity.Warehouse;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.repository.CategoryRepository;
import com.hikaro.warehouse.repository.ProductRepository;
import com.hikaro.warehouse.repository.SupplierRepository;
import com.hikaro.warehouse.repository.WarehouseRepository;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoService {

    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    @Value("${app.demo.async-task-running-delay-ms:5000}")
    private long asyncTaskRunningDelayMs;

    public DemoService(
            SupplierRepository supplierRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductService productService
    ) {
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    public void saveGraphWithoutTransaction(BulkOperationRequestDto request) {
        saveRelatedEntities(request, true);
    }

    @Transactional
    public void saveGraphWithTransaction(BulkOperationRequestDto request) {
        saveRelatedEntities(request, true);
    }

    @Transactional
    public void saveGraphForAsyncTask(BulkOperationRequestDto request) {
        saveRelatedEntities(request, false);
        pauseBeforeAsyncTaskCompletion();
    }

    public void saveProductsBulkWithoutTransaction(List<ProductRequestDto> requests) {
        productService.createBulk(requests);
        throw new IllegalStateException(
                "Intentional failure after bulk product save without transaction"
        );
    }

    @Transactional
    public void saveProductsBulkWithTransaction(List<ProductRequestDto> requests) {
        productService.createBulk(requests);
        throw new IllegalStateException(
                "Intentional failure after bulk product save with transaction"
        );
    }

    private void saveRelatedEntities(
            BulkOperationRequestDto request,
            boolean failAfterSave
    ) {
        Supplier supplier = supplierRepository.save(
                new Supplier(null, request.supplierName(), request.contactEmail())
        );
        Warehouse warehouse = warehouseRepository.save(
                new Warehouse(null, request.warehouseName(), request.warehouseAddress())
        );

        Product product = new Product(
                null,
                request.sku(),
                request.productName(),
                request.quantity()
        );
        product.setSupplier(supplier);
        product.setWarehouse(warehouse);
        product.setCategories(loadCategories(request.categoryIds()));
        productRepository.save(product);

        if (failAfterSave) {
            throw new IllegalStateException(
                    "Intentional failure after saving supplier, warehouse and product"
            );
        }
    }

    private Set<Category> loadCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<Category> categories = new LinkedHashSet<>(categoryRepository.findAllById(ids));
        if (categories.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more categories not found");
        }
        return categories;
    }

    private void pauseBeforeAsyncTaskCompletion() {
        if (asyncTaskRunningDelayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(asyncTaskRunningDelayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Async task delay interrupted after "
                            + Duration.ofMillis(asyncTaskRunningDelayMs),
                    ex
            );
        }
    }
}
