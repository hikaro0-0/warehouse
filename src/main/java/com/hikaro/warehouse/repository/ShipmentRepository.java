package com.hikaro.warehouse.repository;

import com.hikaro.warehouse.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}
