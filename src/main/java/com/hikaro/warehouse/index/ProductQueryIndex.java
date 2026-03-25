package com.hikaro.warehouse.index;

import com.hikaro.warehouse.dto.ProductResponseDto;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProductQueryIndex {

    private final Map<ProductQueryIndexKey, Object> index = new HashMap<>();

    public synchronized Optional<ProductResponseDto> getProduct(ProductQueryIndexKey key) {
        Object value = index.get(key);
        if (value instanceof ProductResponseDto product) {
            return Optional.of(product);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public synchronized Optional<Page<ProductResponseDto>> getProductPage(ProductQueryIndexKey key) {
        Object value = index.get(key);
        if (value instanceof Page<?>) {
            return Optional.of((Page<ProductResponseDto>) value);
        }
        return Optional.empty();
    }

    public synchronized void put(ProductQueryIndexKey key, ProductResponseDto product) {
        index.put(key, product);
    }

    public synchronized void put(ProductQueryIndexKey key, Page<ProductResponseDto> page) {
        index.put(key, page);
    }

    public synchronized void invalidate() {
        index.clear();
    }
}
