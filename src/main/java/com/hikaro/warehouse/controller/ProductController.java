package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.ProductResponseDto;
import com.hikaro.warehouse.mapper.ProductMapper;
import com.hikaro.warehouse.service.ProductService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


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
        try {
            return productMapper.toResponseDto(productService.getById(id));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping
    public List<ProductResponseDto> findByName(@RequestParam(required = false) String name) {
        return productService.findByName(name)
                .stream()
                .map(productMapper::toResponseDto)
                .toList();
    }
}
