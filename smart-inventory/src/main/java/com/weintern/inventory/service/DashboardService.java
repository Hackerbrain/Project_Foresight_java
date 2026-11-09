package com.weintern.inventory.service;

import com.weintern.inventory.domain.PoStatus;
import com.weintern.inventory.repository.ProductRepository;
import com.weintern.inventory.repository.PurchaseOrderRepository;
import com.weintern.inventory.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final StockMovementRepository movementRepository;
    private final PurchaseOrderRepository orderRepository;

    @Transactional(readOnly = true)
    public DashboardStats load() {
        Long units = productRepository.sumUnitsOnHand();
        BigDecimal value = productRepository.sumStockValue();
        Map<PoStatus, Long> byStatus = new EnumMap<>(PoStatus.class);
        for (PoStatus s : PoStatus.values()) byStatus.put(s, orderRepository.countByStatus(s));

        return new DashboardStats(
                productRepository.countByActiveTrue(),
                units == null ? 0 : units,
                value == null ? BigDecimal.ZERO : value,
                productRepository.countLowStock(),
                orderRepository.countByStatusIn(List.of(PoStatus.ORDERED, PoStatus.PARTIALLY_RECEIVED)),
                productRepository.findLowStock(),
                movementRepository.findTop10ByOrderByCreatedAtDescIdDesc(),
                byStatus);
    }
}
