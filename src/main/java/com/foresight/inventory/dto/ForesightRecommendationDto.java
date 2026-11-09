package com.foresight.inventory.dto;

import java.math.BigDecimal;

public class ForesightRecommendationDto {

    private Long productId;
    private String sku;
    private String productName;
    private String categoryName;
    private Integer currentStock;
    private Integer reorderLevel;
    private double avgDailyDemand;
    private double daysOfRunway; // Days until stockout
    private String riskLevel; // CRITICAL, HIGH, MODERATE, HEALTHY, OVERSTOCKED
    private Integer recommendedOrderQuantity;
    private String suggestionMessage;
    private String primaryAction;
    private String supplierName;
    private int supplierLeadTimeDays;
    private BigDecimal estimatedReorderCost;

    public ForesightRecommendationDto() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public double getAvgDailyDemand() { return avgDailyDemand; }
    public void setAvgDailyDemand(double avgDailyDemand) { this.avgDailyDemand = avgDailyDemand; }

    public double getDaysOfRunway() { return daysOfRunway; }
    public void setDaysOfRunway(double daysOfRunway) { this.daysOfRunway = daysOfRunway; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Integer getRecommendedOrderQuantity() { return recommendedOrderQuantity; }
    public void setRecommendedOrderQuantity(Integer recommendedOrderQuantity) { this.recommendedOrderQuantity = recommendedOrderQuantity; }

    public String getSuggestionMessage() { return suggestionMessage; }
    public void setSuggestionMessage(String suggestionMessage) { this.suggestionMessage = suggestionMessage; }

    public String getPrimaryAction() { return primaryAction; }
    public void setPrimaryAction(String primaryAction) { this.primaryAction = primaryAction; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public int getSupplierLeadTimeDays() { return supplierLeadTimeDays; }
    public void setSupplierLeadTimeDays(int supplierLeadTimeDays) { this.supplierLeadTimeDays = supplierLeadTimeDays; }

    public BigDecimal getEstimatedReorderCost() { return estimatedReorderCost; }
    public void setEstimatedReorderCost(BigDecimal estimatedReorderCost) { this.estimatedReorderCost = estimatedReorderCost; }
}
