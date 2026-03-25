package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.ProductRequestDto;
import com.hikaro.warehouse.dto.ProductResponseDto;
import com.hikaro.warehouse.exception.ApiErrorResponse;
import com.hikaro.warehouse.index.ProductQueryIndex;
import com.hikaro.warehouse.index.ProductQueryIndexKey;
import com.hikaro.warehouse.mapper.ProductMapper;
import com.hikaro.warehouse.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Products", description = "Product catalog and search endpoints")
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
    @Operation(summary = "Get product by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
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
    @Operation(summary = "Get paged products, optionally filtered by name")
    @ApiResponse(responseCode = "200", description = "Products fetched successfully")
    public Page<ProductResponseDto> findByName(
            @Parameter(description = "Optional product name filter", example = "Mon")
            @RequestParam(required = false) String name,
            Pageable pageable
    ) {
        return getCachedPage(
                ProductQueryIndexKey.byQuery("BY_NAME", name, null, pageable),
                () -> productService.findByName(name, pageable).map(productMapper::toResponseDto)
        );
    }

    @GetMapping("/n-plus-one")
    @Operation(summary = "Demonstrate N+1 query behavior")
    @ApiResponse(responseCode = "200", description = "Products fetched successfully")
    public Page<ProductResponseDto> demoNplusOne(
            @Parameter(description = "Optional product name filter", example = "Mon")
            @RequestParam(required = false) String name,
            Pageable pageable
    ) {
        return getCachedPage(
                ProductQueryIndexKey.byQuery("N_PLUS_ONE", name, null, pageable),
                () -> productService.demoNplusOne(name, pageable).map(productMapper::toResponseDto)
        );
    }

    @GetMapping("/optimized")
    @Operation(summary = "Get products using optimized query strategy")
    @ApiResponse(responseCode = "200", description = "Products fetched successfully")
    public Page<ProductResponseDto> findWithEntityGraph(
            @Parameter(description = "Optional product name filter", example = "Mon")
            @RequestParam(required = false) String name,
            Pageable pageable
    ) {
        return getCachedPage(
                ProductQueryIndexKey.byQuery("OPTIMIZED", name, null, pageable),
                () -> productService.findByNameWithEntityGraph(name, pageable).map(productMapper::toResponseDto)
        );
    }

    @GetMapping("/search/jpql")
    @Operation(summary = "Search products by name and category using JPQL")
    @ApiResponse(responseCode = "200", description = "Products fetched successfully")
    public Page<ProductResponseDto> findByNameAndCategoryWithJpql(
            @Parameter(description = "Optional product name filter", example = "Mon")
            @RequestParam(required = false) String name,
            @Parameter(description = "Optional category name filter", example = "Displays")
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
    @Operation(summary = "Search products by name and category using native SQL")
    @ApiResponse(responseCode = "200", description = "Products fetched successfully")
    public Page<ProductResponseDto> findByNameAndCategoryWithNativeQuery(
            @Parameter(description = "Optional product name filter", example = "Mon")
            @RequestParam(required = false) String name,
            @Parameter(description = "Optional category name filter", example = "Displays")
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
    @Operation(summary = "Create product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ProductResponseDto create(@Valid @RequestBody ProductRequestDto request) {
        return productMapper.toResponseDto(productService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ProductResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto request
    ) {
        return productMapper.toResponseDto(
                productService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
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
