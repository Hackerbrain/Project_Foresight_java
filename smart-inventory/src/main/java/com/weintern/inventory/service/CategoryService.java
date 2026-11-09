package com.weintern.inventory.service;

import com.weintern.inventory.domain.Category;
import com.weintern.inventory.exception.BusinessException;
import com.weintern.inventory.exception.ResourceNotFoundException;
import com.weintern.inventory.repository.CategoryRepository;
import com.weintern.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Category> listAll() { return categoryRepository.findAllByOrderByNameAsc(); }

    @Transactional
    public Category create(String name, String description) {
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty()) throw new BusinessException("Enter a category name.");
        if (clean.length() > 80) throw new BusinessException("Category name can be at most 80 characters.");
        if (categoryRepository.existsByNameIgnoreCase(clean)) {
            throw new BusinessException("A category named \"" + clean + "\" already exists.");
        }
        Category c = new Category();
        c.setName(clean);
        c.setDescription(description == null || description.isBlank() ? null : description.trim());
        return categoryRepository.save(c);
    }

    @Transactional
    public void delete(Long id) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (productRepository.existsByCategoryId(id)) {
            throw new BusinessException("\"" + c.getName() + "\" still has products. Move them to another category first.");
        }
        categoryRepository.delete(c);
    }
}
