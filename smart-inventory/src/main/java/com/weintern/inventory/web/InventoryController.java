package com.weintern.inventory.web;

import com.weintern.inventory.domain.MovementType;
import com.weintern.inventory.dto.TransactionForm;
import com.weintern.inventory.exception.BusinessException;
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
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    @GetMapping
    public String ledger(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "") String type,
                         @RequestParam(defaultValue = "0") int page, Model model) {
        MovementType filter = parseType(type);
        model.addAttribute("movements", inventoryService.searchLedger(q, filter, page));
        model.addAttribute("types", MovementType.values());
        model.addAttribute("q", q);
        model.addAttribute("type", filter == null ? "" : filter.name());
        model.addAttribute("pageUrl", PageLinks.of("/inventory", "q", q, "type", filter == null ? "" : filter.name()));
        return "inventory/ledger";
    }

    @GetMapping("/transaction")
    public String transactionForm(@RequestParam(required = false) Long productId,
                                  @RequestParam(required = false) String type, Model model) {
        TransactionForm form = new TransactionForm();
        form.setProductId(productId);
        MovementType t = parseType(type);
        if (t != null && t != MovementType.PO_RECEIPT) form.setType(t);
        model.addAttribute("form", form);
        return formView(model);
    }

    @PostMapping("/transaction")
    public String submit(@Valid @ModelAttribute("form") TransactionForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return formView(model);
        try {
            var movement = inventoryService.record(form.getType(), form.getProductId(), form.getQuantity(),
                    form.getReason(), form.getReference());
            redirect.addFlashAttribute("success", movement.getType().getLabel() + " recorded for "
                    + movement.getProduct().getSku() + ". New balance: " + movement.getBalanceAfter() + ".");
            return "redirect:/products/" + movement.getProduct().getId();
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            return formView(model);
        }
    }

    private String formView(Model model) {
        model.addAttribute("products", productService.listActive());
        return "inventory/transaction";
    }

    private static MovementType parseType(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return MovementType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
