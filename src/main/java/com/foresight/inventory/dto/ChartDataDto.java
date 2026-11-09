package com.foresight.inventory.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartDataDto {

    private String timeframe; // "daily", "weekly", "monthly", "yearly"
    private List<String> labels = new ArrayList<>();
    private List<Integer> stockIn = new ArrayList<>();
    private List<Integer> stockOut = new ArrayList<>();
    private List<Integer> netMovement = new ArrayList<>();
    private Map<String, Long> categoryShare = new HashMap<>();

    public ChartDataDto() {}

    public ChartDataDto(String timeframe) {
        this.timeframe = timeframe;
    }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public List<Integer> getStockIn() { return stockIn; }
    public void setStockIn(List<Integer> stockIn) { this.stockIn = stockIn; }

    public List<Integer> getStockOut() { return stockOut; }
    public void setStockOut(List<Integer> stockOut) { this.stockOut = stockOut; }

    public List<Integer> getNetMovement() { return netMovement; }
    public void setNetMovement(List<Integer> netMovement) { this.netMovement = netMovement; }

    public Map<String, Long> getCategoryShare() { return categoryShare; }
    public void setCategoryShare(Map<String, Long> categoryShare) { this.categoryShare = categoryShare; }
}
