package com.foresight.inventory.config;

import com.foresight.inventory.entity.*;
import com.foresight.inventory.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final StockLocationRepository stockLocationRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           SupplierRepository supplierRepository,
                           StockLocationRepository stockLocationRepository,
                           ProductRepository productRepository,
                           StockMovementRepository stockMovementRepository,
                           PurchaseOrderRepository purchaseOrderRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.stockLocationRepository = stockLocationRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Already initialized
        }

        // 1. Users (Standard & Gmail Credentials)
        User admin = new User("admin@foresight.com", passwordEncoder.encode("admin123"), "System Administrator", Role.ADMIN, "LOCAL");
        User anubhab = new User("anubhab@foresight.com", passwordEncoder.encode("anubhab123"), "Anubhab Sharma", Role.ADMIN, "LOCAL");
        User anubhabGmail = new User("anubhab.foresight@gmail.com", passwordEncoder.encode("gmail123"), "Anubhab Sharma (Gmail)", Role.ADMIN, "LOCAL");
        User staffGmail = new User("staff.foresight@gmail.com", passwordEncoder.encode("gmail123"), "Inventory Specialist (Gmail)", Role.INVENTORY_STAFF, "LOCAL");
        User inventory = new User("inventory@foresight.com", passwordEncoder.encode("staff123"), "Inventory Specialist", Role.INVENTORY_STAFF, "LOCAL");
        User purchasing = new User("purchasing@foresight.com", passwordEncoder.encode("purchase123"), "Purchasing Manager", Role.PURCHASING_STAFF, "LOCAL");
        userRepository.save(admin);
        userRepository.save(anubhab);
        userRepository.save(anubhabGmail);
        userRepository.save(staffGmail);
        userRepository.save(inventory);
        userRepository.save(purchasing);

        // 2. Warehouses / Stock Locations
        StockLocation cdc = stockLocationRepository.save(new StockLocation("WH-MAIN", "Central Distribution Center", "MAIN_WAREHOUSE", "Building 4, Logistics Park North"));
        StockLocation west = stockLocationRepository.save(new StockLocation("WH-WEST", "West Coast Fulfillment Hub", "REGIONAL_HUB", "77 Industrial Parkway, Sector 9"));
        StockLocation metro = stockLocationRepository.save(new StockLocation("WH-METRO", "Metro Express Depot", "RETAIL_STORE", "Unit 12, Commerce Boulevard"));

        // 3. Categories
        Category catElec = categoryRepository.save(new Category("Electronics & Computing", "ELEC", "Microcontrollers, sensors, and semiconductors"));
        Category catRaw = categoryRepository.save(new Category("Raw Materials & Metals", "RAW", "Alloys, sheet metals, and wiring"));
        Category catInd = categoryRepository.save(new Category("Industrial Components", "IND", "Bearings, valves, and hydraulics"));
        Category catPack = categoryRepository.save(new Category("Packaging & Assembly", "PACK", "Cartons, enclosures, and protective foam"));

        // 4. Suppliers
        Supplier supApex = supplierRepository.save(new Supplier("Apex Semiconductors Ltd", "Elena Vance", "orders@apexsemi.com", "+1 555-0192", "Silicon Valley Blvd, CA", 10, 4.9));
        Supplier supTitan = supplierRepository.save(new Supplier("Titan Raw Materials Corp", "Marcus Vance", "sales@titanmetals.com", "+1 555-0847", "Industrial Way, OH", 5, 4.7));
        Supplier supMicro = supplierRepository.save(new Supplier("Global Micro-Sensors Inc", "Sarah Connor", "supply@globalsensors.com", "+1 555-0331", "Tech Park, Austin TX", 14, 4.3));
        Supplier supLogix = supplierRepository.save(new Supplier("NextGen Logistics Supplies", "David Chen", "contact@nextgenpack.com", "+1 555-0988", "Harbor Road, Seattle WA", 3, 4.8));

        // 5. Products (matching project specification scenario!)
        // Scenario from doc: Store has 25 units, reorder threshold is 10. Sales reduced inventory to 8 -> marks as low stock!
        Product p1 = new Product(
                "PRD-1001", "Industrial IoT Micro-Controller", "High performance dual-core ESP32 industrial grade unit",
                catElec, supApex, cdc, "PCS", new BigDecimal("45.00"), new BigDecimal("75.00"),
                8, 10, 100, "https://images.unsplash.com/photo-1518770660439-4636190af475?w=300&auto=format&fit=crop"
        );

        Product p2 = new Product(
                "PRD-1002", "Arm Cortex Sensor Node", "Wireless telemetry module with temperature and vibration sensing",
                catElec, supMicro, west, "PCS", new BigDecimal("32.50"), new BigDecimal("58.00"),
                4, 15, 80, "https://images.unsplash.com/photo-1555664424-778a1e5e1b48?w=300&auto=format&fit=crop"
        );

        Product p3 = new Product(
                "PRD-1003", "Copper Ribbon Cable 50m", "High-purity shielded 16-channel ribbon cable spool",
                catRaw, supTitan, cdc, "SPOOL", new BigDecimal("85.00"), new BigDecimal("130.00"),
                120, 25, 200, "https://images.unsplash.com/photo-1544724569-5f546fd6f2b5?w=300&auto=format&fit=crop"
        );

        Product p4 = new Product(
                "PRD-1004", "Titanium Alloy Rod 10mm", "High strength aerospace grade titanium rod 1 meter",
                catRaw, supTitan, cdc, "ROD", new BigDecimal("110.00"), new BigDecimal("180.00"),
                45, 12, 100, "https://images.unsplash.com/photo-1504917599217-d4dc5ebe6122?w=300&auto=format&fit=crop"
        );

        Product p5 = new Product(
                "PRD-1005", "Lithium-Ion Battery Pack 48V", "Rechargeable high density Li-Ion backup power cell",
                catElec, supApex, west, "UNIT", new BigDecimal("220.00"), new BigDecimal("350.00"),
                0, 8, 50, "https://images.unsplash.com/photo-1619642751034-765dfdf7c58e?w=300&auto=format&fit=crop"
        );

        Product p6 = new Product(
                "PRD-1006", "Precision Ball Bearings 608RS", "Ceramic hybrid low-friction high-RPM sealed bearing",
                catInd, supLogix, metro, "BOX", new BigDecimal("18.00"), new BigDecimal("34.00"),
                340, 50, 150, "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=300&auto=format&fit=crop"
        );

        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);
        productRepository.save(p4);
        productRepository.save(p5);
        productRepository.save(p6);

        // 6. Realistic Historical Stock Movements across past days, weeks, months for Graph generation
        LocalDateTime now = LocalDateTime.now();

        // Historical movements
        createMovement(p1, cdc, MovementType.STOCK_IN, 25, 0, 25, "Initial warehouse batch", "INIT-1001", now.minusMonths(2));
        createMovement(p1, cdc, MovementType.STOCK_OUT, -10, 25, 15, "Assembly line dispatch", "DISP-01", now.minusDays(18));
        createMovement(p1, cdc, MovementType.STOCK_OUT, -7, 15, 8, "Client order shipment", "ORD-990", now.minusDays(3));

        createMovement(p2, west, MovementType.STOCK_IN, 30, 0, 30, "Restock from supplier", "RESTOCK-01", now.minusMonths(3));
        createMovement(p2, west, MovementType.STOCK_OUT, -15, 30, 15, "Field installation kit", "KIT-22", now.minusDays(20));
        createMovement(p2, west, MovementType.STOCK_OUT, -11, 15, 4, "Enterprise deployment", "ENT-401", now.minusDays(4));

        createMovement(p3, cdc, MovementType.STOCK_IN, 150, 0, 150, "Bulk shipment", "BULK-01", now.minusMonths(4));
        createMovement(p3, cdc, MovementType.STOCK_OUT, -30, 150, 120, "Production line batch 3", "PRD-B3", now.minusDays(10));

        createMovement(p4, cdc, MovementType.STOCK_IN, 60, 0, 60, "Metal delivery", "MET-10", now.minusMonths(5));
        createMovement(p4, cdc, MovementType.STOCK_OUT, -15, 60, 45, "Machining order", "MCH-02", now.minusDays(14));

        createMovement(p5, west, MovementType.STOCK_IN, 20, 0, 20, "Batch arrival", "BAT-09", now.minusMonths(2));
        createMovement(p5, west, MovementType.STOCK_OUT, -20, 20, 0, "Emergency power supply retrofit", "PWR-88", now.minusDays(1));

        createMovement(p6, metro, MovementType.STOCK_IN, 400, 0, 400, "Pallet bulk delivery", "PAL-01", now.minusMonths(1));
        createMovement(p6, metro, MovementType.STOCK_OUT, -60, 400, 340, "Wholesale distribution", "WHL-14", now.minusDays(2));

        // Today's movements for daily chart
        createMovement(p3, cdc, MovementType.STOCK_IN, 10, 110, 120, "Rapid restock", "RAP-01", now.minusHours(4));
        createMovement(p1, cdc, MovementType.STOCK_OUT, -1, 9, 8, "Sample request", "SMP-05", now.minusHours(2));

        // 7. Purchase Orders matching project illustration!
        // "Illustrative example: product has 8 units, reorder level 10. Dashboard marks low stock.
        // Purchasing user creates an order for 40 units."
        PurchaseOrder po1 = new PurchaseOrder("PO-2026-001", supApex, cdc, "Replenishment for IoT Micro-Controllers below threshold", "purchasing@foresight.com");
        po1.setStatus(OrderStatus.ORDERED);
        PurchaseOrderItem item1 = new PurchaseOrderItem(po1, p1, 40, p1.getCostPrice());
        po1.addItem(item1);
        purchaseOrderRepository.save(po1);

        // PO 2: Partially received scenario (50 ordered, 25 received)
        PurchaseOrder po2 = new PurchaseOrder("PO-2026-002", supMicro, west, "Quarterly sensor node order", "purchasing@foresight.com");
        PurchaseOrderItem item2 = new PurchaseOrderItem(po2, p2, 50, p2.getCostPrice());
        item2.setQuantityReceived(25);
        po2.addItem(item2);
        po2.setStatus(OrderStatus.PARTIALLY_RECEIVED);
        purchaseOrderRepository.save(po2);

        // PO 3: Draft order
        PurchaseOrder po3 = new PurchaseOrder("PO-2026-003", supTitan, cdc, "Draft scheduled order for raw metal bars", "purchasing@foresight.com");
        PurchaseOrderItem item3 = new PurchaseOrderItem(po3, p4, 30, p4.getCostPrice());
        po3.addItem(item3);
        po3.setStatus(OrderStatus.DRAFT);
        purchaseOrderRepository.save(po3);
    }

    private void createMovement(Product product, StockLocation loc, MovementType type,
                                int qty, int prev, int next, String reason, String ref, LocalDateTime time) {
        StockMovement m = new StockMovement(product, loc, null, type, qty, prev, next, reason, ref, "System Seed");
        m.setTimestamp(time);
        stockMovementRepository.save(m);
    }
}
