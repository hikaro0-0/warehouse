package com.hikaro.warehouse.repository;

import com.hikaro.warehouse.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = {"warehouse", "supplier", "categories"})
    @Query("select p from Product p")
    List<Product> findAllWithDetails();

    @EntityGraph(attributePaths = {"warehouse", "supplier", "categories"})
    @Query("""
            select p
            from Product p
            where lower(p.name) like lower(concat('%', :name, '%'))
            """)
    List<Product> findAllWithDetailsByName(@Param("name") String name);
}
