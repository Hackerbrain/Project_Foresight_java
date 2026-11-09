package com.weintern.inventory.repository;

import com.weintern.inventory.domain.MovementType;
import com.weintern.inventory.domain.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    /** allTypes = true ignores the type filter (a dummy type must still be supplied). */
    @Query("select m from StockMovement m where (:allTypes = true or m.type = :type) "
            + "and (:q = '' or lower(m.product.name) like lower(concat('%', :q, '%')) "
            + "or lower(m.product.sku) like lower(concat('%', :q, '%')))")
    Page<StockMovement> search(@Param("q") String q, @Param("allTypes") boolean allTypes,
                               @Param("type") MovementType type, Pageable pageable);

    List<StockMovement> findTop10ByOrderByCreatedAtDescIdDesc();

    List<StockMovement> findTop20ByProductIdOrderByCreatedAtDescIdDesc(Long productId);
}
