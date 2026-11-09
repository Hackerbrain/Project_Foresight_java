package com.weintern.inventory.web;

import com.weintern.inventory.dto.SupplierForm;
import com.weintern.inventory.service.ProductService;
import com.weintern.inventory.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final ProductService productService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("suppliers", supplierService.search(q, page));
        model.addAttribute("q", q);
        model.addAttribute("pageUrl", PageLinks.of("/suppliers", "q", q));
        return "suppliers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new SupplierForm());
        return formView(model, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") SupplierForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return formView(model, null);
        var supplier = supplierService.create(form);
        redirect.addFlashAttribute("success", "Supplier " + supplier.getName() + " added.");
        return "redirect:/suppliers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", supplierService.toForm(supplierService.get(id)));
        return formView(model, id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") SupplierForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return formView(model, id);
        supplierService.update(id, form);
        redirect.addFlashAttribute("success", "Supplier updated.");
        return "redirect:/suppliers";
    }

    private String formView(Model model, Long supplierId) {
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("products", productService.listActive());
        return "suppliers/form";
    }
}
