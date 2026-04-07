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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoService {

    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public DemoService(
            SupplierRepository supplierRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            com.hikaro.warehouse.repository.ShipmentRepository shipmentRepository,
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
        saveRelatedEntities(request);
    }

    @Transactional
    public void saveGraphWithTransaction(BulkOperationRequestDto request) {
        saveRelatedEntities(request);
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

    private void saveRelatedEntities(BulkOperationRequestDto request) {
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

        throw new IllegalStateException(
                "Intentional failure after saving supplier, warehouse and product"
        );
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
}
