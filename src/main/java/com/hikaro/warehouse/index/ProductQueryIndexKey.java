package com.hikaro.warehouse.index;

import java.util.Objects;
import org.springframework.data.domain.Pageable;

public final class ProductQueryIndexKey {

    private final String queryType;
    private final Long productId;
    private final String name;
    private final String categoryName;
    private final int pageNumber;
    private final int pageSize;
    private final String sort;

    private ProductQueryIndexKey(
            String queryType,
            Long productId,
            String name,
            String categoryName,
            int pageNumber,
            int pageSize,
            String sort
    ) {
        this.queryType = queryType;
        this.productId = productId;
        this.name = name;
        this.categoryName = categoryName;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    public static ProductQueryIndexKey byId(Long productId) {
        return new ProductQueryIndexKey("BY_ID", productId, null, null, -1, -1, "UNSORTED");
    }

    public static ProductQueryIndexKey byQuery(
            String queryType,
            String name,
            String categoryName,
            Pageable pageable
    ) {
        return new ProductQueryIndexKey(
                queryType,
                null,
                name,
                categoryName,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().toString()
        );
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ProductQueryIndexKey other)) {
            return false;
        }
        return pageNumber == other.pageNumber
                && pageSize == other.pageSize
                && Objects.equals(queryType, other.queryType)
                && Objects.equals(productId, other.productId)
                && Objects.equals(name, other.name)
                && Objects.equals(categoryName, other.categoryName)
                && Objects.equals(sort, other.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryType, productId, name, categoryName, pageNumber, pageSize, sort);
    }
}
