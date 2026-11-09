package com.weintern.inventory.service;

import com.weintern.inventory.domain.PoStatus;
import com.weintern.inventory.domain.Product;
import com.weintern.inventory.domain.StockMovement;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Value
public class DashboardStats {
    long productCount;
    long unitsOnHand;
    BigDecimal stockValue;
    long lowStockCount;
    long openOrders;
    List<Product> lowStockProducts;
    List<StockMovement> recentMovements;
    Map<PoStatus, Long> ordersByStatus;
}
