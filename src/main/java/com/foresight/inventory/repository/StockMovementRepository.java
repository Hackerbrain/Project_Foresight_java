package com.foresight.inventory.repository;

import com.foresight.inventory.entity.MovementType;
import com.foresight.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findTop50ByOrderByTimestampDesc();

    Page<StockMovement> findAllByOrderByTimestampDesc(Pageable pageable);

    List<StockMovement> findByProductIdOrderByTimestampDesc(Long productId);

    @Query("SELECT m FROM StockMovement m WHERE m.timestamp >= :startDate ORDER BY m.timestamp ASC")
    List<StockMovement> findMovementsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT m FROM StockMovement m WHERE m.product.id = :productId AND m.timestamp >= :startDate ORDER BY m.timestamp ASC")
    List<StockMovement> findProductMovementsSince(@Param("productId") Long productId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT m.type, COUNT(m), SUM(ABS(m.quantityChange)) FROM StockMovement m WHERE m.timestamp >= :startDate GROUP BY m.type")
    List<Object[]> getMovementAggregatesSince(@Param("startDate") LocalDateTime startDate);
}
