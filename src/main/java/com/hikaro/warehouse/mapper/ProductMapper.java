package com.hikaro.warehouse.mapper;

import com.hikaro.warehouse.dto.ProductResponseDto;
import com.hikaro.warehouse.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponseDto toResponseDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getQuantity(),
                product.getLocation()
        );
    }
}
