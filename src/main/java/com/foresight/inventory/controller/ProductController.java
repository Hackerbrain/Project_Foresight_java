package com.foresight.inventory.controller;

import com.foresight.inventory.dto.ProductRequest;
import com.foresight.inventory.entity.Category;
import com.foresight.inventory.entity.Product;
import com.foresight.inventory.repository.CategoryRepository;
import com.foresight.inventory.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductService productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @GetMapping("/products")
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        Page<Product> productPage = productService.searchProducts(
                search, categoryId, PageRequest.of(page, size, Sort.by(sortBy).ascending()));
        return ResponseEntity.ok(productPage.getContent());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/products/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest request, Principal principal) {
        String user = principal != null ? principal.getName() : "Anonymous";
        Product created = productService.createProduct(request, user);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @Valid @RequestBody ProductRequest request,
                                           Principal principal) {
        String user = principal != null ? principal.getName() : "Anonymous";
        Product updated = productService.updateProduct(id, request, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Product deactivated successfully");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/products/upload-csv")
    public ResponseEntity<?> uploadProductsCsv(@RequestParam("file") MultipartFile file, Principal principal) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "CSV file is empty"));
        }
        try {
            String user = principal != null ? principal.getName() : "Anonymous";
            int count = productService.importProductsFromCsv(file.getInputStream(), user);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "importedCount", count,
                    "message", "Successfully imported " + count + " products."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
