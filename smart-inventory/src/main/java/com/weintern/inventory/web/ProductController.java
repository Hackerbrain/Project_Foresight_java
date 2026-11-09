package com.weintern.inventory.web;

import com.weintern.inventory.dto.ProductForm;
import com.weintern.inventory.exception.BusinessException;
import com.weintern.inventory.repository.StockLocationRepository;
import com.weintern.inventory.service.CategoryService;
import com.weintern.inventory.service.InventoryService;
import com.weintern.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final InventoryService inventoryService;
    private final StockLocationRepository locationRepository;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "0") long categoryId,
                       @RequestParam(defaultValue = "false") boolean low,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("products", productService.search(q, categoryId, low, page));
        model.addAttribute("categories", categoryService.listAll());
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("low", low);
        model.addAttribute("pageUrl", PageLinks.of("/products", "q", q, "categoryId", categoryId, "low", low));
        return "products/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.get(id));
        model.addAttribute("movements", inventoryService.recentForProduct(id));
        return "products/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ProductForm());
        return formView(model, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ProductForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return formView(model, null);
        try {
            var product = productService.create(form);
            redirect.addFlashAttribute("success", "Product " + product.getSku() + " created.");
            return "redirect:/products/" + product.getId();
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            return formView(model, null);
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", productService.toForm(productService.get(id)));
        return formView(model, id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") ProductForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return formView(model, id);
        try {
            productService.update(id, form);
            redirect.addFlashAttribute("success", "Product updated.");
            return "redirect:/products/" + id;
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            return formView(model, id);
        }
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, RedirectAttributes redirect) {
        productService.archive(id);
        redirect.addFlashAttribute("success", "Product archived. Its stock history is kept.");
        return "redirect:/products";
    }

    private String formView(Model model, Long productId) {
        model.addAttribute("productId", productId);
        model.addAttribute("categories", categoryService.listAll());
        model.addAttribute("locations", locationRepository.findAllByOrderByNameAsc());
        return "products/form";
    }
}
