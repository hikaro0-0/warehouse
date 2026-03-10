package com.hikaro.warehouse.service;

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
import jakarta.persistence.EntityManager;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final String NOT_FOUND_SUFFIX = " not found";
    private static final String PRODUCT_WITH_ID_PREFIX = "Product with id ";

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;

    public ProductService(
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            SupplierRepository supplierRepository,
            CategoryRepository categoryRepository,
            EntityManager entityManager
    ) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findAllWithDetails(null)
                .stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                PRODUCT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Product> findByName(String name) {
        String filter = normalizeName(name);
        return productRepository.findAllWithDetails(filter);
    }

    @Transactional(readOnly = true)
    public List<Product> demoNplusOne(String name) {
        entityManager.clear();
        if (isBlank(name)) {
            return productRepository.findAll();
        }
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Product> findByNameWithEntityGraph(String name) {
        entityManager.clear();
        return productRepository.findAllWithDetails(normalizeName(name));
    }

    public Product create(ProductRequestDto request) {
        Product product = new Product();
        applyRequest(product, request);
        return productRepository.save(product);
    }

    public Product update(Long id, ProductRequestDto request) {
        Product product = productRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                PRODUCT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX
                        )
                );
        applyRequest(product, request);
        return productRepository.save(product);
    }

    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                PRODUCT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX
                        )
                );
        productRepository.delete(product);
    }

    private void applyRequest(Product product, ProductRequestDto request) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Warehouse with id "
                                        + request.warehouseId()
                                        + NOT_FOUND_SUFFIX
                        )
                );
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Supplier with id "
                                        + request.supplierId()
                                        + NOT_FOUND_SUFFIX
                        )
                );
        Set<Category> categories = loadCategories(request.categoryIds());

        product.setSku(request.sku());
        product.setName(request.name());
        product.setQuantity(request.quantity());
        product.setWarehouse(warehouse);
        product.setSupplier(supplier);
        product.setCategories(categories);
    }

    private Set<Category> loadCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<Category> categories = new LinkedHashSet<>(categoryRepository.findAllById(categoryIds));
        if (categories.size() != categoryIds.size()) {
            throw new ResourceNotFoundException("One or more categories not found");
        }
        return categories;
    }

    private String normalizeName(String name) {
        return isBlank(name) ? null : name;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
