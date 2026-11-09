package com.foresight.inventory.dto;

import java.math.BigDecimal;

public class DashboardStatsDto {

    private long totalProducts;
    private long lowStockProducts;
    private long totalSuppliers;
    private long activePurchaseOrders;
    private BigDecimal totalInventoryValue;
    private long totalStockUnits;

    public DashboardStatsDto() {}

    public DashboardStatsDto(long totalProducts, long lowStockProducts, long totalSuppliers,
                             long activePurchaseOrders, BigDecimal totalInventoryValue, long totalStockUnits) {
        this.totalProducts = totalProducts;
        this.lowStockProducts = lowStockProducts;
        this.totalSuppliers = totalSuppliers;
        this.activePurchaseOrders = activePurchaseOrders;
        this.totalInventoryValue = totalInventoryValue;
        this.totalStockUnits = totalStockUnits;
    }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public long getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(long lowStockProducts) { this.lowStockProducts = lowStockProducts; }

    public long getTotalSuppliers() { return totalSuppliers; }
    public void setTotalSuppliers(long totalSuppliers) { this.totalSuppliers = totalSuppliers; }

    public long getActivePurchaseOrders() { return activePurchaseOrders; }
    public void setActivePurchaseOrders(long activePurchaseOrders) { this.activePurchaseOrders = activePurchaseOrders; }

    public BigDecimal getTotalInventoryValue() { return totalInventoryValue; }
    public void setTotalInventoryValue(BigDecimal totalInventoryValue) { this.totalInventoryValue = totalInventoryValue; }

    public long getTotalStockUnits() { return totalStockUnits; }
    public void setTotalStockUnits(long totalStockUnits) { this.totalStockUnits = totalStockUnits; }
}
