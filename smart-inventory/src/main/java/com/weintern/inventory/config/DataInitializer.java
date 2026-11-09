package com.weintern.inventory.config;

import com.weintern.inventory.domain.*;
import com.weintern.inventory.dto.*;
import com.weintern.inventory.repository.AppUserRepository;
import com.weintern.inventory.repository.StockLocationRepository;
import com.weintern.inventory.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Loads demo data on first start (only when the database has no users).
 * Disable with app.seed.enabled=false. The accounts below are for demonstration only.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final AppUserService userService;
    private final CategoryService categoryService;
    private final StockLocationRepository locationRepository;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final PurchaseOrderService orderService;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;
        log.info("Empty database detected - loading demo data");

        createUser("admin", "Asha Admin", "Admin@123", Role.ADMIN);
        createUser("inventory", "Ivan Inventory", "Inventory@123", Role.INVENTORY_STAFF);
        createUser("purchasing", "Priya Purchasing", "Purchasing@123", Role.PURCHASING_STAFF);

        // Run the rest as the admin so ledger entries show a real user.
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", null, AuthorityUtils.createAuthorityList("ROLE_ADMIN")));
        try {
            seedCatalogue();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void createUser(String username, String fullName, String password, Role role) {
        UserForm f = new UserForm();
        f.setUsername(username);
        f.setFullName(fullName);
        f.setPassword(password);
        f.setRole(role);
        userService.create(f);
    }

    private void seedCatalogue() {
        StockLocation main = new StockLocation();
        main.setName("Main warehouse");
        main.setAddress("Dhanbad, Jharkhand");
        main = locationRepository.save(main);

        Category electronics = categoryService.create("Electronics", "Devices and accessories");
        Category stationery = categoryService.create("Stationery", "Paper, pens and office supplies");
        Category packaging = categoryService.create("Packaging", "Boxes, tape and labels");

        Product scanner = product("EL-1001", "Wireless barcode scanner", electronics, "pcs", "1850.00", "2499.00", 10, 8, main);
        Product keyboard = product("EL-1002", "USB keyboard", electronics, "pcs", "420.00", "599.00", 15, 42, main);
        Product mouse = product("EL-1003", "Optical mouse", electronics, "pcs", "210.00", "349.00", 20, 18, main);
        Product a4 = product("ST-2001", "A4 paper ream (500 sheets)", stationery, "ream", "260.00", "330.00", 30, 120, main);
        Product pens = product("ST-2002", "Ballpoint pens (box of 50)", stationery, "box", "180.00", "260.00", 12, 9, main);
        Product tape = product("PK-3001", "Packing tape roll", packaging, "roll", "45.00", "70.00", 50, 210, main);
        Product cartons = product("PK-3002", "Corrugated carton, medium", packaging, "pcs", "28.00", "45.00", 100, 60, main);

        Supplier techSupply = supplier("TechSource Distributors", "Rohit Verma", "orders@techsource.example",
                "+91 98765 43210", "Ranchi, Jharkhand", List.of(scanner, keyboard, mouse));
        supplier("PaperWorld Traders", "Meena Iyer", "sales@paperworld.example",
                "+91 91234 56780", "Kolkata, West Bengal", List.of(a4, pens));
        supplier("PackRight Industries", "Sanjay Gupta", "contact@packright.example",
                "+91 99887 76655", "Bokaro, Jharkhand", List.of(tape, cartons));

        // A purchase order matching the handbook example: scanner at 8 units, order 40.
        PurchaseOrder po = orderService.createDraft(orderForm(techSupply.getId()));
        PurchaseOrderItemForm line = new PurchaseOrderItemForm();
        line.setProductId(scanner.getId());
        line.setQuantity(40);
        orderService.addItem(po.getId(), line);
        orderService.place(po.getId());
        log.info("Demo data ready. Sign in as admin / Admin@123");
    }

    private PurchaseOrderForm orderForm(Long supplierId) {
        PurchaseOrderForm f = new PurchaseOrderForm();
        f.setSupplierId(supplierId);
        f.setExpectedDate(LocalDate.now().plusDays(7));
        f.setNotes("Restock scanners - low stock alert");
        return f;
    }

    private Product product(String sku, String name, Category category, String unit, String cost, String price,
                            int reorder, int opening, StockLocation location) {
        ProductForm f = new ProductForm();
        f.setSku(sku);
        f.setName(name);
        f.setCategoryId(category.getId());
        f.setUnit(unit);
        f.setCostPrice(new BigDecimal(cost));
        f.setSellingPrice(new BigDecimal(price));
        f.setReorderLevel(reorder);
        f.setOpeningStock(opening);
        f.setLocationId(location.getId());
        return productService.create(f);
    }

    private Supplier supplier(String name, String contact, String email, String phone, String address, List<Product> products) {
        SupplierForm f = new SupplierForm();
        f.setName(name);
        f.setContactPerson(contact);
        f.setEmail(email);
        f.setPhone(phone);
        f.setAddress(address);
        f.setProductIds(products.stream().map(Product::getId).toList());
        return supplierService.create(f);
    }
}
