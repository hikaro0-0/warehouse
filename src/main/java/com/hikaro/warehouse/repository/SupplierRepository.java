package com.hikaro.warehouse.repository;

import com.hikaro.warehouse.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
