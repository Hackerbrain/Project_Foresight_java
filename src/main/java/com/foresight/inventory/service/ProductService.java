package com.foresight.inventory.service;

import com.foresight.inventory.dto.ProductRequest;
import com.foresight.inventory.entity.*;
import com.foresight.inventory.repository.CategoryRepository;
import com.foresight.inventory.repository.ProductRepository;
import com.foresight.inventory.repository.StockLocationRepository;
import com.foresight.inventory.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final StockLocationRepository stockLocationRepository;
    private final InventoryService inventoryService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          SupplierRepository supplierRepository,
                          StockLocationRepository stockLocationRepository,
                          InventoryService inventoryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.stockLocationRepository = stockLocationRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String search, Long categoryId, Pageable pageable) {
        return productRepository.searchProducts(search, categoryId, pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Transactional
    public Product createProduct(ProductRequest request, String performedBy) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId()).orElse(null);
        }

        StockLocation location = null;
        if (request.getLocationId() != null) {
            location = stockLocationRepository.findById(request.getLocationId()).orElse(null);
        } else {
            location = stockLocationRepository.findAll().stream().findFirst().orElse(null);
        }

        Product product = new Product(
                request.getSku(),
                request.getName(),
                request.getDescription(),
                category,
                supplier,
                location,
                request.getUnit(),
                request.getCostPrice(),
                request.getSellingPrice(),
                0, // initial stock starts at 0, then we do initial stock-in if provided
                request.getReorderLevel(),
                request.getMaxStockLevel(),
                request.getImageUrl()
        );

        Product savedProduct = productRepository.save(product);

        // If an initial stock was specified upon product creation, record traceable stock movement
        if (request.getCurrentStock() != null && request.getCurrentStock() > 0) {
            inventoryService.recordStockMovement(
                    savedProduct.getId(),
                    location != null ? location.getId() : null,
                    MovementType.STOCK_IN,
                    request.getCurrentStock(),
                    "Initial stock upon product creation",
                    "INIT-" + savedProduct.getSku(),
                    performedBy
            );
        }

        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest request, String performedBy) {
        Product product = getProductById(id);

        if (!product.getSku().equalsIgnoreCase(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId()).orElse(null);
            product.setSupplier(supplier);
        }

        if (request.getLocationId() != null) {
            StockLocation location = stockLocationRepository.findById(request.getLocationId()).orElse(null);
            product.setDefaultLocation(location);
        }

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setUnit(request.getUnit());
        product.setCostPrice(request.getCostPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setReorderLevel(request.getReorderLevel());
        product.setMaxStockLevel(request.getMaxStockLevel());
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            product.setImageUrl(request.getImageUrl());
        }

        // If quantity is explicitly updated via product edit form, record an adjustment audit movement
        if (request.getCurrentStock() != null && !request.getCurrentStock().equals(product.getCurrentStock())) {
            int delta = request.getCurrentStock() - product.getCurrentStock();
            inventoryService.recordStockMovement(
                    product.getId(),
                    product.getDefaultLocation() != null ? product.getDefaultLocation().getId() : null,
                    MovementType.ADJUSTMENT,
                    delta,
                    "Manual stock adjustment during product update",
                    "ADJ-EDIT",
                    performedBy
            );
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public int importProductsFromCsv(InputStream inputStream, String performedBy) {
        int importedCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // skip header
                    continue;
                }
                String[] tokens = line.split(",");
                if (tokens.length >= 6) {
                    String sku = tokens[0].trim();
                    String name = tokens[1].trim();
                    String categoryName = tokens[2].trim();
                    BigDecimal costPrice = new BigDecimal(tokens[3].trim());
                    BigDecimal sellingPrice = new BigDecimal(tokens[4].trim());
                    int stock = Integer.parseInt(tokens[5].trim());
                    int reorder = tokens.length > 6 ? Integer.parseInt(tokens[6].trim()) : 10;

                    Category cat = categoryRepository.findByName(categoryName)
                            .orElseGet(() -> categoryRepository.save(new Category(categoryName, categoryName.toUpperCase().replace(" ", "_"), categoryName)));

                    if (!productRepository.existsBySku(sku)) {
                        ProductRequest req = new ProductRequest();
                        req.setSku(sku);
                        req.setName(name);
                        req.setCategoryId(cat.getId());
                        req.setCostPrice(costPrice);
                        req.setSellingPrice(sellingPrice);
                        req.setCurrentStock(stock);
                        req.setReorderLevel(reorder);
                        req.setMaxStockLevel(Math.max(100, stock * 2));
                        createProduct(req, performedBy);
                        importedCount++;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }
        return importedCount;
    }
}
