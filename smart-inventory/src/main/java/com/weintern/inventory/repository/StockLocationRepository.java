package com.weintern.inventory.repository;

import com.weintern.inventory.domain.StockLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockLocationRepository extends JpaRepository<StockLocation, Long> {
    List<StockLocation> findAllByOrderByNameAsc();
}
