package com.weintern.inventory.repository;

import com.weintern.inventory.domain.PoStatus;
import com.weintern.inventory.domain.PurchaseOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select po from PurchaseOrder po where po.id = :id")
    Optional<PurchaseOrder> findByIdForUpdate(@Param("id") Long id);

    Page<PurchaseOrder> findByStatus(PoStatus status, Pageable pageable);

    long countByStatus(PoStatus status);

    long countByStatusIn(Collection<PoStatus> statuses);
}
