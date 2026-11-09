package com.foresight.inventory.service;

import com.foresight.inventory.dto.ChartDataDto;
import com.foresight.inventory.dto.DashboardStatsDto;
import com.foresight.inventory.dto.ForesightRecommendationDto;
import com.foresight.inventory.entity.*;
import com.foresight.inventory.repository.ProductRepository;
import com.foresight.inventory.repository.PurchaseOrderRepository;
import com.foresight.inventory.repository.StockMovementRepository;
import com.foresight.inventory.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ForesightAnalyticsService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;

    public ForesightAnalyticsService(ProductRepository productRepository,
                                     StockMovementRepository stockMovementRepository,
                                     PurchaseOrderRepository purchaseOrderRepository,
                                     SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        List<Product> products = productRepository.findByActiveTrue();
        long totalProducts = products.size();
        long lowStockCount = products.stream().filter(Product::isLowStock).count();
        long totalSuppliers = supplierRepository.count();

        long activeOrders = purchaseOrderRepository.countByStatus(OrderStatus.DRAFT) +
                purchaseOrderRepository.countByStatus(OrderStatus.ORDERED) +
                purchaseOrderRepository.countByStatus(OrderStatus.PARTIALLY_RECEIVED);

        BigDecimal totalValuation = products.stream()
                .map(p -> p.getCostPrice().multiply(BigDecimal.valueOf(p.getCurrentStock() != null ? p.getCurrentStock() : 0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalUnits = products.stream()
                .mapToLong(p -> p.getCurrentStock() != null ? p.getCurrentStock() : 0)
                .sum();

        return new DashboardStatsDto(totalProducts, lowStockCount, totalSuppliers, activeOrders, totalValuation, totalUnits);
    }

    @Transactional(readOnly = true)
    public ChartDataDto getChartData(String timeframe) {
        String tf = (timeframe == null || timeframe.isBlank()) ? "weekly" : timeframe.toLowerCase();
        ChartDataDto dto = new ChartDataDto(tf);

        LocalDateTime now = LocalDateTime.now();
        List<StockMovement> movements;

        if ("daily".equals(tf)) {
            // Group by 4-hour slots of current day
            movements = stockMovementRepository.findMovementsSince(now.minusHours(24));
            String[] slots = {"00:00", "04:00", "08:00", "12:00", "16:00", "20:00"};
            for (String slot : slots) {
                dto.getLabels().add(slot);
                dto.getStockIn().add(0);
                dto.getStockOut().add(0);
                dto.getNetMovement().add(0);
            }
            for (StockMovement m : movements) {
                int hour = m.getTimestamp().getHour();
                int idx = Math.min(hour / 4, slots.length - 1);
                int qty = Math.abs(m.getQuantityChange());
                if (m.getType() == MovementType.STOCK_IN || m.getType() == MovementType.PO_RECEIPT || (m.getType() == MovementType.ADJUSTMENT && m.getQuantityChange() > 0)) {
                    dto.getStockIn().set(idx, dto.getStockIn().get(idx) + qty);
                } else if (m.getType() == MovementType.STOCK_OUT || (m.getType() == MovementType.ADJUSTMENT && m.getQuantityChange() < 0)) {
                    dto.getStockOut().set(idx, dto.getStockOut().get(idx) + qty);
                }
            }
        } else if ("monthly".equals(tf)) {
            // Group by 4 weeks of the month
            movements = stockMovementRepository.findMovementsSince(now.minusDays(28));
            for (int i = 4; i >= 1; i--) {
                dto.getLabels().add("Week " + (5 - i));
                dto.getStockIn().add(0);
                dto.getStockOut().add(0);
                dto.getNetMovement().add(0);
            }
            for (StockMovement m : movements) {
                long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(m.getTimestamp(), now);
                int weekIdx = 3 - (int) Math.min(3, Math.max(0, daysAgo / 7));
                int qty = Math.abs(m.getQuantityChange());
                if (m.getType() == MovementType.STOCK_IN || m.getType() == MovementType.PO_RECEIPT || (m.getType() == MovementType.ADJUSTMENT && m.getQuantityChange() > 0)) {
                    dto.getStockIn().set(weekIdx, dto.getStockIn().get(weekIdx) + qty);
                } else if (m.getType() == MovementType.STOCK_OUT || (m.getType() == MovementType.ADJUSTMENT && m.getQuantityChange() < 0)) {
                    dto.getStockOut().set(weekIdx, dto.getStockOut().get(weekIdx) + qty);
                }
            }
        } else if ("yearly".equals(tf)) {
            // Group by 12 months
            movements = stockMovementRepository.findMovementsSince(now.minusMonths(12));
            DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM");
            for (int i = 11; i >= 0; i--) {
                LocalDate d = now.minusMonths(i).toLocalDate();
                dto.getLabels().add(d.format(monthFmt));
                dto.getStockIn().add(0);
                dto.getStockOut().add(0);
                dto.getNetMovement().add(0);
            }
            for (StockMovement m : movements) {
                long monthsAgo = java.time.temporal.ChronoUnit.MONTHS.between(m.getTimestamp().toLocalDate().withDayOfMonth(1), now.toLocalDate().withDayOfMonth(1));
                if (monthsAgo >= 0 && monthsAgo < 12) {
                    int monthIdx = 11 - (int) monthsAgo;
                    int qty = Math.abs(m.getQuantityChange());
                    if (m.getType() == MovementType.STOCK_IN || m.getType() == MovementType.PO_RECEIPT || (m.getType() == MovementType.ADJUSTMENT && m.getQuantityChange() > 0)) {
                        dto.getStockIn().set(monthIdx, dto.getStockIn().get(monthIdx) + qty);
                    } else if (m.getType() == MovementType.STOCK_OUT || (m.getType() == MovementType.ADJUSTMENT && m.getQuantityChange() < 0)) {
                        dto.getStockOut().set(monthIdx, dto.getStockOut().get(monthIdx) + qty);
                    }
                }
            }
        } else {
            // Default "weekly": last 7 days
            movements = stockMovementRepository.findMovementsSince(now.minusDays(6).withHour(0).withMinute(0));
            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE dd");
            for (int i = 6; i >= 0; i--) {
                dto.getLabels().add(now.minusDays(i).format(dayFmt));
                dto.getStockIn().add(0);
                dto.getStockOut().add(0);
                dto.getNetMovement().add(0);
            }
            for (StockMovement m : movements) {
                long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(m.getTimestamp().toLocalDate(), now.toLocalDate());
                if (daysAgo >= 0 && daysAgo < 7) {
                    int dayIdx = 6 - (int) daysAgo;
                    int qty = Math.abs(m.getQuantityChange());
                    if (m.getType() == MovementType.STOCK_IN || m.getType() == MovementType.PO_RECEIPT || (m.getType() == MovementType.ADJUSTMENT && m.getQuantityChange() > 0)) {
                        dto.getStockIn().set(dayIdx, dto.getStockIn().get(dayIdx) + qty);
                    } else if (m.getType() == MovementType.STOCK_OUT || (m.getType() == MovementType.ADJUSTMENT && m.getQuantityChange() < 0)) {
                        dto.getStockOut().set(dayIdx, dto.getStockOut().get(dayIdx) + qty);
                    }
                }
            }
        }

        // Calculate net movement for each slot
        for (int i = 0; i < dto.getLabels().size(); i++) {
            dto.getNetMovement().set(i, dto.getStockIn().get(i) - dto.getStockOut().get(i));
        }

        // Category distribution
        List<Product> products = productRepository.findByActiveTrue();
        Map<String, Long> catShare = products.stream()
                .collect(Collectors.groupingBy(p -> p.getCategory().getName(), Collectors.counting()));
        dto.setCategoryShare(catShare);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<ForesightRecommendationDto> getForesightRecommendations() {
        List<Product> products = productRepository.findByActiveTrue();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<ForesightRecommendationDto> recommendations = new ArrayList<>();

        for (Product product : products) {
            List<StockMovement> movements = stockMovementRepository.findProductMovementsSince(product.getId(), thirtyDaysAgo);

            // Compute total outflow (sales, consumption) in past 30 days
            int totalOutflow = movements.stream()
                    .filter(m -> m.getType() == MovementType.STOCK_OUT || (m.getType() == MovementType.ADJUSTMENT && m.getQuantityChange() < 0))
                    .mapToInt(m -> Math.abs(m.getQuantityChange()))
                    .sum();

            // Calculate daily velocity (burn rate)
            double avgDailyDemand = totalOutflow > 0 ? (double) totalOutflow / 30.0 : 0.5; // fallback baseline 0.5 units/day

            int currentStock = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
            int reorderLevel = product.getReorderLevel() != null ? product.getReorderLevel() : 10;
            int maxStock = product.getMaxStockLevel() != null ? product.getMaxStockLevel() : Math.max(100, reorderLevel * 4);

            double daysOfRunway = avgDailyDemand > 0 ? (currentStock / avgDailyDemand) : 999.0;

            String riskLevel;
            String message;
            String primaryAction;
            int recommendedOrder;

            int leadTime = (product.getSupplier() != null) ? product.getSupplier().getLeadTimeDays() : 7;
            // Safety stock formula: (LeadTime * AvgDailyDemand)
            int safetyStock = (int) Math.ceil(leadTime * avgDailyDemand);

            if (currentStock <= 0) {
                riskLevel = "CRITICAL";
                recommendedOrder = Math.max(20, maxStock);
                message = "Stockout detected! Zero units on hand. Immediate emergency replenishment required.";
                primaryAction = "Create Emergency PO";
            } else if (currentStock <= reorderLevel) {
                riskLevel = "HIGH";
                recommendedOrder = Math.max(10, (maxStock - currentStock + safetyStock));
                message = String.format("Stock (%d) is below reorder threshold (%d). At burn rate of %.1f units/day, stock will deplete in %.1f days (Lead time: %d days).",
                        currentStock, reorderLevel, avgDailyDemand, daysOfRunway, leadTime);
                primaryAction = "Reorder Now";
            } else if (daysOfRunway <= leadTime + 3) {
                riskLevel = "MODERATE";
                recommendedOrder = Math.max(10, (maxStock - currentStock));
                message = String.format("Stock runway (%.1f days) is approaching supplier lead time (%d days). Plan replenishment soon.",
                        daysOfRunway, leadTime);
                primaryAction = "Schedule Reorder";
            } else if (currentStock > maxStock * 1.5) {
                riskLevel = "OVERSTOCKED";
                recommendedOrder = 0;
                message = String.format("Current inventory (%d units) exceeds max threshold (%d units). Holding excess working capital.",
                        currentStock, maxStock);
                primaryAction = "Halt Purchasing";
            } else {
                riskLevel = "HEALTHY";
                recommendedOrder = 0;
                message = String.format("Healthy stock level with ~%.0f days of supply remaining at normal demand rate.", daysOfRunway);
                primaryAction = "Maintain Monitoring";
            }

            ForesightRecommendationDto rec = new ForesightRecommendationDto();
            rec.setProductId(product.getId());
            rec.setSku(product.getSku());
            rec.setProductName(product.getName());
            rec.setCategoryName(product.getCategory().getName());
            rec.setCurrentStock(currentStock);
            rec.setReorderLevel(reorderLevel);
            rec.setAvgDailyDemand(Math.round(avgDailyDemand * 100.0) / 100.0);
            rec.setDaysOfRunway(Math.round(daysOfRunway * 10.0) / 10.0);
            rec.setRiskLevel(riskLevel);
            rec.setRecommendedOrderQuantity(recommendedOrder);
            rec.setSuggestionMessage(message);
            rec.setPrimaryAction(primaryAction);
            rec.setSupplierName(product.getSupplier() != null ? product.getSupplier().getName() : "Unassigned");
            rec.setSupplierLeadTimeDays(leadTime);
            rec.setEstimatedReorderCost(product.getCostPrice().multiply(BigDecimal.valueOf(recommendedOrder)));

            recommendations.add(rec);
        }

        // Sort by risk priority: CRITICAL first, then HIGH, then MODERATE, OVERSTOCKED, HEALTHY
        recommendations.sort(Comparator.comparingInt(r -> {
            switch (r.getRiskLevel()) {
                case "CRITICAL": return 1;
                case "HIGH": return 2;
                case "MODERATE": return 3;
                case "OVERSTOCKED": return 4;
                default: return 5;
            }
        }));

        return recommendations;
    }
}
