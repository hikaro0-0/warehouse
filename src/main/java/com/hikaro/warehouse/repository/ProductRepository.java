package com.hikaro.warehouse.repository;

import com.hikaro.warehouse.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"warehouse", "supplier", "categories"})
    @Query("select p from Product p")
    Page<Product> findAllWithDetails(Pageable pageable);

    @EntityGraph(attributePaths = {"warehouse", "supplier", "categories"})
    @Query("""
            select p
            from Product p
            where lower(p.name) like :namePattern
            """)
    Page<Product> findAllWithDetailsByName(
            @Param("namePattern") String namePattern,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"warehouse", "supplier", "categories"})
    @Query(
            value = """
                    select distinct p
                    from Product p
                    join p.categories c
                    where lower(p.name) like :namePattern
                      and lower(c.name) like :categoryPattern
                    """,
            countQuery = """
                    select count(distinct p)
                    from Product p
                    join p.categories c
                    where lower(p.name) like :namePattern
                      and lower(c.name) like :categoryPattern
                    """
    )
    Page<Product> findAllWithDetailsByNameAndCategoryJpql(
            @Param("namePattern") String namePattern,
            @Param("categoryPattern") String categoryPattern,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"warehouse", "supplier", "categories"})
    @Query(
            value = """
                    select p
                    from Product p
                    where (:namePattern is null or lower(p.name) like :namePattern)
                      and (
                          :categoryPattern is null
                          or exists (
                              select 1
                              from p.categories c
                              where lower(c.name) like :categoryPattern
                          )
                      )
                    """,
            countQuery = """
                    select count(p)
                    from Product p
                    where (:namePattern is null or lower(p.name) like :namePattern)
                      and (
                          :categoryPattern is null
                          or exists (
                              select 1
                              from p.categories c
                              where lower(c.name) like :categoryPattern
                          )
                      )
                    """
    )
    Page<Product> findAllWithDetailsByNameAndCategory(
            @Param("namePattern") String namePattern,
            @Param("categoryPattern") String categoryPattern,
            Pageable pageable
    );
}
