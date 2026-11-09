# Smart Inventory and Supply Chain Management System

WeIntern Major Project 01 (Team 01) - Java full stack.

A web application for a small or medium-sized business to manage products, stock levels, suppliers,
purchase orders and stock movement from one place. Low-stock items are flagged automatically, every
change to stock is written to an auditable ledger, and purchase orders support partial receipts.

**Stack:** Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, Spring Security, Thymeleaf, Bean Validation,
Maven. Database: H2 (zero-setup default), MySQL or PostgreSQL via profiles.

## Features

| Module | What it does |
|---|---|
| Authentication and roles | Form login, BCrypt passwords, CSRF protection, three roles with server-side authorization |
| Product catalogue | SKU, name, category, unit, cost/selling price (`BigDecimal`), reorder level, location, search, filters, pagination, archive |
| Supplier management | Supplier details, products supplied, active/inactive, search |
| Inventory ledger | Stock in, stock out and adjustment (set counted quantity). Each change records quantity, balance, reason, reference, user and timestamp |
| Purchase orders | Draft -> Ordered -> Partially received / Received (or Cancelled), partial receipts, outstanding quantities |
| Dashboard and reports | Products, units, stock value, low-stock list with a stock-vs-reorder gauge, orders by status, recent movements |
| Audit and validation | Quantities, prices and status transitions validated on the server; row locking and optimistic locking prevent lost updates |

## Quick start (about two minutes)

Requirements: **JDK 17+** and **Maven 3.9+**.

```bash
git clone <your-repository-url>
cd smart-inventory
mvn spring-boot:run
```

Open <http://localhost:8080>. On first start the app creates demo data (users, categories, products,
suppliers and one purchase order). Data is stored in `./data/` (H2 file database), so it survives restarts.
Delete the `data` folder to start fresh.

### Demo accounts (demo data only)

| Username | Password | Role | Can do |
|---|---|---|---|
| `admin` | `Admin@123` | Administrator | Everything, including user management |
| `inventory` | `Inventory@123` | Inventory staff | Products, categories, stock transactions, receive orders |
| `purchasing` | `Purchasing@123` | Purchasing staff | Suppliers, purchase orders, receive orders |

All roles can view products, suppliers, the ledger, orders and the dashboard.
Turn demo data off with `APP_SEED_ENABLED=false` and create real users from **Users** (log in once with a seeded admin first).

## Try the handbook scenario

1. Sign in as `inventory`. **Wireless barcode scanner (EL-1001)** has 8 units against a reorder level of 10, so it
   shows as *low stock* on the dashboard.
2. Sign in as `purchasing` and open **PO-00001** (already placed for 40 scanners).
3. Under *Receive goods* enter **25** and choose *Record receipt*. Stock moves from 8 to 33, the order becomes
   *Partially received* and 15 units stay outstanding.
4. Open the product or the **Stock ledger** to see the `PO receipt` entry with the user, reference and balance.
5. Receive the remaining 15 to complete the order.

## Using MySQL or PostgreSQL

```bash
# MySQL
export DB_URL="jdbc:mysql://localhost:3306/smart_inventory?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC"
export DB_USERNAME=root DB_PASSWORD=your-password
mvn spring-boot:run -Dspring-boot.run.profiles=mysql

# PostgreSQL (create the database first: CREATE DATABASE smart_inventory;)
export DB_URL="jdbc:postgresql://localhost:5432/smart_inventory"
export DB_USERNAME=postgres DB_PASSWORD=your-password
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

See `.env.example` for the variables. Never commit real credentials.

## Running the tests

```bash
mvn test
```

Covers stock rules (insufficient stock, adjustments, mandatory reason, low-stock threshold), the full
purchase-order flow including the 8 -> 33 partial-receipt scenario, over-receipt rejection, cancellation rules,
and role-based access (including CSRF). Details in [docs/TEST_EVIDENCE.md](docs/TEST_EVIDENCE.md).

## Build a runnable jar

```bash
mvn clean package
java -jar target/smart-inventory-1.0.0.jar --spring.profiles.active=mysql
```

## Project structure

```
src/main/java/com/weintern/inventory
  config/       SecurityConfig, DataInitializer (demo data), global model attributes
  domain/       JPA entities and enums
  repository/   Spring Data repositories (search queries, row locks)
  service/      Business rules: InventoryService, PurchaseOrderService, ProductService, ...
  dto/          Validated form objects (the entities are never bound directly to requests)
  web/          MVC controllers and exception handling
  exception/    BusinessException, ResourceNotFoundException
src/main/resources/templates   Thymeleaf pages
src/test/java                  Unit, integration and security tests
docs/                          Requirements, architecture + ER diagram, test evidence, project plan
```

## Design decisions worth knowing

- **One door for stock changes.** `InventoryService` is the only code that changes `quantityOnHand`. It locks the
  product row, validates the result, and writes the ledger row in the same transaction.
- **Receiving is all-or-nothing.** `PurchaseOrderService.receive` locks the order, validates every line, then adds stock.
  If any line is invalid nothing changes.
- **Low stock means at or below the reorder level** (quantity <= reorder level).
- **Money is `BigDecimal` / `DECIMAL(12,2)`**, never floating point.
- **Products are archived, not deleted**, so ledger history is never orphaned.
- **Lazy loading in views.** `spring.jpa.open-in-view` is left on for simplicity at this scale.
- **Schema management.** Hibernate `ddl-auto=update` is used for convenience. For production, switch to Flyway/Liquibase migrations.

## Known limitations

- Single stock location per product (multi-warehouse transfers are an optional stretch goal, not implemented).
- No CSV export, batch/expiry tracking or lead-time summaries (optional stretch goals).
- No password reset or self-service profile page; administrators create and disable users.
- The default H2 file database is for demonstration. Use MySQL or PostgreSQL for real use.

## Credits

No external datasets are used. Demo products, suppliers and people are fictional. Fonts are system fonts; no external assets.
