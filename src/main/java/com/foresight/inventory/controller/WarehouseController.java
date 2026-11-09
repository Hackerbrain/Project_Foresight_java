package com.foresight.inventory.controller;

import com.foresight.inventory.entity.StockLocation;
import com.foresight.inventory.service.WarehouseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public ResponseEntity<List<StockLocation>> getWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllLocations());
    }

    @PostMapping
    public ResponseEntity<StockLocation> createWarehouse(@RequestBody StockLocation location) {
        return ResponseEntity.ok(warehouseService.saveLocation(location));
    }
}
