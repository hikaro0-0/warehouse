package com.hikaro.warehouse.repository;

import com.hikaro.warehouse.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
}
