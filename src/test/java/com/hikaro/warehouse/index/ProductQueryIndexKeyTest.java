package com.hikaro.warehouse.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hikaro.warehouse.dto.ProductResponseDto;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ProductQueryIndexKeyTest {

    @Test
    void shouldUseAllQueryParametersInEqualsAndHashCode() {
        ProductQueryIndexKey first = ProductQueryIndexKey.byQuery(
                "SEARCH_JPQL",
                "monitor",
                "premium",
                PageRequest.of(0, 10)
        );
        ProductQueryIndexKey second = ProductQueryIndexKey.byQuery(
                "SEARCH_JPQL",
                "monitor",
                "premium",
                PageRequest.of(0, 10)
        );
        ProductQueryIndexKey differentPage = ProductQueryIndexKey.byQuery(
                "SEARCH_JPQL",
                "monitor",
                "premium",
                PageRequest.of(1, 10)
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, differentPage);
    }

    @Test
    void shouldBuildByIdKeysAndHandleEqualityGuards() {
        ProductQueryIndexKey first = ProductQueryIndexKey.byId(7L);
        ProductQueryIndexKey second = ProductQueryIndexKey.byId(7L);
        ProductQueryIndexKey different = ProductQueryIndexKey.byId(8L);

        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(first, null);
        assertNotEquals(first, "BY_ID");
    }

    @Test
    void shouldStoreAndInvalidateDifferentCachedEntryTypes() {
        ProductQueryIndex index = new ProductQueryIndex();
        ProductQueryIndexKey productKey = ProductQueryIndexKey.byId(1L);
        ProductQueryIndexKey pageKey = ProductQueryIndexKey.byQuery("BY_NAME", "monitor", null, PageRequest.of(0, 10));
        ProductResponseDto response = new ProductResponseDto(
                1L,
                "SKU-1",
                "Monitor",
                5,
                1L,
                "Main",
                2L,
                "ACME",
                List.of("Displays")
        );
        Page<ProductResponseDto> page = new PageImpl<>(List.of(response));

        index.put(productKey, response);
        index.put(pageKey, page);

        assertTrue(index.getProduct(productKey).isPresent());
        assertTrue(index.getProductPage(pageKey).isPresent());
        assertTrue(index.getProduct(pageKey).isEmpty());
        assertTrue(index.getProductPage(productKey).isEmpty());

        index.invalidate();

        assertFalse(index.getProduct(productKey).isPresent());
        assertFalse(index.getProductPage(pageKey).isPresent());
    }
}
