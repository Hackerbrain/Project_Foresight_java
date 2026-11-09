package com.weintern.inventory.service;

import com.weintern.inventory.domain.Product;
import com.weintern.inventory.domain.Supplier;
import com.weintern.inventory.dto.SupplierForm;
import com.weintern.inventory.exception.ResourceNotFoundException;
import com.weintern.inventory.repository.ProductRepository;
import com.weintern.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SupplierService {

    public static final int PAGE_SIZE = 10;

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<Supplier> search(String q, int page) {
        return supplierRepository.search(q == null ? "" : q.trim(),
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("name").ascending()));
    }

    @Transactional(readOnly = true)
    public Supplier get(Long id) {
        return supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    @Transactional(readOnly = true)
    public List<Supplier> listActive() { return supplierRepository.findByActiveTrueOrderByNameAsc(); }

    @Transactional
    public Supplier create(SupplierForm form) {
        Supplier s = new Supplier();
        copy(form, s);
        return supplierRepository.save(s);
    }

    @Transactional
    public Supplier update(Long id, SupplierForm form) {
        Supplier s = get(id);
        copy(form, s);
        return s;
    }

    public SupplierForm toForm(Supplier s) {
        SupplierForm f = new SupplierForm();
        f.setName(s.getName());
        f.setContactPerson(s.getContactPerson());
        f.setEmail(s.getEmail());
        f.setPhone(s.getPhone());
        f.setAddress(s.getAddress());
        f.setActive(s.isActive());
        f.setProductIds(s.getProducts().stream().map(Product::getId).toList());
        return f;
    }

    private void copy(SupplierForm f, Supplier s) {
        s.setName(f.getName().trim());
        s.setContactPerson(blankToNull(f.getContactPerson()));
        s.setEmail(blankToNull(f.getEmail()));
        s.setPhone(blankToNull(f.getPhone()));
        s.setAddress(blankToNull(f.getAddress()));
        s.setActive(f.isActive());
        Set<Product> products = new HashSet<>();
        if (f.getProductIds() != null && !f.getProductIds().isEmpty()) {
            products.addAll(productRepository.findAllById(f.getProductIds()));
        }
        s.getProducts().clear();
        s.getProducts().addAll(products);
    }

    private static String blankToNull(String v) { return v == null || v.isBlank() ? null : v.trim(); }
}
