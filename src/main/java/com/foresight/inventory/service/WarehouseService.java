package com.foresight.inventory.service;

import com.foresight.inventory.entity.StockLocation;
import com.foresight.inventory.repository.StockLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WarehouseService {

    private final StockLocationRepository stockLocationRepository;

    public WarehouseService(StockLocationRepository stockLocationRepository) {
        this.stockLocationRepository = stockLocationRepository;
    }

    @Transactional(readOnly = true)
    public List<StockLocation> getAllLocations() {
        return stockLocationRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public StockLocation getLocationById(Long id) {
        return stockLocationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));
    }

    @Transactional
    public StockLocation saveLocation(StockLocation location) {
        return stockLocationRepository.save(location);
    }
}
