package com.weintern.inventory.repository;

import com.weintern.inventory.domain.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    Optional<Product> findBySkuIgnoreCase(String sku);

    boolean existsByCategoryId(Long categoryId);

    List<Product> findByActiveTrueOrderByNameAsc();

    /** Row-level lock so concurrent stock updates on one product are serialised. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    /** categoryId = 0 means "all categories"; lowOnly = false means "all stock levels". */
    @Query("select p from Product p where p.active = true "
            + "and (:q = '' or lower(p.name) like lower(concat('%', :q, '%')) or lower(p.sku) like lower(concat('%', :q, '%'))) "
            + "and (:categoryId = 0L or p.category.id = :categoryId) "
            + "and (:lowOnly = false or p.quantityOnHand <= p.reorderLevel)")
    Page<Product> search(@Param("q") String q, @Param("categoryId") long categoryId,
                         @Param("lowOnly") boolean lowOnly, Pageable pageable);

    @Query("select p from Product p where p.active = true and p.quantityOnHand <= p.reorderLevel "
            + "order by (p.quantityOnHand - p.reorderLevel) asc, p.name asc")
    List<Product> findLowStock();

    @Query("select count(p) from Product p where p.active = true and p.quantityOnHand <= p.reorderLevel")
    long countLowStock();

    long countByActiveTrue();

    @Query("select sum(p.quantityOnHand) from Product p where p.active = true")
    Long sumUnitsOnHand();

    @Query("select sum(p.quantityOnHand * p.costPrice) from Product p where p.active = true")
    BigDecimal sumStockValue();
}
