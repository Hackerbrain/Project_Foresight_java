# Requirements and Use-Case Summary

## 1. Problem statement
Small businesses track stock in notebooks and spreadsheets. Counts drift, low stock is noticed too late, and nobody
can say who changed a number. This system gives one place to manage products, suppliers, purchase orders and stock
movement, with a traceable history and clear low-stock visibility.

## 2. Users and roles

| Role | Purpose | Permissions |
|---|---|---|
| Administrator | Owns the system | All actions, plus user management |
| Inventory staff | Looks after stock | Manage products and categories; record stock in/out/adjustments; receive purchase orders |
| Purchasing staff | Buys from suppliers | Manage suppliers; create/place/cancel purchase orders; receive purchase orders |

All signed-in users can view products, suppliers, the ledger, purchase orders and the dashboard.

## 3. User stories

| ID | As a... | I want to... | So that... |
|---|---|---|---|
| US-01 | any user | sign in securely | only authorised people use the system |
| US-02 | inventory staff | add and edit products with SKU, price and reorder level | the catalogue is accurate |
| US-03 | any user | search and filter products, and show only low-stock items | I can find items quickly |
| US-04 | inventory staff | record stock in, stock out and adjustments with a reason | inventory matches reality and is auditable |
| US-05 | any user | view a searchable movement history | I can see who changed what and when |
| US-06 | purchasing staff | manage suppliers and the products they supply | I know where to buy each item |
| US-07 | purchasing staff | create a purchase order, add items and place it | replenishment is tracked |
| US-08 | inventory/purchasing staff | record partial or full receipt of an order | stock updates when goods arrive and outstanding units stay visible |
| US-09 | manager | see a dashboard of stock value, low stock and order status | I can decide what to reorder |
| US-10 | administrator | create and disable users and assign roles | access follows responsibilities |

## 4. Main use cases

**UC-1 Replenish low stock.** Stock falls to or below the reorder level -> product appears under *Low stock* ->
purchasing staff create a draft order for the supplier -> add items -> place order.

**UC-2 Receive goods.** Staff open an Ordered/Partially received order -> enter units received per line ->
system validates (not above outstanding), adds stock, writes one `PO receipt` ledger entry per line, and sets the
status to Partially received or Received.

**UC-3 Correct stock after a count.** Inventory staff choose *Adjustment*, enter the counted quantity and a reason ->
system records the difference as a signed movement.

## 5. Business rules

1. Quantity on hand can never be negative or exceed 1,000,000.
2. Every quantity change creates exactly one ledger entry with quantity change, balance after, reason, reference, user and time.
3. Low stock = quantity on hand <= reorder level.
4. Purchase-order status moves only Draft -> Ordered -> Partially received -> Received; Cancelled is allowed from Draft or Ordered if nothing has been received.
5. Items can only be added or removed while an order is a Draft; an order needs at least one item to be placed.
6. A receipt can never exceed the outstanding quantity of a line.
7. Products with history are archived, not deleted.

## 6. Acceptance criteria traceability

| Acceptance criterion (handbook) | Where it is met | Evidence |
|---|---|---|
| Authorized users can manage products and suppliers | `ProductController`, `SupplierController`, `SecurityConfig` | `SecurityAccessTest`, manual tests M-02, M-06 |
| Every stock change updates inventory and creates a movement record | `InventoryService.apply` | `InventoryServiceTest` |
| Receiving a PO updates stock consistently and supports partial receipt | `PurchaseOrderService.receive` | `PurchaseOrderServiceTest.partialThenFullReceipt...` |
| Low-stock status is based on the reorder threshold | `Product.isLowStock`, `ProductRepository.findLowStock` | `InventoryServiceTest.lowStockIsBasedOnReorderLevel` |
| Invalid quantities and unauthorized actions are rejected with clear errors | Bean Validation, `BusinessException`, `SecurityConfig` | `InventoryServiceTest`, `SecurityAccessTest`, M-04, M-08 |
| A fresh setup can be run using the README | `README.md`, H2 default, `DataInitializer` | Manual test M-01 |

## 7. Out of scope
Enterprise integrations (ERP, barcode hardware, payment), multi-currency, customer-facing sales, email notifications.
