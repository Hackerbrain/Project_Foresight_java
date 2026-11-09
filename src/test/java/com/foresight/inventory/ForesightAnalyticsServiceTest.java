package com.foresight.inventory;

import com.foresight.inventory.dto.ChartDataDto;
import com.foresight.inventory.dto.DashboardStatsDto;
import com.foresight.inventory.dto.ForesightRecommendationDto;
import com.foresight.inventory.entity.*;
import com.foresight.inventory.repository.ProductRepository;
import com.foresight.inventory.repository.PurchaseOrderRepository;
import com.foresight.inventory.repository.StockMovementRepository;
import com.foresight.inventory.repository.SupplierRepository;
import com.foresight.inventory.service.ForesightAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ForesightAnalyticsServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ForesightAnalyticsService analyticsService;

    private Product lowStockProduct;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        Category category = new Category("Electronics", "ELEC", "Electronics");
        supplier = new Supplier("Apex Semi", "Elena", "elena@apex.com", "555-1234", "CA", 7, 4.8);
        supplier.setId(1L);

        // Current stock = 5, Reorder level = 10 -> Low stock!
        lowStockProduct = new Product(
                "PRD-LOW", "Low Stock Sensor", "Desc",
                category, supplier, null, "PCS",
                new BigDecimal("20.00"), new BigDecimal("35.00"),
                5, 10, 50, null
        );
        lowStockProduct.setId(100L);
    }

    @Test
    void testGetDashboardStatsCalculation() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of(lowStockProduct));
        when(supplierRepository.count()).thenReturn(1L);
        when(purchaseOrderRepository.countByStatus(OrderStatus.DRAFT)).thenReturn(1L);
        when(purchaseOrderRepository.countByStatus(OrderStatus.ORDERED)).thenReturn(2L);
        when(purchaseOrderRepository.countByStatus(OrderStatus.PARTIALLY_RECEIVED)).thenReturn(0L);

        DashboardStatsDto stats = analyticsService.getDashboardStats();

        assertEquals(1, stats.getTotalProducts());
        assertEquals(1, stats.getLowStockProducts());
        assertEquals(3, stats.getActivePurchaseOrders());
        assertEquals(new BigDecimal("100.00"), stats.getTotalInventoryValue()); // 5 * 20.00
        assertEquals(5, stats.getTotalStockUnits());
    }

    @Test
    void testForesightRecommendationIdentifiesLowStockRisk() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of(lowStockProduct));
        when(stockMovementRepository.findProductMovementsSince(eq(100L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<ForesightRecommendationDto> recommendations = analyticsService.getForesightRecommendations();

        assertFalse(recommendations.isEmpty());
        ForesightRecommendationDto rec = recommendations.get(0);
        assertEquals("HIGH", rec.getRiskLevel());
        assertEquals("PRD-LOW", rec.getSku());
        assertTrue(rec.getRecommendedOrderQuantity() > 0);
        assertTrue(rec.getSuggestionMessage().contains("below reorder threshold"));
        assertEquals("Reorder Now", rec.getPrimaryAction());
    }

    @Test
    void testChartDataTimeframesReturnExpectedSeries() {
        when(stockMovementRepository.findMovementsSince(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(productRepository.findByActiveTrue()).thenReturn(List.of(lowStockProduct));

        ChartDataDto daily = analyticsService.getChartData("daily");
        assertEquals(6, daily.getLabels().size()); // 6 slots of 4 hours

        ChartDataDto weekly = analyticsService.getChartData("weekly");
        assertEquals(7, weekly.getLabels().size()); // 7 days

        ChartDataDto monthly = analyticsService.getChartData("monthly");
        assertEquals(4, monthly.getLabels().size()); // 4 weeks

        ChartDataDto yearly = analyticsService.getChartData("yearly");
        assertEquals(12, yearly.getLabels().size()); // 12 months
    }
}
