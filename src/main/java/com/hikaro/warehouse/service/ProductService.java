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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
        return findByIdWithDetails(id);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByName(String name, Pageable pageable) {
        return Optional.ofNullable(normalizeName(name))
                .map(filter -> productRepository.findAllWithDetailsByName(toLikePattern(filter), pageable))
                .orElseGet(() -> productRepository.findAllWithDetails(pageable));
    }

    @Transactional(readOnly = true)
    public Page<Product> demoNplusOne(String name, Pageable pageable) {
        entityManager.clear();
        Page<Product> products = Optional.ofNullable(normalizeName(name))
                .map(filter -> productRepository.findByNameContainingIgnoreCase(filter, pageable))
                .orElseGet(() -> productRepository.findAll(pageable));
        initializeAssociations(products.getContent());
        return products;
    }

    @Transactional(readOnly = true)
    public Page<Product> findByNameWithEntityGraph(String name, Pageable pageable) {
        entityManager.clear();
        return Optional.ofNullable(normalizeName(name))
                .map(filter -> productRepository.findAllWithDetailsByName(toLikePattern(filter), pageable))
                .orElseGet(() -> productRepository.findAllWithDetails(pageable));
    }

    @Transactional(readOnly = true)
    public Page<Product> findByNameAndCategoryWithJpql(
            String name,
            String categoryName,
            Pageable pageable
    ) {
        return productRepository.findAllWithDetailsByNameAndCategoryJpql(
                toLikePattern(requireFilter(name, "name")),
                toLikePattern(requireFilter(categoryName, "categoryName")),
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

    @Transactional
    public Product create(ProductRequestDto request) {
        Product savedProduct = productRepository.save(buildProduct(request));
        productQueryIndex.invalidate();
        return findByIdWithDetails(savedProduct.getId());
    }

    @Transactional
    public List<Product> createBulk(List<ProductRequestDto> requests) {
        List<Product> savedProducts = productRepository.saveAll(
                requests.stream()
                        .map(this::buildProduct)
                        .toList()
        );
        productQueryIndex.invalidate();
        return savedProducts.stream()
                .map(Product::getId)
                .map(this::findByIdWithDetails)
                .toList();
    }

    @Transactional
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
        return findByIdWithDetails(savedProduct.getId());
    }

    @Transactional
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

    private Product findByIdWithDetails(Long id) {
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

    private Product buildProduct(ProductRequestDto request) {
        Product product = new Product();
        applyRequest(product, request);
        return product;
    }

    private void applyRequest(Product product, ProductRequestDto request) {
        Warehouse warehouse = findWarehouse(request.warehouseId());
        Supplier supplier = findSupplier(request.supplierId());
        Set<Category> categories = loadCategories(request.categoryIds());

        product.setSku(request.sku());
        product.setName(request.name());
        product.setQuantity(request.quantity());
        product.setWarehouse(warehouse);
        product.setSupplier(supplier);
        product.setCategories(categories);
    }

    private Warehouse findWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Warehouse with id " + warehouseId + NOT_FOUND_SUFFIX
                        )
                );
    }

    private Supplier findSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Supplier with id " + supplierId + NOT_FOUND_SUFFIX
                        )
                );
    }

    private Set<Category> loadCategories(List<Long> categoryIds) {
        List<Long> requestedCategoryIds = Optional.ofNullable(categoryIds).orElseGet(List::of);
        if (requestedCategoryIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        Set<Category> categories = categoryRepository.findAllById(requestedCategoryIds)
                .stream()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (categories.size() != requestedCategoryIds.size()) {
            throw new ResourceNotFoundException("One or more categories not found");
        }
        return categories;
    }

    private String normalizeName(String name) {
        return Optional.ofNullable(name)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElse(null);
    }

    private String requireFilter(String value, String fieldName) {
        return Optional.ofNullable(normalizeName(value))
                .orElseThrow(() -> new IllegalArgumentException(fieldName + " must not be blank"));
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
            product.getCategories().forEach(Category::getName);
        });
    }
}
