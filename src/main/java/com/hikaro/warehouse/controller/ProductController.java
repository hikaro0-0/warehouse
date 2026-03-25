package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.ProductRequestDto;
import com.hikaro.warehouse.dto.ProductResponseDto;
import com.hikaro.warehouse.index.ProductQueryIndex;
import com.hikaro.warehouse.index.ProductQueryIndexKey;
import com.hikaro.warehouse.mapper.ProductMapper;
import com.hikaro.warehouse.service.ProductService;
import java.util.function.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final ProductQueryIndex productQueryIndex;

    public ProductController(
            ProductService productService,
            ProductMapper productMapper,
            ProductQueryIndex productQueryIndex
    ) {
        this.productService = productService;
        this.productMapper = productMapper;
        this.productQueryIndex = productQueryIndex;
    }

    @GetMapping("/{id}")
    public ProductResponseDto getById(@PathVariable Long id) {
        ProductQueryIndexKey key = ProductQueryIndexKey.byId(id);
        return productQueryIndex.getProduct(key)
                .orElseGet(() -> {
                    ProductResponseDto response = productMapper.toResponseDto(productService.getById(id));
                    productQueryIndex.put(key, response);
                    return response;
                });
    }

    @GetMapping
    public Page<ProductResponseDto> findByName(
            @RequestParam(required = false) String name,
            Pageable pageable
    ) {
        return getCachedPage(
                ProductQueryIndexKey.byQuery("BY_NAME", name, null, pageable),
                () -> productService.findByName(name, pageable).map(productMapper::toResponseDto)
        );
    }

    @GetMapping("/n-plus-one")
    public Page<ProductResponseDto> demoNplusOne(
            @RequestParam(required = false) String name,
            Pageable pageable
    ) {
        return getCachedPage(
                ProductQueryIndexKey.byQuery("N_PLUS_ONE", name, null, pageable),
                () -> productService.demoNplusOne(name, pageable).map(productMapper::toResponseDto)
        );
    }

    @GetMapping("/optimized")
    public Page<ProductResponseDto> findWithEntityGraph(
            @RequestParam(required = false) String name,
            Pageable pageable
    ) {
        return getCachedPage(
                ProductQueryIndexKey.byQuery("OPTIMIZED", name, null, pageable),
                () -> productService.findByNameWithEntityGraph(name, pageable).map(productMapper::toResponseDto)
        );
    }

    @GetMapping("/search/jpql")
    public Page<ProductResponseDto> findByNameAndCategoryWithJpql(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryName,
            Pageable pageable
    ) {
        return getCachedPage(
                ProductQueryIndexKey.byQuery("SEARCH_JPQL", name, categoryName, pageable),
                () -> productService.findByNameAndCategoryWithJpql(name, categoryName, pageable)
                        .map(productMapper::toResponseDto)
        );
    }

    @GetMapping("/search/native")
    public Page<ProductResponseDto> findByNameAndCategoryWithNativeQuery(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryName,
            Pageable pageable
    ) {
        return getCachedPage(
                ProductQueryIndexKey.byQuery("SEARCH_NATIVE", name, categoryName, pageable),
                () -> productService.findByNameAndCategoryWithNativeQuery(name, categoryName, pageable)
                        .map(productMapper::toResponseDto)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDto create(@RequestBody ProductRequestDto request) {
        return productMapper.toResponseDto(productService.create(request));
    }

    @PutMapping("/{id}")
    public ProductResponseDto update(
            @PathVariable Long id,
            @RequestBody ProductRequestDto request
    ) {
        return productMapper.toResponseDto(
                productService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    private Page<ProductResponseDto> getCachedPage(
            ProductQueryIndexKey key,
            Supplier<Page<ProductResponseDto>> loader
    ) {
        return productQueryIndex.getProductPage(key)
                .orElseGet(() -> {
                    Page<ProductResponseDto> loadedPage = loader.get();
                    productQueryIndex.put(key, loadedPage);
                    return loadedPage;
                });
    }
}
