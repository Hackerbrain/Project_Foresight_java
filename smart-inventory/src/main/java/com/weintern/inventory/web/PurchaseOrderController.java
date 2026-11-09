package com.weintern.inventory.web;

import com.weintern.inventory.domain.PoStatus;
import com.weintern.inventory.domain.Product;
import com.weintern.inventory.domain.PurchaseOrder;
import com.weintern.inventory.dto.PurchaseOrderForm;
import com.weintern.inventory.dto.PurchaseOrderItemForm;
import com.weintern.inventory.exception.BusinessException;
import com.weintern.inventory.service.ProductService;
import com.weintern.inventory.service.PurchaseOrderService;
import com.weintern.inventory.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private static final String QTY_PREFIX = "qty_";

    private final PurchaseOrderService orderService;
    private final SupplierService supplierService;
    private final ProductService productService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String status,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        PoStatus filter = parseStatus(status);
        model.addAttribute("orders", orderService.list(filter, page));
        model.addAttribute("statuses", PoStatus.values());
        model.addAttribute("status", filter == null ? "" : filter.name());
        model.addAttribute("pageUrl", PageLinks.of("/purchase-orders", "status", filter == null ? "" : filter.name()));
        return "purchase-orders/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new PurchaseOrderForm());
        model.addAttribute("suppliers", supplierService.listActive());
        return "purchase-orders/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") PurchaseOrderForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                PurchaseOrder po = orderService.createDraft(form);
                redirect.addFlashAttribute("success", po.getOrderNumber() + " created as a draft. Add items, then place the order.");
                return "redirect:/purchase-orders/" + po.getId();
            } catch (BusinessException ex) {
                model.addAttribute("error", ex.getMessage());
            }
        }
        model.addAttribute("suppliers", supplierService.listActive());
        return "purchase-orders/new";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        PurchaseOrder po = orderService.get(id);
        model.addAttribute("po", po);
        if (po.getStatus() == PoStatus.DRAFT) {
            model.addAttribute("itemForm", new PurchaseOrderItemForm());
            // Offer the supplier's own products first; fall back to the full catalogue if none are linked.
            List<Product> candidates = po.getSupplier().getProducts().isEmpty()
                    ? productService.listActive()
                    : po.getSupplier().getProducts().stream().filter(Product::isActive)
                        .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList();
            model.addAttribute("candidates", candidates);
        }
        return "purchase-orders/detail";
    }

    @PostMapping("/{id}/items")
    public String addItem(@PathVariable Long id, @Valid @ModelAttribute("itemForm") PurchaseOrderItemForm form,
                          BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
        } else {
            act(redirect, () -> orderService.addItem(id, form), "Item added.");
        }
        return "redirect:/purchase-orders/" + id;
    }

    @PostMapping("/{id}/items/{itemId}/remove")
    public String removeItem(@PathVariable Long id, @PathVariable Long itemId, RedirectAttributes redirect) {
        act(redirect, () -> orderService.removeItem(id, itemId), "Item removed.");
        return "redirect:/purchase-orders/" + id;
    }

    @PostMapping("/{id}/place")
    public String place(@PathVariable Long id, RedirectAttributes redirect) {
        act(redirect, () -> orderService.place(id), "Order placed with the supplier.");
        return "redirect:/purchase-orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirect) {
        act(redirect, () -> orderService.cancel(id), "Order cancelled.");
        return "redirect:/purchase-orders/" + id;
    }

    @PostMapping("/{id}/receive")
    public String receive(@PathVariable Long id, @RequestParam Map<String, String> params, RedirectAttributes redirect) {
        try {
            Map<Long, Integer> quantities = new HashMap<>();
            for (Map.Entry<String, String> e : params.entrySet()) {
                if (!e.getKey().startsWith(QTY_PREFIX) || e.getValue() == null || e.getValue().isBlank()) continue;
                quantities.put(Long.valueOf(e.getKey().substring(QTY_PREFIX.length())), Integer.valueOf(e.getValue().trim()));
            }
            PurchaseOrder po = orderService.receive(id, quantities);
            redirect.addFlashAttribute("success", po.getStatus() == PoStatus.RECEIVED
                    ? "Order fully received. Stock has been updated."
                    : "Partial receipt recorded. " + po.getTotalOutstanding() + " units are still outstanding.");
        } catch (NumberFormatException ex) {
            redirect.addFlashAttribute("error", "Received quantities must be whole numbers.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/purchase-orders/" + id;
    }

    private void act(RedirectAttributes redirect, Runnable action, String successMessage) {
        try {
            action.run();
            redirect.addFlashAttribute("success", successMessage);
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
    }

    private static PoStatus parseStatus(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return PoStatus.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
