package com.hikaro.warehouse.repository;

import com.hikaro.warehouse.entity.Dispatch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    @EntityGraph(attributePaths = {"warehouse", "recipient", "items", "items.product"})
    List<Dispatch> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"warehouse", "recipient", "items", "items.product"})
    Optional<Dispatch> findWithDetailsById(Long id);
}
