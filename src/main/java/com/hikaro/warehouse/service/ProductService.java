package com.hikaro.warehouse.service;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ProductQueryIndex productQueryIndex;

    public ProductService(
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            SupplierRepository supplierRepository,
            CategoryRepository categoryRepository,
            EntityManager entityManager,
            ProductQueryIndex productQueryIndex
    ) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
        this.entityManager = entityManager;
        this.productQueryIndex = productQueryIndex;
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findAllWithDetails(Pageable.unpaged())
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
    public Page<Product> findByName(String name, Pageable pageable) {
        String filter = normalizeName(name);
        if (filter == null) {
            return productRepository.findAllWithDetails(pageable);
        }
        return productRepository.findAllWithDetailsByName(toLikePattern(filter), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> demoNplusOne(String name, Pageable pageable) {
        entityManager.clear();
        Page<Product> products;
        if (isBlank(name)) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByNameContainingIgnoreCase(name, pageable);
        }
        initializeAssociations(products.getContent());
        return products;
    }

    @Transactional(readOnly = true)
    public Page<Product> findByNameWithEntityGraph(String name, Pageable pageable) {
        entityManager.clear();
        String filter = normalizeName(name);
        if (filter == null) {
            return productRepository.findAllWithDetails(pageable);
        }
        return productRepository.findAllWithDetailsByName(toLikePattern(filter), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByNameAndCategoryWithJpql(
            String name,
            String categoryName,
            Pageable pageable
    ) {
        String nameFilter = normalizeName(name);
        String categoryFilter = normalizeName(categoryName);
        if (nameFilter == null && categoryFilter == null) {
            return productRepository.findAllWithDetails(pageable);
        }
        return productRepository.findAllWithDetailsByNameAndCategoryJpql(
                toLikePattern(nameFilter),
                toLikePattern(categoryFilter),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<Product> findByNameAndCategoryWithNativeQuery(
            String name,
            String categoryName,
            Pageable pageable
    ) {
        String nameFilter = normalizeName(name);
        String categoryFilter = normalizeName(categoryName);
        if (nameFilter == null && categoryFilter == null) {
            return productRepository.findAllWithDetails(pageable);
        }
        Page<Product> products = productRepository.findAllWithDetailsByNameAndCategoryNative(
                toLikePattern(nameFilter),
                toLikePattern(categoryFilter),
                pageable
        );
        initializeAssociations(products.getContent());
        return products;
    }

    public Product create(ProductRequestDto request) {
        Product product = new Product();
        applyRequest(product, request);
        Product savedProduct = productRepository.save(product);
        productQueryIndex.invalidate();
        return savedProduct;
    }

    public Product update(Long id, ProductRequestDto request) {
        Product product = productRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                PRODUCT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX
                        )
                );
        applyRequest(product, request);
        Product savedProduct = productRepository.save(product);
        productQueryIndex.invalidate();
        return savedProduct;
    }

    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                PRODUCT_WITH_ID_PREFIX + id + NOT_FOUND_SUFFIX
                        )
                );
        productRepository.delete(product);
        productQueryIndex.invalidate();
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

    private String toLikePattern(String value) {
        if (value == null) {
            return null;
        }
        return "%" + value.toLowerCase(Locale.ROOT) + "%";
    }

    private void initializeAssociations(List<Product> products) {
        products.forEach(product -> {
            product.getWarehouse().getName();
            product.getSupplier().getName();
            product.getCategories().size();
        });
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
