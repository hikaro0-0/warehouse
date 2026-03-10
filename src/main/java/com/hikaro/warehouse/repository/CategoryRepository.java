package com.hikaro.warehouse.repository;

import com.hikaro.warehouse.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
