package com.hikaro.warehouse.service;

import com.hikaro.warehouse.entity.Product;
import com.hikaro.warehouse.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + id + " not found"));
    }

    public List<Product> findByName(String name) {
        if (name == null || name.isBlank()) {
            return productRepository.findAll();
        }
        return productRepository.findByNameContainingIgnoreCase(name);
    }
}
