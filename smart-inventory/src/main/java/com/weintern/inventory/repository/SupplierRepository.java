package com.weintern.inventory.repository;

import com.weintern.inventory.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("select s from Supplier s where :q = '' or lower(s.name) like lower(concat('%', :q, '%')) "
            + "or lower(s.contactPerson) like lower(concat('%', :q, '%'))")
    Page<Supplier> search(@Param("q") String q, Pageable pageable);

    List<Supplier> findByActiveTrueOrderByNameAsc();
}
