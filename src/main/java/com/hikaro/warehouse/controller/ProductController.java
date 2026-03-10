package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.ProductRequestDto;
import com.hikaro.warehouse.dto.ProductResponseDto;
import com.hikaro.warehouse.mapper.ProductMapper;
import com.hikaro.warehouse.service.ProductService;
import java.util.List;
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

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping("/{id}")
    public ProductResponseDto getById(@PathVariable Long id) {
        return productMapper.toResponseDto(productService.getById(id));
    }

    @GetMapping
    public List<ProductResponseDto> findByName(@RequestParam(required = false) String name) {
        return productService.findByName(name)
                .stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/n-plus-one")
    public List<ProductResponseDto> demoNplusOne(@RequestParam(required = false) String name) {
        return productService.demoNplusOne(name)
                .stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/optimized")
    public List<ProductResponseDto> findWithEntityGraph(
            @RequestParam(required = false) String name
    ) {
        return productService.findByNameWithEntityGraph(name)
                .stream()
                .map(productMapper::toResponseDto)
                .toList();
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
}
