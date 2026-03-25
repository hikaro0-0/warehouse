package com.hikaro.warehouse.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
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
}
