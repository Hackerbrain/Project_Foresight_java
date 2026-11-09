package com.weintern.inventory.service;

import com.weintern.inventory.domain.Product;
import com.weintern.inventory.dto.ProductForm;
import com.weintern.inventory.exception.BusinessException;
import com.weintern.inventory.exception.ResourceNotFoundException;
import com.weintern.inventory.repository.CategoryRepository;
import com.weintern.inventory.repository.ProductRepository;
import com.weintern.inventory.repository.StockLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    public static final int PAGE_SIZE = 10;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockLocationRepository locationRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public Page<Product> search(String q, long categoryId, boolean lowOnly, int page) {
        return productRepository.search(q == null ? "" : q.trim(), categoryId, lowOnly,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("name").ascending()));
    }

    @Transactional(readOnly = true)
    public Product get(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional(readOnly = true)
    public List<Product> listActive() { return productRepository.findByActiveTrueOrderByNameAsc(); }

    @Transactional
    public Product create(ProductForm form) {
        String sku = form.getSku().trim().toUpperCase();
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new BusinessException("SKU " + sku + " is already used by another product.");
        }
        Product product = new Product();
        copy(form, product);
        product.setSku(sku);
        product.setQuantityOnHand(0);
        product = productRepository.save(product);

        int opening = form.getOpeningStock() == null ? 0 : form.getOpeningStock();
        if (opening > 0) {
            inventoryService.stockIn(product.getId(), opening, "Opening stock", null);
        }
        return product;
    }

    @Transactional
    public Product update(Long id, ProductForm form) {
        Product product = get(id);
        String sku = form.getSku().trim().toUpperCase();
        productRepository.findBySkuIgnoreCase(sku).ifPresent(other -> {
            if (!other.getId().equals(id)) throw new BusinessException("SKU " + sku + " is already used by another product.");
        });
        copy(form, product);
        product.setSku(sku);
        return product;
    }

    /** Products with history are archived, never deleted, so the ledger stays intact. */
    @Transactional
    public void archive(Long id) {
        get(id).setActive(false);
    }

    public ProductForm toForm(Product p) {
        ProductForm f = new ProductForm();
        f.setSku(p.getSku());
        f.setName(p.getName());
        f.setDescription(p.getDescription());
        f.setCategoryId(p.getCategory().getId());
        f.setUnit(p.getUnit());
        f.setCostPrice(p.getCostPrice());
        f.setSellingPrice(p.getSellingPrice());
        f.setReorderLevel(p.getReorderLevel());
        f.setLocationId(p.getLocation() == null ? null : p.getLocation().getId());
        return f;
    }

    private void copy(ProductForm f, Product p) {
        p.setName(f.getName().trim());
        p.setDescription(f.getDescription() == null || f.getDescription().isBlank() ? null : f.getDescription().trim());
        p.setCategory(categoryRepository.findById(f.getCategoryId())
                .orElseThrow(() -> new BusinessException("The selected category does not exist.")));
        p.setUnit(f.getUnit().trim());
        p.setCostPrice(f.getCostPrice());
        p.setSellingPrice(f.getSellingPrice());
        p.setReorderLevel(f.getReorderLevel());
        p.setLocation(f.getLocationId() == null ? null : locationRepository.findById(f.getLocationId())
                .orElseThrow(() -> new BusinessException("The selected location does not exist.")));
    }
}
