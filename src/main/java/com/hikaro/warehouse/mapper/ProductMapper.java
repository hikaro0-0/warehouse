package com.hikaro.warehouse.mapper;

import com.hikaro.warehouse.dto.ProductResponseDto;
import com.hikaro.warehouse.entity.Category;
import com.hikaro.warehouse.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponseDto toResponseDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getQuantity(),
                product.getWarehouse().getId(),
                product.getWarehouse().getName(),
                product.getSupplier().getId(),
                product.getSupplier().getName(),
                product.getCategories()
                        .stream()
                        .map(Category::getName)
                        .sorted()
                        .toList()
        );
    }
}
